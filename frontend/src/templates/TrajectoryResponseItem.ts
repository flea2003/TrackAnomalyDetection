interface TrajectoryResponseItem {
  aisSignal: {
    id: number;
    speed: number;
    longitude: number;
    latitude: number;
    course: number;
    heading: number;
    timestamp: string;
    departurePort: string;
  };
  anomalyScore: number;
}
export default TrajectoryResponseItem;
