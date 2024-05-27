package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.circularMetric;

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
    AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        // currentSignal.updateHeading(); // in case no heading was reported

        boolean isAnomaly = false;
        String explanation = "";

        DecimalFormat df = getDecimalFormatter();

        if (headingDiffTooBig(pastSignal, currentSignal)) {
            isAnomaly = true;
            explanation += "Heading changed too much: "
                    + df.format(circularMetric(pastSignal.getHeading(), currentSignal.getHeading()))
                    + " is more than threshold " + df.format(HEADING_DIFFERENCE_THRESHOLD)
                    + explanationEnding();
        }

        if (courseDiffTooBig(pastSignal, currentSignal)) {
            isAnomaly = true;
            explanation += "Course changed too much: "
                    + df.format(circularMetric(pastSignal.getCourse(), currentSignal.getCourse()))
                    + " is more than threshold " + df.format(COURSE_DIFFERENCE_THRESHOLD)
                    + explanationEnding();
        }

        return new AnomalyScoreWithExplanation(isAnomaly, getAnomalyScore(), explanation);

    }

    /**
     * Checks if the change in the heading variable is bigger than the threshold.
     *
     * @param pastSignal the past AIS signal
     * @param currentSignal the current AIS signal
     * @return true if the heading difference is too big, false otherwise
     */
    private boolean headingDiffTooBig(AISSignal pastSignal, AISSignal currentSignal) {
        return circularMetric(pastSignal.getHeading(), currentSignal.getHeading()) > HEADING_DIFFERENCE_THRESHOLD;
    }

    /**
     * Checks if the change in the course variable is bigger than the threshold.
     *
     * @param pastSignal the past AIS signal
     * @param currentSignal the current AIS signal
     * @return true if the course difference is too big, false otherwise
     */
    private boolean courseDiffTooBig(AISSignal pastSignal, AISSignal currentSignal) {
        return circularMetric(pastSignal.getCourse(), currentSignal.getCourse()) > COURSE_DIFFERENCE_THRESHOLD;
    }

    /**
     * Gets the anomaly score of the heuristic. This score is given to the ship that
     * is considered an anomaly based on the heuristic.
     *
     * @return the anomaly score of the heuristic
     */
    @Override
    float getAnomalyScore() {
        return 34f;
    }

    /**
     * Explanation string for the heuristic which is used when the ship is non-anomalous.
     *
     * @return explanation string
     */
    @Override
    String getNonAnomalyExplanation() {
        return "The ship's turning direction is ok" + explanationEnding();
    }
}
