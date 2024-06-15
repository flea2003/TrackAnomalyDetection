import TrajectoryPoint from "../model/TrajectoryPoint";
import HttpSender from "../utils/communication/HttpSender";
import ErrorNotificationService from "./ErrorNotificationService";
import TrajectoryResponseItem from "../templates/TrajectoryResponseItem";
import ShipDetails from "../model/ShipDetails";
import TimeUtilities from "../utils/TimeUtilities";

class TrajectoryService {

  /**
   * Backend API endpoint for retrieving the subsampled information about the
   * previous AIS signals and their corresponding anomaly information of a ship.
   * The fetched information is used for drawing past trajectories
   */
  static shipSampledHistory = "/ships/history/sampled/";

  static newestTimestampOfCurrentShip = "";



  /**
   * Function that retrieves the subsampled information about the previous AIS
   * signals and their corresponding anomaly information of a particular ship.
   * The fetched information is used for drawing past trajectories
   * @param id
   */
  static queryBackendForSampledHistoryOfAShip: (id: number) =>
    Promise<TrajectoryPoint[]> = async (id) =>
  {

    const response = await HttpSender.get(
      TrajectoryService.shipSampledHistory + id
    );

    console.log(response);
    console.log(TrajectoryService.shipSampledHistory + id);

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

    this.newestTimestampOfCurrentShip = responseWithoutNulls[0];

    return responseWithoutNulls.map((x: TrajectoryResponseItem) =>
      new TrajectoryPoint(x.shipId, x.longitude, x.latitude, x.timestamp, x.anomalyScore));

    // return HttpSender.getDummyData();
  }


  static shouldQueryBackend = (ships: ShipDetails[], id: number) => {
      const newestSignal = ships.find(x => x.id === id);
      if (newestSignal === undefined) return true;
      else return newestSignal.timestamp !== this.newestTimestampOfCurrentShip;
  }
}

export default TrajectoryService;