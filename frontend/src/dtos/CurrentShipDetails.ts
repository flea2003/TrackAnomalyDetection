import AnomalyInformation from "./AnomalyInformation";
import MaxAnomalyScoreDetails from "./MaxAnomalyScoreDetails";
import AISSignal from "./AISSignal";

interface CurrentShipDetails {
  aisInformation: AISSignal;
  anomalyInformation: AnomalyInformation;
  maxAnomalyScoreDetails: MaxAnomalyScoreDetails;
}

export default CurrentShipDetails;
