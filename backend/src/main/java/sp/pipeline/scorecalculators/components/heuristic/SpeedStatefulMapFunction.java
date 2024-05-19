package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.harvesineDistance;

import java.time.Duration;
import sp.dtos.AnomalyInformation;
import sp.model.AISSignal;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final String goodMsg = "The ship's speed is ok.";
    private static final String badMsg = "The ship's speed is anomalous.";

    /**
     * Performs a stateful map operation that receives an AIS signal and produces an
     * AnomalyInformation based on the predefined heuristics for the speed of the ship.
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

        // In the case that our stateful map has encountered signals in the past
        if (pastAnomalyInformation != null && pastAISSignal != null) {
            double globeDistance = harvesineDistance(value.getLatitude(), value.getLongitude(),
                pastAISSignal.getLatitude(), pastAISSignal.getLongitude());
            double time = Duration.between(pastAISSignal.getTimestamp(), value.getTimestamp()).toMinutes();
            double computedSpeed = globeDistance / (time + 0.00001);

            boolean speedIsLow = computedSpeed <= 55.5;
            boolean reportedSpeedIsAccurate = Math.abs(value.getSpeed() - computedSpeed) <= 10;
            boolean accelerationIsLow = (getAisSignalValueState().value().getSpeed() - value.getSpeed()) / (time + 0.00001) < 50;

            if (!speedIsLow || !reportedSpeedIsAccurate || !accelerationIsLow) {
                this.getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
        }
        return super.setAnomalyInformationResult(anomalyInformation, value, 33f, badMsg, goodMsg);
    }

}
