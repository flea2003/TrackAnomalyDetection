class ShipDetails {
  static rounding = 1000;
  id: number;
  heading: number;
  lat: number;
  lng: number;
  anomalyScore: number;
  explanation: string;
  maxAnomalyScore: number;
  correspondingTimestamp: string;
  departurePort: string;
  course: number;
  speed: number;

  constructor(
    id: number,
    heading: number,
    lat: number,
    lng: number,
    anomalyScore: number,
    description: string,
    maxAnomalyScore: number,
    correspondingTimestamp: string,
    departurePort: string,
    course: number,
    speed: number,
  ) {
    this.id = id;
    this.heading = heading;
    this.lat = lat;
    this.lng = lng;
    this.anomalyScore = anomalyScore;
    this.explanation = description;
    this.maxAnomalyScore = maxAnomalyScore;
    this.correspondingTimestamp = correspondingTimestamp;
    this.departurePort = departurePort;
    this.course = course;
    this.speed = speed;
  }

  /**
   * This method returns a list of properties of the ship. This is used to present the properties in a human-readable format,
   * when the ship details page is opened. This should not include the name of the ship.
   *
   * @returns a list of properties of the ship
   */
  getPropertyList() {
    return [
      { type: "Object type", value: "Ship" },
      { type: "Anomaly score", value: this.anomalyScore + "%" },
      { type: "Explanation", value: this.explanation },
      {
        type: "Highest Recorded Anomaly Score",
        value: this.maxAnomalyScore + "%",
      },
      {
        type: "Timestamp of the Highest Anomaly Score",
        value: this.correspondingTimestamp,
      },
      { type: "Heading", value: "" + this.heading },
      { type: "Departure Port", value: "" + this.departurePort },
      { type: "Course", value: "" + this.course },
      { type: "Speed", value: "" + this.speed },
      {
        type: "Latitude",
        value:
          "" +
          Math.round(this.lat * ShipDetails.rounding) / ShipDetails.rounding,
      },
      {
        type: "Longitude",
        value:
          "" +
          Math.round(this.lng * ShipDetails.rounding) / ShipDetails.rounding,
      },
      { type: "Heading", value: "" + this.heading },
    ];
  }
}

export default ShipDetails;
