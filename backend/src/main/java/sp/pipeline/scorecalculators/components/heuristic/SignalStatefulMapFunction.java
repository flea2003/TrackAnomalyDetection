package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.harvesineDistance;

import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

public class SignalStatefulMapFunction extends HeuristicStatefulMapFunction {

    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {

        AnomalyInformation anomalyInformation = new AnomalyInformation();

        AnomalyInformation pastAnomalyInformation = getAnomalyInformationValueState().value();
        AISSignal pastAISSignal = getAisSignalValueState().value();

        if (pastAnomalyInformation != null && pastAISSignal != null) {

            double time = value.getTimestamp().difference(pastAISSignal.getTimestamp());
            if (time > 10 && harvesineDistance(value.getLatitude(), value.getLongitude(),
                    pastAISSignal.getLatitude(), pastAISSignal.getLongitude()) > time / 60 * 6) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            }

            anomalyInformation.setShipHash(value.getShipHash());
            anomalyInformation.setCorrespondingTimestamp(value.getTimestamp());

            if (getLastDetectedAnomalyTime().value() != null
                && value.getTimestamp().difference(getLastDetectedAnomalyTime().value()) <= 30) {
                anomalyInformation.setScore(33.0f);
                anomalyInformation.setExplanation("The signal timing is anomalous.");
            } else {
                anomalyInformation.setScore(0.0f);
                anomalyInformation.setExplanation("The signal timing is great.");
            }
        } else {
            anomalyInformation.setScore(0.0f);
            anomalyInformation.setExplanation("The signal timing is great.");
            anomalyInformation.setShipHash(value.getShipHash());
            anomalyInformation.setCorrespondingTimestamp(value.getTimestamp());
        }
        getAnomalyInformationValueState().update(anomalyInformation);
        getAisSignalValueState().update(value);

        return anomalyInformation;
    }
}
