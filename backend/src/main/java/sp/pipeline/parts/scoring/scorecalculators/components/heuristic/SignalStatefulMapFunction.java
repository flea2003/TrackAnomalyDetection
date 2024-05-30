package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.getDistanceTravelled;
import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.timeDiffInHours;
import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.timeDiffInMinutes;

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
    protected AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        // only check if there was a signal in the past
        if (pastSignal == null) {
            return new AnomalyScoreWithExplanation(false, 0f, "");
        }

        boolean isAnomaly = false;
        String explanation = "";

        DecimalFormat df = getDecimalFormatter();

        boolean signalsNotFrequent = timeDiffInMinutes(currentSignal, pastSignal) > SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES;
        boolean shipTravelledMuch = distanceDividedByHours(currentSignal, pastSignal) > TRAVELLED_DISTANCE_THRESHOLD;

        if (signalsNotFrequent && shipTravelledMuch) {
            isAnomaly = true;

            explanation += "Time between two consecutive signals is too large: "
                    + df.format(timeDiffInMinutes(currentSignal, pastSignal))
                    + " minutes is more than threshold of " + SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES + " minutes,"
                    + " and ship's speed (between two signals) is too large: "
                    + df.format(distanceDividedByHours(currentSignal, pastSignal))
                    + " km/h is more than threshold of " + TRAVELLED_DISTANCE_THRESHOLD + " km/h"
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
    protected float getAnomalyScore() {
        return 25f;
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
