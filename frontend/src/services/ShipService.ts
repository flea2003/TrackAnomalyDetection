// Create a class to get ship information from the server
import ShipDetails from "../model/ShipDetails";
import APIResponseItem from "../templates/APIResponseItem";
import HttpSender from "../utils/communication/HttpSender";
import ErrorNotificationService from "./ErrorNotificationService";
import endpointConfig from "../configs/endpointsConfig.json";
import connectionSettings from "../configs/connectionSettings.json";

class ShipService {
  /** Backend API endpoint for retrieving (polling) the information about
   * the latest ship details for each ship, encapsulating: the AIS information and current/max anomaly information
   */
  static shipsCurrentDetailsEndpoint =
    connectionSettings.backendCurrentShipDetailsEndpoint;
  static historyEndpoint = connectionSettings.backendHistoryEndpoint;

  /**
   * This method queries the backend for the CurrentShipDetails array
   * The resulting array of type ShipDetails is the result of transforming the
   * retrieved array. The method returns a promise that resolves to an array of
   * ShipDetails, which is filtered based on the anomaly threshold, and also sorted
   *
   * @returns a promise that resolves to an array of ShipDetails.
   */
  static queryBackendForShipsArray: () => Promise<ShipDetails[]> = async () => {
    const response = await HttpSender.get(
      ShipService.shipsCurrentDetailsEndpoint,
    );

    if (!Array.isArray(response)) {
      ErrorNotificationService.addError("Server returned not an array");
      return [];
    }

    if (response.length === 0) {
      ErrorNotificationService.addInformation("Ship array is empty");
      return [];
    }

    const responseWithoutNulls = response.filter((item) => item !== null);
    if (responseWithoutNulls.length !== response.length) {
      ErrorNotificationService.addError("Ship array contained null items");
    }

    return ShipService.sortList(
      responseWithoutNulls.map((item: APIResponseItem) =>
        ShipService.extractCurrentShipDetails(item),
      ),
      "desc",
    );
  };

  /**
   * Utility method that parses the received JSON data and assembles the
   * corresponding ShipDetails object
   * @param item - the received JSON object
   * @return - dummy ShipDetails object
   */
  static extractCurrentShipDetails: (item: APIResponseItem) => ShipDetails = (
    item,
  ) => {
    if (
      !item.currentAISSignal &&
      item.currentAnomalyInformation &&
      item.maxAnomalyScoreInfo
    ) {
      return new ShipDetails(
        item.currentAnomalyInformation.id,
        0,
        0,
        0,
        item.currentAnomalyInformation.correspondingTimestamp,
        item.currentAnomalyInformation.score,
        item.currentAnomalyInformation.explanation,
        item.maxAnomalyScoreInfo.maxAnomalyScore,
        item.maxAnomalyScoreInfo.correspondingTimestamp,
        "Information not available (yet)",
        0,
        0,
      );
    }
    if (
      item.currentAISSignal &&
      (!item.currentAnomalyInformation || !item.maxAnomalyScoreInfo)
    ) {
      return new ShipDetails(
        item.currentAISSignal.id,
        item.currentAISSignal.heading,
        item.currentAISSignal.latitude,
        item.currentAISSignal.longitude,
        item.currentAISSignal.timestamp,
        -1,
        "Information not available (yet)",
        0,
        "Information not available (yet)",
        item.currentAISSignal.departurePort,
        item.currentAISSignal.course,
        item.currentAISSignal.speed,
      );
    } else {
      return new ShipDetails(
        item.currentAISSignal.id,
        item.currentAISSignal.heading,
        item.currentAISSignal.latitude,
        item.currentAISSignal.longitude,
        item.currentAISSignal.timestamp,
        item.currentAnomalyInformation.score,
        item.currentAnomalyInformation.explanation,
        item.maxAnomalyScoreInfo.maxAnomalyScore,
        item.maxAnomalyScoreInfo.correspondingTimestamp,
        item.currentAISSignal.departurePort,
        item.currentAISSignal.course,
        item.currentAISSignal.speed,
      );
    }
  };

  /**
   * Utility method that sorts the list of ShipDetails entries based on the corresponding values of the anomaly score.
   * @param list - fetched list with ShipDetails instances
   * @param order - either `asc` for ascending or '`desc` for descending (default)
   */
  static sortList = (list: ShipDetails[], order = "desc") => {
    if (!["desc", "asc"].includes(order)) {
      ErrorNotificationService.addError("Invalid sorting order");
      return [];
    }
    const sortedList = list.sort((a, b) => {
      const aScore = a.anomalyScore;
      const bScore = b.anomalyScore;
      if (aScore > bScore) {
        return -1;
      }
      if (aScore === bScore) {
        return 0;
      } else {
        return 1;
      }
    });
    if (order === "asc") {
      return sortedList.reverse();
    }
    return sortedList;
  };
}

export default ShipService;
