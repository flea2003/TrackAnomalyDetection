// Create a class to get ship information from the server
import ShipDetails from "../model/ShipDetails";
import APIResponseItem from "../templates/APIResponseItem";
import HttpSender from "../utils/HttpSender";
import ErrorNotificationService from "./ErrorNotificationService";

class ShipService {
  static httpSender: HttpSender = new HttpSender();

  /** Backend API endpoint for retrieving (polling) the information about
   * the latest ship details for each ship, encapsulating: the AIS information and current/max anomaly information
   */
  static shipsCurrentDetailsEndpoint = "/ships/details";

  /**
   * This method queries the backend for the CurrentShipDetails array
   * The resulting array of type ShipDetails is the result of transforming the retrieved arrays.
   * The method returns a promise that resolves to an array of ShipDetails.
   * @returns a promise that resolves to an array of ShipDetails.
   */
  static queryBackendForShipsArray: () => Promise<ShipDetails[]> = () => {
    // Fetch the latest ship details of all monitored ships
    return ShipService.getCurrentShipDetails();
  };

  /**
   * Helper function that leverages the static instance of HttpSender in order to query the backend server
   * @returns - array of the latest DTO that encapsulate the last ship details of the ships
   */
  static getCurrentShipDetails: () => Promise<ShipDetails[]> = async () => {
    const response = await ShipService.httpSender
      .get(ShipService.shipsCurrentDetailsEndpoint);

    if (!Array.isArray(response)) {
      ErrorNotificationService.addError("Server returned not an array")
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

    return responseWithoutNulls.map(
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (item: any) => ShipService.extractCurrentShipDetails(item)
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
}

export default ShipService;
