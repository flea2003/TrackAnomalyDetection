package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.harvesineDistance;

import java.time.Duration;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final String goodMsg = "The ship's speed is great.";
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
            double time = Duration.between(getAisSignalValueState().value().getTimestamp(), value.getTimestamp()).toMinutes();
            double computedSpeed = globeDistance / (time + 0.00001);
            // If any of these heuristics hold, we update our lastDetectedAnomalyTime:
            // 1. Check if the computed speed is greater than 40 km/h
            // 2. Check if the absolute difference between the computed and the reported speed is greater than 10 km/h
            // 3. Check if the acceleration of the ship is greater than 50 km/h
            if (computedSpeed > 40
                || Math.abs(value.getSpeed() - computedSpeed) > 10
                || (getAisSignalValueState().value().getSpeed() - value.getSpeed()) / (time + 0.00001) > 50) {
                this.getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
        }
        return super.setAnomalyInformationResult(anomalyInformation, value, 33f, badMsg, goodMsg);
    }

}
