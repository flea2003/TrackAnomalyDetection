package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.getDistanceTravelled;
import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.timeDiffInMinutes;

import sp.model.AISSignal;
import java.text.DecimalFormat;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final double SPEED_THRESHOLD = 55.5;
    private static final double ACCELERATION_THRESHOLD = 50;
    private static final double REPORTED_SPEED_ACCURACY_MARGIN = 10;

    /**
     * Checks if the current signal is an anomaly.
     * The current signal is considered an anomaly if at least one of the following is true:
     * - ship is going too fast
     * - the reported speed is too different from the calculated one
     * - ship is accelerating too fast
     *
     * @param currentSignal current AIS signal
     * @param pastSignal past AIS signal (non-null object)
     * @return AnomalyScoreWithExplanation object which indicates whether the current
     *     signal is an anomaly. If it is an anomaly, an explanation string and anomaly score
     *     are also included in the same return object.
     */
    AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        String explanation = "";
        boolean isAnomaly = false;

        DecimalFormat df = getDecimalFormatter();

        if (speedIsTooFast(currentSignal, pastSignal)) {
            isAnomaly = true;
            explanation += "Too fast: " + df.format(computeSpeed(currentSignal, pastSignal))
                    + " is faster than threshold " + df.format(SPEED_THRESHOLD)
                    + explanationEnding();
        }

        if (reportedSpeedIsNotAccurate(currentSignal, pastSignal)) {
            isAnomaly = true;
            explanation += "Speed is inaccurate: " + df.format(computeSpeed(currentSignal, pastSignal))
                    + " is different from reported speed of " + df.format(currentSignal.getSpeed())
                    + " by more than allowed margin " + df.format(REPORTED_SPEED_ACCURACY_MARGIN)
                    + explanationEnding();
        }

        if (accelerationTooBig(currentSignal, pastSignal)) {
            isAnomaly = true;
            explanation += "Acceleration too big: " + df.format(computedAcceleration(currentSignal, pastSignal))
                    + " is bigger than threshold " + df.format(ACCELERATION_THRESHOLD)
                    + explanationEnding();
        }

        return new AnomalyScoreWithExplanation(isAnomaly, getAnomalyScore(), explanation);
    }

    /**
     * Compute speed based on the data of this and the past signals.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return the computed speed
     */
    private double computeSpeed(AISSignal currentSignal, AISSignal pastSignal) {
        double time = (double) timeDiffInMinutes(currentSignal, pastSignal);
        return getDistanceTravelled(currentSignal, pastSignal) / (time + 0.00001);
    }

    /**
     * Calculate the difference between the reported speed in the distance and the calculated
     * distance based on the two signals (the current one and the past one).
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return the calculated difference
     */
    private double reportedSpeedDifference(AISSignal currentSignal, AISSignal pastSignal) {
        return Math.abs(currentSignal.getSpeed() - computeSpeed(currentSignal, pastSignal));
    }

    /**
     * Compute the acceleration based on the current and the past signals.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return the computed acceleration
     */
    private double computedAcceleration(AISSignal currentSignal, AISSignal pastSignal) {
        double speedDiff = currentSignal.getSpeed() - pastSignal.getSpeed();
        return speedDiff / (timeDiffInMinutes(currentSignal, pastSignal) + 0.00001);
    }

    /**
     * Gets the anomaly score of the heuristic. This score is given to the ship that
     * is considered an anomaly based on the heuristic.
     *
     * @return the anomaly score of the heuristic
     */
    @Override
    float getAnomalyScore() {
        return 33f;
    }

    /**
     * Explanation string for the heuristic which is used when the ship is non-anomalous.
     *
     * @return explanation string
     */
    @Override
    String getNonAnomalyExplanation() {
        return "The ship's speed is ok" + explanationEnding();
    }

    /**
     * Checks if the speed is more than the threshold.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return true if the speed is too big, false otherwise
     */
    private boolean speedIsTooFast(AISSignal currentSignal, AISSignal pastSignal) {
        return computeSpeed(currentSignal, pastSignal) > SPEED_THRESHOLD;
    }

    /**
     * Checks if the reported speed and the calculated speed difference is bigger than the
     * threshold.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return true if the difference is too big, false otherwise
     */
    private boolean reportedSpeedIsNotAccurate(AISSignal currentSignal, AISSignal pastSignal) {
        return reportedSpeedDifference(currentSignal, pastSignal) > REPORTED_SPEED_ACCURACY_MARGIN;
    }

    /**
     * Checks if the acceleration is bigger than the threshold.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return true if the acceleration is too big, false otherwise
     */
    private boolean accelerationTooBig(AISSignal currentSignal, AISSignal pastSignal) {
        return computedAcceleration(currentSignal, pastSignal) > ACCELERATION_THRESHOLD;
    }
}
