package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.getDistanceTravelled;
import static sp.pipeline.scorecalculators.components.heuristic.Tools.timeDiffInHours;
import static sp.pipeline.scorecalculators.components.heuristic.Tools.timeDiffInMinutes;

import sp.model.AISSignal;
import java.text.DecimalFormat;


public class SignalStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final long SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES = 10;
    private static final double TRAVELLED_DISTANCE_THRESHOLD = 6;

    /**
     * Checks if the current signal is an anomaly.
     * Ship is considered an anomaly when both of the following are true:
     * - the time between two signals is too large
     * - the ship travelled a big distance between two signals
     *
     * @param currentSignal current AIS signal
     * @param pastSignal past AIS signal (non-null object)
     * @return AnomalyScoreWithExplanation object which indicates whether the current
     *     signal is an anomaly. If it is an anomaly, an explanation string and anomaly score
     *     are also included in the same return object.
     */
    @Override
    AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        boolean isAnomaly = false;
        String explanation = "";

        DecimalFormat df = getDecimalFormatter();

        if (signalsNotFrequent(currentSignal, pastSignal) && shipTravelledMuch(currentSignal, pastSignal)) {
            isAnomaly = true;

            explanation += "Time between two signals is too large: " + df.format(timeDiffInMinutes(currentSignal, pastSignal))
                    + " minutes is more than threshold " + SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES + " minutes, "
                    + " and ship travelled too much between signals: "
                    + df.format(distanceDividedByHours(currentSignal, pastSignal))
                    + " is more than threshold " + TRAVELLED_DISTANCE_THRESHOLD
                    + explanationEnding();
        }

        return new AnomalyScoreWithExplanation(isAnomaly, getAnomalyScore(), explanation);
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
        return "The time difference between consecutive AIS signals is ok" + explanationEnding();
    }

    /**
     * Checks if signals were sent NOT frequently. The comparison is done with the threshold
     * value SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return true if the time between the signals is too big, false otherwise
     */
    private boolean signalsNotFrequent(AISSignal currentSignal, AISSignal pastSignal) {
        return timeDiffInMinutes(currentSignal, pastSignal) > SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES;
    }

    /**
     * Checks if ship did not travel more than the threshold value.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return true if the ship travelled more than the threshold, false otherwise
     */
    private boolean shipTravelledMuch(AISSignal currentSignal, AISSignal pastSignal) {
        return distanceDividedByHours(currentSignal, pastSignal) > TRAVELLED_DISTANCE_THRESHOLD;
    }

    /**
     * Calculates the distance divided by hours. Used for method `shipTravelledMuch`.
     *
     * @param currentSignal the current signal
     * @param pastSignal the past signal
     * @return the calculated distance
     */
    private double distanceDividedByHours(AISSignal currentSignal, AISSignal pastSignal) {
        return getDistanceTravelled(currentSignal, pastSignal) / timeDiffInHours(currentSignal, pastSignal);
    }
}
