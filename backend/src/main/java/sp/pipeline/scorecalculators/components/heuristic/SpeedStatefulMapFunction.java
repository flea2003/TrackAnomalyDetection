package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Utils.harvesineDistance;

import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

    /**
     * Performs a stateful map operation from an incoming AISSignal to an Anomaly Information object.
     *
     * @param value The input value.
     * @return the computed Anomaly Information object
     * @throws Exception - exception from value state descriptors
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        AnomalyInformation anomalyInformation = new AnomalyInformation();
        AnomalyInformation pastAnomalyInformation = getAnomalyInformationValueState().value();
        AISSignal pastAISSignal = getAisSignalValueState().value();

        if (pastAnomalyInformation != null && pastAISSignal != null) {
            double globeDistance = harvesineDistance(value.getLatitude(), value.getLongitude(),
                    pastAISSignal.getLatitude(), pastAISSignal.getLongitude());
            double timeDifference = value.getTimestamp().difference(pastAISSignal.getTimestamp());
            double computedSpeed = globeDistance / timeDifference;

            if (computedSpeed > 40) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            } else if (Math.abs(value.getSpeed() - computedSpeed) > 10) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            } else if ((getAisSignalValueState().value().getSpeed() - value.getSpeed()) / timeDifference > 50) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
            anomalyInformation.setShipHash(value.getShipHash());
            anomalyInformation.setCorrespondingTimestamp(value.getTimestamp());

            if (getLastDetectedAnomalyTime().value() != null && value.getTimestamp()
                .difference(getLastDetectedAnomalyTime().value()) <= 30) {
                anomalyInformation.setScore(33.0f);
                anomalyInformation.setExplanation("Bad Speed.");
            } else {
                anomalyInformation.setScore(0.0f);
                anomalyInformation.setExplanation("Good Speed.");
            }
        } else {
            anomalyInformation.setScore(0.0f);
            anomalyInformation.setExplanation("Good Speed.");
            anomalyInformation.setShipHash(value.getShipHash());
            anomalyInformation.setCorrespondingTimestamp(value.getTimestamp());
        }
        getAnomalyInformationValueState().update(anomalyInformation);
        getAisSignalValueState().update(value);
        return anomalyInformation;
    }

}
