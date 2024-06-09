package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.getDistanceTravelled;
import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.timeDiffInMinutes;

import sp.model.AISSignal;
import java.text.DecimalFormat;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final double SPEED_THRESHOLD = 30;
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
    protected AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        // only check if there was a signal in the past
        if (pastSignal == null) {
            return new AnomalyScoreWithExplanation(false, 0f, "");
        }

        String explanation = "";
        boolean isAnomaly = false;

        DecimalFormat df = getDecimalFormatter();

        // Compute and check speed between the signals
        if (currentSignal.getSpeed() > SPEED_THRESHOLD) {
            isAnomaly = true;
            explanation += "Speed is too big: " + df.format(currentSignal.getSpeed())
                    + " km/h is faster than threshold of " + df.format(SPEED_THRESHOLD)
                    + " km/h" + explanationEnding();
        }

        // Check the difference between the computed speed and the reported speed
        if (reportedSpeedDifference(currentSignal, pastSignal) > REPORTED_SPEED_ACCURACY_MARGIN) {
            isAnomaly = true;
            explanation += "Speed is inaccurate: the approximated speed of " + df.format(computeSpeed(currentSignal, pastSignal))
                    + " km/h is different from reported speed of " + df.format(currentSignal.getSpeed())
                    + " km/h by more than allowed margin of " + df.format(REPORTED_SPEED_ACCURACY_MARGIN)
                    + " km/min" + explanationEnding();
        }

        // Compute and check acceleration between two signals
        if (computedAcceleration(currentSignal, pastSignal) > ACCELERATION_THRESHOLD) {
            isAnomaly = true;
            explanation += "Acceleration is too big: " + df.format(computedAcceleration(currentSignal, pastSignal))
                    + " km/min^2 is bigger than threshold of " + df.format(ACCELERATION_THRESHOLD)
                    + " km/min^2" + explanationEnding();
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
    protected float getAnomalyScore() {
        return 25f;
    }
}
