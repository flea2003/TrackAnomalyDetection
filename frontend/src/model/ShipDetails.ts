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
    return this.roundShipDetail(this.lat);
  }

  getRoundedLongitude() {
    return this.roundShipDetail(this.lng);
  }

  /**
   * Utility method for processing numerical values.
   * @param x - numerical value
   */
  roundShipDetail(x: number) {
    return Math.round(x * ShipDetails.rounding) / ShipDetails.rounding;
  }
}

/**
 * Compares the positions (longitude and latitude) of the two ships.
 * If any of the ship is null, returns false. Else returns true if the positions differ.
 *
 * @param ship1 first ship to compare
 * @param ship2 second ship to compare
 */
export function differentShipPositions(
  ship1: ShipDetails | null,
  ship2: ShipDetails | null,
) {
  if (ship1 === null || ship2 === null) return false;
  return ship1.lat !== ship2.lat || ship1.lng !== ship2.lng;
}

export default ShipDetails;
