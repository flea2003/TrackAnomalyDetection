package sp.pipeline.scorecalculators.components.heuristic;

import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        if (value.getHeading() == 511) {
            value.setHeading(value.getCourse());
        }
        AnomalyInformation anomalyInformation = new AnomalyInformation();
        AnomalyInformation pastAnomalyInformation = getAnomalyInformationValueState().value();
        AISSignal pastAISSignal = getAisSignalValueState().value();

        if (pastAnomalyInformation != null && pastAISSignal != null) {
            double headingDifference = 180.0
                - Math.abs(180 - (pastAISSignal.getHeading() % 360 - value.getHeading() % 360 + 360) % 360);
            double corseDifference = 180.0
                - Math.abs(180 - (pastAISSignal.getCourse() % 360 - value.getCourse() % 360 + 360) % 360);

            if (headingDifference >= 40 || corseDifference >= 40) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
            anomalyInformation.setShipHash(value.getShipHash());
            anomalyInformation.setCorrespondingTimestamp(value.getTimestamp());

            if (getLastDetectedAnomalyTime().value() != null && value.getTimestamp()
                .difference(getLastDetectedAnomalyTime().value()) <= 30) {
                anomalyInformation.setScore(34.0f);
                anomalyInformation.setExplanation("The ship's turning direction is anomalous.");
            } else {
                anomalyInformation.setScore(0.0f);
                anomalyInformation.setExplanation("The ship's turning direction is great.");
            }
        } else {
            anomalyInformation.setScore(0.0f);
            anomalyInformation.setExplanation("The ship's turning direction is great.");
            anomalyInformation.setShipHash(value.getShipHash());
            anomalyInformation.setCorrespondingTimestamp(value.getTimestamp());
        }
        getAnomalyInformationValueState().update(anomalyInformation);
        getAisSignalValueState().update(value);

        return anomalyInformation;
    }
}
