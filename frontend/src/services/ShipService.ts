// Create a class to get ship information from the server
import ShipDetails from "../model/ShipDetails";
import APIResponseItem from "../templates/APIResponseItem";
import HttpSender from "../utils/HttpSender";

class ShipService {
  static httpSender: HttpSender = new HttpSender();

  /** Backend API endpoint for retrieving (polling) the information about
     / the latest ship details for each ship, encapsulating: the AIS information and current/max anomaly information
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
  static getCurrentShipDetails: () => Promise<ShipDetails[]> = () => {
    return ShipService.httpSender
      .get(ShipService.shipsCurrentDetailsEndpoint)
      .then((response) => {
        // TODO: Implementing proper error handling for the cases in which the retrieved array is empty
        if (Array.isArray(response) && response.length > 0) {
          return response.map(
            // eslint-disable-next-line
            (item: any) => {
              // TODO: fix this place (better handling of this case)
              if (item == null) {
                return ShipService.dummyShipDetails();
              } else {
                console.log(item);
                return ShipService.extractCurrentShipDetails(item);
              }
            },
          );
        } else {
          return [];
        }
      });
  };

  /**
   * Utility method that returns a dummy instance of CurrentShipDetails.
   * @return - dummy CurrentShipDetails object
   */
  static dummyShipDetails: () => ShipDetails = () => {
    return new ShipDetails(-1, 0, 0, 0, -1, "", 0, "", "", 0, 0);
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
    console.log(item);
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
