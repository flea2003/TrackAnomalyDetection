import TrajectoryPoint from "./TrajectoryPoint";
import { LatLng } from "leaflet";

/**
 * A class that stores a pair of a trajectory object and a notification
 */
class TrajectoryAndNotificationPair {
  trajectory: TrajectoryPoint[];
  notification: LatLng | undefined;

  constructor(trajectory: TrajectoryPoint[], notification: LatLng | undefined) {
    this.trajectory = trajectory;
    this.notification = notification;
  }
}

export default TrajectoryAndNotificationPair;