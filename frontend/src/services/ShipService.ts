// Create a class to get ship information from the server
import ShipDetails from "../model/ShipDetails";
import HttpSender from "../utils/HttpSender";
import AISSignal from "../dtos/AISSignal";
import AnomalyInformation from "../dtos/AnomalyInformation";
import ErrorNotificationService from "./ErrorNotificationService";

class ShipService {
  static httpSender: HttpSender = new HttpSender();

  // Backend API endpoints for retrieving (polling) the information about
  // the latest AIS signals received for each ship
  // and the latest computed Anomaly Scores for each ship
  static shipsAISEndpoint = "/ships/ais";
  static shipsAnomalyInfoEndpoint = "/ships/anomaly";

  /**
   * This method queries the backend for both the Ships Array and the Anomaly Scores Array.
   * The resulting array of type ShipDetails is the result of aggregating the retrieved arrays.
   * The method returns a promise that resolves to an array of ShipDetails.
   * @returns a promise that resolves to an array of ShipDetails.
   */
  static queryBackendForShipsArray: () => Promise<ShipDetails[]> = async () => {
    // Fetch the latest AIS signals of all monitored ships
    const AISResults = ShipService.getAllAISResults();

    // Fetch the latest computed anomaly scores of all monitored ships
    const AnomalyInfoResults = ShipService.getAnomalyInfoResults();

    // As the resulting list of type ShipDetails is the result of an aggregation,
    // we have to wait for both Promise objects to resolve to lists as we can not
    // aggregate unresolved Promise objects
    const result0 = await Promise.all([AISResults, AnomalyInfoResults]);
    const [aisResults, anomalyInfoResults] = result0;
    return aisResults.reduce((result: ShipDetails[], aisSignal: AISSignal) => {
      // We match the AISSignal items based on the ID (hash) of the ship
      const matchingAnomalyInfo = anomalyInfoResults.find(
        (item) => item["id"] === aisSignal["id"],
      );
      if (matchingAnomalyInfo) {
        // Compose a ShipDetails instance given the matching AISSignal and AnomalyInformation items
        const shipDetailsItem = ShipService.createShipDetailsFromDTOs(
          aisSignal,
          matchingAnomalyInfo,
        );
        result.push(shipDetailsItem);
      } else {
        ErrorNotificationService.addError(
          "No matching anomaly info was found.",
        );
      }
      return result;
    }, []);
  };

  /**
   * Helper function that leverages the static instance of HttpSender in order to query the backend server
   * @returns - array of the latest DTOs that encapsulate the last received AIS info of the ships
   */
  static getAllAISResults: () => Promise<AISSignal[]> = () => {
    return getResponseMappedToArray(
      ShipService.shipsAISEndpoint,
      // eslint-disable-next-line
      (item: any) => {
        return {
          id: item.id,
          speed: item.speed,
          long: item.longitude,
          lat: item.latitude,
          course: item.course,
          departurePort: item.departurePort,
          heading: item.heading,
          timestamp: item.timestamp,
        };
      },
    );
  };

  /**
   * Helper function that leverages the static instance of HttpSender in order to query the backend server
   * @returns - array of the latest DTOs that encapsulate the last anomaly info of the ships
   */
  static getAnomalyInfoResults: () => Promise<AnomalyInformation[]> = () => {
    return getResponseMappedToArray(
      ShipService.shipsAnomalyInfoEndpoint,
      // eslint-disable-next-line
      (item: any) => {
        return {
          id: item.id,
          description: item.explanation,
          anomalyScore: item.score,
        };
      },
    );
  };

  /**
   * Helper function that takes 2 matching instances of AISSignal and AnomalyInformation
   * and creates a new instance of ShipDetails that incorporates the information from them.
   * @param aisSignal - the instance that encapsulates the AIS info of a ship
   * @param anomalyInfo - the instance that encapsulates the anomaly info of a ship
   */
  static createShipDetailsFromDTOs(
    aisSignal: AISSignal,
    anomalyInfo: AnomalyInformation,
  ): ShipDetails {
    // TODO: Modify the 'explanation' field of the ShipDetails instance
    return new ShipDetails(
      aisSignal.id,
      aisSignal.heading,
      aisSignal.lat,
      aisSignal.long,
      anomalyInfo.anomalyScore,
      anomalyInfo.description,
      aisSignal.departurePort,
      aisSignal.course,
      aisSignal.speed,
    );
  }
}

/**
 * Helper function that gets the response, and then checks if the response
 * is an array which is non-empty. If it is an array and non-empty, then it
 * calls the callback function.
 *
 * In case the result is not an array, or it is empty, then the notification
 * is sent to ErrorNotificationService, and this method returns just an empty array.
 *
 * @param endpoint The path of the API endpoint which will be called.
 * @param mapCallback The callback function that will be applied to each item in the array,
 *  in order to map response elements to resulting array.
 */
async function getResponseMappedToArray<T, K>(
  endpoint: string,
  mapCallback: (item: T) => K,
) {
  const response = await ShipService.httpSender.get(endpoint);

  if (!Array.isArray(response)) {
    ErrorNotificationService.addError("Server returned not an array.");
    return [];
  }

  if (response.length === 0) {
    ErrorNotificationService.addInformation("Ship array is empty.");
    return [];
  }

  const responseWithoutNulls = response.filter((item) => item != null);
  if (responseWithoutNulls.length !== response.length) {
    ErrorNotificationService.addError("Ship array contained null items.");
  }

  return responseWithoutNulls.map((item: T) => {
    return mapCallback(item);
  });
}

export default ShipService;
