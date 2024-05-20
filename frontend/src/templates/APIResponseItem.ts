interface APIResponseItem {
  aisInformation: {
    id: number;
    speed: number;
    longitude: number;
    latitude: number;
    course: number;
    heading: number;
    timestamp: string;
    departurePort: string;
  };
  anomalyInformation: {
    id: number;
    anomalyScore: number;
    explanation: string;
    correspondingTimestamp: string;
  };
  maxAnomalyScoreDetails: {
    maxAnomalyScore: number;
    correspondingTimestamp: string;
  };
}

export default APIResponseItem;