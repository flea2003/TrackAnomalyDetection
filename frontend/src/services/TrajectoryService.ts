import TrajectoryPoint from "../model/TrajectoryPoint";
import HttpSender from "../utils/communication/HttpSender";
import ErrorNotificationService from "./ErrorNotificationService";
import TrajectoryResponseItem from "../templates/TrajectoryResponseItem";
import ShipDetails from "../model/ShipDetails";

class TrajectoryService {

  /**
   * Backend API endpoint for retrieving the subsampled information about the
   * previous AIS signals and their corresponding anomaly information of a ship.
   * The fetched information is used for drawing past trajectories
   */
  static shipSampledHistory = "/ships/history/sampled/";

  /**
   * Id of the ship whose trajectory is/was most recently displayed on the map
   */
  static idOfCurrentlyStoredShip = -1;

  /**
   * Timestamp of the newest signal that corresponds to the displayed trajectory
   */
  static newestTimestampOfCurrentlyStoredShip = "";



  /**
   * Function that retrieves the subsampled information about the previous AIS
   * signals and their corresponding anomaly information of a particular ship.
   * The fetched information is used for drawing past trajectories
   *
   * Also, note that the trajectory should come in sorted value, with first elements
   * of the array being the most recent signals.
   *
   * @param id id of the ship that whose trajectory is requested
   */
  static queryBackendForSampledHistoryOfAShip = async (id: number) =>
  {
    const response = await HttpSender.get(
      TrajectoryService.shipSampledHistory + id
    );

    if (!Array.isArray(response)) {
      ErrorNotificationService.addError("Server returned not an array for the trajectory history");
      return [];
    }

    if (response.length === 0) {
      ErrorNotificationService.addInformation("Trajectory length is 0");
      return [];
    }

    const responseWithoutNulls = response.filter((item) => item !== null);
    if (responseWithoutNulls.length !== response.length) {
      ErrorNotificationService.addError("Trajectory array contained null items");
    }

    const finalResult = responseWithoutNulls.map((x: TrajectoryResponseItem) =>
      new TrajectoryPoint(x.shipId, x.longitude, x.latitude, x.timestamp, x.anomalyScore));

    this.newestTimestampOfCurrentlyStoredShip = finalResult[0].timestamp;
    this.idOfCurrentlyStoredShip = finalResult[0].shipId;

    return finalResult;
  }

  /**
   * Function that checks whether backend should be queried for a more recent version
   * of the trajectory.
   *
   * If trajectory is needed for a ship which is not the same that has just been displayed
   * (meaning that the stored id and new id differ), then backend needs to be queried.
   *
   * Also, if the same ship is still being displayed, however, the newest displayed signal
   * is older than the most recent update of the ship, trajectory should also be updated.
   *
   * @param ship instance of a ship whose trajectory needs to be displayed
   */
  static shouldQueryBackend = (ship: ShipDetails | undefined) => {
      if (ship === undefined) return false;

      const timestamp = ship.timestamp;
      const id = ship.id;

      if (id !== this.idOfCurrentlyStoredShip) return true;
      return timestamp !== this.newestTimestampOfCurrentlyStoredShip;
  }
}

export default TrajectoryService;