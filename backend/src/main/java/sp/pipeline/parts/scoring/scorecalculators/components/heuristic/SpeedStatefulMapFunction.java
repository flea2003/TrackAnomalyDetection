package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import java.time.Duration;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.harvesineDistance;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final String goodMsg = "The ship's speed is ok.";
    private static final String badMsg = "The ship's speed is anomalous.";

    /**
     * Performs a stateful map operation that receives an AIS signal and produces an
     * AnomalyInformation based on the predefined heuristics for the speed of the ship.
     *
     * @param value The input value.
     * @return the computed Anomaly Information object
     * @throws Exception exception from value state descriptors
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
        return isAnomaly(
                computeSpeed(value, pastAISSignal),
                getReportedSpeedDifference(value, pastAISSignal),
                calculateAcceleration(value, pastAISSignal)
        );
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

    /**
     * Computes speed by taking the distance travelled between two AIS signals.
     *
     * @param value the current AIS signal
     * @param pastAISSignal the past AIS signal
     * @return the speed computed based on these two signals
     */
    public double computeSpeed(AISSignal value, AISSignal pastAISSignal) {
        double globeDistance = harvesineDistance(
                value.getLatitude(), value.getLongitude(),
                pastAISSignal.getLatitude(), pastAISSignal.getLongitude()
        );
        double time = getTimeDifference(value, pastAISSignal);

        return globeDistance / (time + 0.00001);
    }

    /**
     * Calculates the absolute difference between the reported speed in the AIS signal and the
     * calculated speed (using the previous signal).
     *
     * @param value the current AIS signal
     * @param pastValue the previous AIS signal
     * @return the difference between reported speed and the calculated speed
     */
    public double getReportedSpeedDifference(AISSignal value, AISSignal pastValue) {
        return Math.abs(value.getSpeed() - computeSpeed(pastValue, value));
    }

    /**
     * Calculate the time difference between two AIS signals.
     *
     * @param value the current AIS signal
     * @param pastAISSignal the previous AIS signal
     * @return the difference between two signals
     */
    public double getTimeDifference(AISSignal value, AISSignal pastAISSignal) {
        return Duration.between(pastAISSignal.getTimestamp(), value.getTimestamp()).toMinutes();
    }

    /**
     * Calculates acceleration based on the data in two consecutive AIS signals.
     *
     * @param value the current AIS signal
     * @param pastAISSignal the previous AIS signal
     * @return the computed acceleration
     */
    public double calculateAcceleration(AISSignal value, AISSignal pastAISSignal) {
        double time = getTimeDifference(value, pastAISSignal);
        return (value.getSpeed() - pastAISSignal.getSpeed()) / (time + 0.00001);
    }
}
