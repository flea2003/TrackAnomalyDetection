class ShipDetails {
  static rounding = 1000;
  id: number;
  heading: number;
  lat: number;
  lng: number;
  timestamp: string;
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
    timestamp: string,
    anomalyScore: number,
    description: string,
    maxAnomalyScore: number,
    maxAnomalyScoreTimestamp: string,
    departurePort: string,
    course: number,
    speed: number,
  ) {
    this.id = id;
    this.heading = heading;
    this.lat = lat;
    this.lng = lng;
    this.timestamp = timestamp;
    this.anomalyScore = anomalyScore;
    this.explanation = description;
    this.maxAnomalyScore = maxAnomalyScore;
    this.correspondingTimestamp = maxAnomalyScoreTimestamp;
    this.departurePort = departurePort;
    this.course = course;
    this.speed = speed;
  }

  getRoundedLatitude() {
    return roundShipDetail(this.lat);
  }

  getRoundedLongitude() {
    return roundShipDetail(this.lng);
  }
}

/**
 * Utility method for processing numerical values.
 * @param x - numerical value
 */
function roundShipDetail(x: number) {
  return Math.round(x * ShipDetails.rounding) / ShipDetails.rounding;
}

export default ShipDetails;
