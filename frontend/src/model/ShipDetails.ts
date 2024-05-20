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
      { type: "Ship ID", value: this.id.toString() },
      { type: "Explanation", value: this.explanation.toString() },
      { type: "Anomaly Score", value: this.anomalyScore.toString() + "%" },
      {
        type: "Highest Recorded Anomaly Score",
        value: this.maxAnomalyScore.toString() + "%",
      },
      {
        type: "Timestamp of the Highest Anomaly Score",
        value: this.correspondingTimestamp,
      },
      { type: "Heading", value: this.heading.toString() },
      { type: "Departure Port", value: this.departurePort },
      { type: "Course", value: this.course.toString() },
      {
        type: "Position",
        value: this.getPositionString(),
      },
      { type: "Speed", value: this.speed.toString() },
      { type: "Heading", value: this.heading.toString() },
      { type: "Departure", value: this.departurePort.toString() },
    ];
  }

  getPositionString() {
    return roundShipDetail(this.lat) + ", " + roundShipDetail(this.lng);
  }
}

function roundShipDetail(x: number) {
  return Math.round(x * ShipDetails.rounding) / ShipDetails.rounding;
}

export default ShipDetails;
