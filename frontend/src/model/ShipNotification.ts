import ShipDetails from "./ShipDetails";
import TimeUtilities from "../utils/TimeUtilities";

class ShipNotification {
  id: number;
  isRead: boolean;
  shipDetails: ShipDetails;

  /**
   * Constructor for the notifications class
   *
   * @param id id of the notification
   * @param isRead status of whether the notification was read
   * @param shipDetails corresponding ship details of the notification
   */
  constructor(id: number, isRead: boolean, shipDetails: ShipDetails) {
    this.id = id;
    this.isRead = isRead;
    this.shipDetails = shipDetails;
  }

  /**
   * This method returns a list of properties of the notification. This is used
   * to present the properties in a human-readable format, when the notification
   * details page is opened.
   *
   * @returns a list of properties of the ship
   */
  getPropertyList() {
    return [
      { type: "Ship ID", value: this.shipDetails.id },
      {
        type: "Time of the anomaly",
        value: TimeUtilities.reformatTimestamp(
          this.shipDetails.correspondingTimestamp,
        ),
      },
      { type: "Anomaly Score", value: this.shipDetails.anomalyScore + "%" },
      { type: "Explanation", value: this.shipDetails.explanation },
      {
        type: "Highest Recorded Anomaly Score At The Time",
        value: this.shipDetails.maxAnomalyScore.toString() + "%",
      },
      { type: "Heading", value: this.shipDetails.heading.toString() },
      { type: "Departure Port", value: this.shipDetails.departurePort },
      { type: "Course", value: this.shipDetails.course.toString() },
      {
        type: "Position",
        value: this.shipDetails.getPositionString(),
      },
      { type: "Speed", value: this.shipDetails.speed.toString() },
    ];
  }
}

export default ShipNotification;
