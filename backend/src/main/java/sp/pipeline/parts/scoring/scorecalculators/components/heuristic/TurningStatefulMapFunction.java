package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.circularMetric;
import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.getCorrectedHeading;

import sp.model.AISSignal;
import java.text.DecimalFormat;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final float HEADING_DIFFERENCE_THRESHOLD = 40;
    private static final float COURSE_DIFFERENCE_THRESHOLD = 40;

    /**
     * Checks if the current signal is an anomaly.
     * Ship is considered an anomaly when at least one of the following is true:
     * - heading value between the signals changed too much
     * - course value between the signals changed too much
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

        // Check heading difference
        if (circularMetric(getCorrectedHeading(pastSignal), getCorrectedHeading(currentSignal)) > HEADING_DIFFERENCE_THRESHOLD) {
            isAnomaly = true;
            explanation += "Heading difference between two consecutive signals is too large: "
                    + df.format(circularMetric(pastSignal.getHeading(), currentSignal.getHeading()))
                    + " degrees is more than threshold of " + df.format(HEADING_DIFFERENCE_THRESHOLD)
                    + " degrees"
                    + explanationEnding();
        }

        // Check course difference
        if (circularMetric(pastSignal.getCourse(), currentSignal.getCourse()) > COURSE_DIFFERENCE_THRESHOLD) {
            isAnomaly = true;
            explanation += "Course difference between two consecutive signals is too large: "
                    + df.format(circularMetric(pastSignal.getCourse(), currentSignal.getCourse()))
                    + " degrees is more than threshold of " + df.format(COURSE_DIFFERENCE_THRESHOLD)
                    + " degrees"
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

}
