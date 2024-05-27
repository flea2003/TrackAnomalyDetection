package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.circularMetric;

import sp.model.AISSignal;
import java.text.DecimalFormat;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final float HEADING_DIFFERENCE_THRESHOLD = 40;
    private static final float COURSE_DIFFERENCE_THRESHOLD = 40;
    private static final float NO_HEADING = 511;

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
        boolean isAnomaly = false;
        String explanation = "";

        DecimalFormat df = getDecimalFormatter();

        // Check heading difference
        if (circularMetric(getCorrectedHeading(pastSignal), getCorrectedHeading(currentSignal)) > HEADING_DIFFERENCE_THRESHOLD) {
            isAnomaly = true;
            explanation += "Heading difference between consecutive signals: "
                    + df.format(circularMetric(pastSignal.getHeading(), currentSignal.getHeading()))
                    + " degrees is more than threshold of " + df.format(HEADING_DIFFERENCE_THRESHOLD)
                    + " degrees"
                    + explanationEnding();
        }

        // Check course difference
        if (circularMetric(pastSignal.getCourse(), currentSignal.getCourse()) > COURSE_DIFFERENCE_THRESHOLD) {
            isAnomaly = true;
            explanation += "Course difference between consecutive signals: "
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
        return 34f;
    }

    /**
     * Gets the heading of the signal. If the signal has not reported any heading
     * (which is denoted by 511), then the course is returned instead.
     *
     * @param signal the AIS signal
     * @return the heading if exists, or the course otherwise
     */
    private float getCorrectedHeading(AISSignal signal) {
        if (signal.getHeading() == NO_HEADING) {
            return signal.getCourse();
        }

        return signal.getHeading();
    }
}
