// Create a class to get ship information from the server
import ShipDetails from "../model/ShipDetails";
import HttpSender from "../utils/HttpSender";
import AISSignal from "../dtos/AISSignal";
import CurrentShipDetails from "../dtos/CurrentShipDetails";

class ShipService {

    static httpSender: HttpSender = new HttpSender();

    /** Backend API endpoint for retrieving (polling) the information about
    / the latest ship details for each ship, encapsulating: the AIS information and current/max anomaly information
    */
    static shipsCurrentDetailsEndpoint = "/ships/details";

  /**
   * This method queries the backend for both the Ships Array and the Anomaly Scores Array.
   * The resulting array of type ShipDetails is the result of aggregating the retrieved arrays.
   * The method returns a promise that resolves to an array of ShipDetails.
   * @returns a promise that resolves to an array of ShipDetails.
   */
  static queryBackendForShipsArray: () => Promise<ShipDetails[]> = () => {
    // Fetch the latest ship details of all monitored ships
    const currentShipDetails = ShipService.getCurrentShipDetails();

    return currentShipDetails;
  };


  /**
   * Helper function that leverages the static instance of HttpSender in order to query the backend server
   * @returns - array of the latest DTOs that encapsulate the last extended anomaly info of the ships
   */
  static getAnomalyInfoResults: () => Promise<CurrentShipDetails[]> =
    () => {
      return ShipService.httpSender
        .get(ShipService.shipsCurrentDetailsEndpoint)
        .then((response) => {
          // TODO: Implementing proper error handling for the cases in which the retrieved array is empty
          if (Array.isArray(response) && response.length > 0) {
            const currentShipDetails: CurrentShipDetails[] =
              response.map(
                // eslint-disable-next-line
                (item: any) => {
                  // TODO: fix this place (better handling of this case)
                  if (item == null) {
                    return {
                      anomalyInformation: {
                        id: "null ship",
                        description: "",
                        anomalyScore: -1,
                      },
                      maxAnomalyScoreDetails: {
                        maxAnomalyScore: 0,
                        correspondingTimestamp: "",
                      },
                    };
                  } else {
                    const maxAnomScoreDet = item.maxAnomalyScoreDetails;
                    console.log(maxAnomScoreDet);
                    return {
                      anomalyInformation: {
                        id: item.anomalyInformation.id,
                        description: item.anomalyInformation.explanation,
                        anomalyScore: item.anomalyInformation.score,
                      },
                      maxAnomalyScoreDetails: {
                        maxAnomalyScore:
                          item.maxAnomalyScoreDetails.maxAnomalyScore,
                        correspondingTimestamp:
                          item.maxAnomalyScoreDetails.correspondingTimestamp,
                      },
                    };
                  }
                },
              );
            return currentShipDetails;
          } else {
            return [];
          }
        });
    };

export default ShipService;
