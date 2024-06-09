interface NotificationResponseItem {
  id: number;
  shipID: number;
  isRead: boolean;
  currentShipDetails: {
    currentAISSignal: {
      id: number;
      speed: number;
      longitude: number;
      latitude: number;
      course: number;
      heading: number;
      timestamp: string;
      departurePort: string;
    };
    currentAnomalyInformation: {
      id: number;
      score: number;
      explanation: string;
      correspondingTimestamp: string;
    };
    maxAnomalyScoreInfo: {
      maxAnomalyScore: number;
      correspondingTimestamp: string;
    };
  };
}
export default NotificationResponseItem;
