import AnomalyInformation from "./AnomalyInformation";
import MaxAnomalyScoreDetails from "./MaxAnomalyScoreDetails";

interface ExtendedAnomalyInformation {
  anomalyInformation: AnomalyInformation;
  maxAnomalyScoreInfo: MaxAnomalyScoreDetails;
}

export default ExtendedAnomalyInformation;
