class TrajectoryPoint {
  shipId: number;
  longitude: number;
  latitude: number;
  timestamp: string;
  anomalyScore: number;

  constructor(
    shipId: number,
    longitude: number,
    latitude: number,
    timestamp: string,
    anomalyScore: number,
  ) {
    this.shipId = shipId;
    this.longitude = longitude;
    this.latitude = latitude;
    this.timestamp = timestamp;
    this.anomalyScore = anomalyScore;
  }
}

export default TrajectoryPoint;
