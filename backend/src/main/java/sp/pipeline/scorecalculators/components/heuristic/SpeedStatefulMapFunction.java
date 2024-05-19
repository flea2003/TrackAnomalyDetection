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
        AISSignal pastAISSignal = getAisSignalValueState().value();

        // In the case that our stateful map has encountered signals in the past
        if (pastAISSignal != null && isAnomaly(value, pastAISSignal)) {
            this.getLastDetectedAnomalyTime().update(value.getTimestamp());
        }

        return super.setAnomalyInformationResult(value, 33f, badMsg, goodMsg);
    }

    /**
     * Checks if the current value is anomaly based on heuristics (current speed, reported
     * speed difference and the acceleration).
     *
     * @param value current AIS signal
     * @param pastAISSignal past AIS signal
     * @return true if the current AIS signal is considered an anomaly based on speed
     *     heuristics, and false otherwise.
     */
    public boolean isAnomaly(AISSignal value, AISSignal pastAISSignal) {
        double globeDistance = harvesineDistance(value.getLatitude(), value.getLongitude(),
                pastAISSignal.getLatitude(), pastAISSignal.getLongitude());
        double time = Duration.between(pastAISSignal.getTimestamp(), value.getTimestamp()).toMinutes();

        double computedSpeed = globeDistance / (time + 0.00001);
        double reportedSpeedDifference = Math.abs(value.getSpeed() - computedSpeed);
        double computedAcceleration = (value.getSpeed() - pastAISSignal.getSpeed()) / (time + 0.00001);

        return isAnomaly(computedSpeed, reportedSpeedDifference, computedAcceleration);
    }

    /**
     * Checks if the current value is anomaly based on heuristics (current speed, reported
     * speed difference and the acceleration).
     *
     * @param computedSpeed computed speed based on the past AIS signal
     * @param reportedSpeedDifference the difference between computed speed and reported speed
     * @param computedAcceleration computed acceleration based on the past AIS signal
     * @return true if the current AIS signal is considered an anomaly based on speed
     *     heuristics, and false otherwise.
     */
    public boolean isAnomaly(double computedSpeed, double reportedSpeedDifference, double computedAcceleration) {
        boolean speedIsLow = (computedSpeed <= 55.5);
        boolean reportedSpeedIsAccurate = (reportedSpeedDifference <= 10);
        boolean accelerationIsLow = (computedAcceleration < 50);

        return !speedIsLow || !reportedSpeedIsAccurate || !accelerationIsLow;
    }
}
