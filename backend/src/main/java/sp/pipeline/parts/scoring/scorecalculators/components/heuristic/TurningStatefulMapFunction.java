package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.circularMetric;

import sp.model.AISSignal;
import java.text.DecimalFormat;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final float HEADING_DIFFERENCE_THRESHOLD = 40;
    private static final float COURSE_DIFFERENCE_THRESHOLD = 40;
    private static final float NO_HEADING = 511f;

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
        StringBuilder explanationBuilder = new StringBuilder();
        DecimalFormat df = getDecimalFormatter();

        isAnomaly |= checkForHeadingToBePresent(currentSignal.getHeading(), explanationBuilder, df);

        // Only proceed to the next checks if there is a past signal
        if (pastSignal == null) {
            return new AnomalyScoreWithExplanation(isAnomaly, getAnomalyScore(), explanationBuilder.toString());
        }

        isAnomaly |= checkForHeadingDifference(currentSignal, pastSignal, explanationBuilder, df);
        isAnomaly |= checkForCourseDifference(currentSignal, pastSignal, explanationBuilder, df);

        return new AnomalyScoreWithExplanation(isAnomaly, getAnomalyScore(), explanationBuilder.toString());

    }

    /**
     * Checks if the given heading is present. The heading is considered to be present
     * when its value is not equal to 511.
     * Modifies the explanationBuilder if needed.
     *
     * @param heading the heading given in the AIS signal
     * @param explanationBuilder the StringBuilder for explanation string
     * @param df DecimalFormat to use for floating-point numbers
     * @return true if heading is not present, false otherwise
     */
    private boolean checkForHeadingToBePresent(float heading, StringBuilder explanationBuilder, DecimalFormat df) {
        // Inform if the heading was not sent (code 511)
        if (heading == NO_HEADING) {
            explanationBuilder
                    .append("Heading was not given (value ")
                    .append(df.format(NO_HEADING))
                    .append(")")
                    .append(explanationEnding());

            return true; // anomaly
        }

        return false; // not anomaly
    }

    /**
     * Checks if the heading difference between two consecutive signals is too large.
     * If any of the headings is not given, the check is skipped.
     * Modifies the explanationBuilder if needed.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @param explanationBuilder the StringBuilder for explanation string
     * @param df DecimalFormat to use for floating-point numbers
     * @return true if headings are present and the difference is too large,
     *     false otherwise
     */
    private boolean checkForHeadingDifference(AISSignal currentSignal, AISSignal pastSignal,
                                              StringBuilder explanationBuilder, DecimalFormat df) {

        float currentHeading = currentSignal.getHeading();
        float pastHeading = pastSignal.getHeading();

        // The heading difference will only be checked if both headings were sent
        if (pastHeading == NO_HEADING || currentHeading == NO_HEADING) {
            return false; // not anomaly
        }

        if (circularMetric(pastHeading, currentHeading) <= HEADING_DIFFERENCE_THRESHOLD) {
            return false; // not anomaly
        }

        explanationBuilder
                .append("Heading difference between two consecutive signals is too large: ")
                .append(df.format(circularMetric(pastSignal.getHeading(), currentSignal.getHeading())))
                .append(" degrees is more than threshold of ")
                .append(df.format(HEADING_DIFFERENCE_THRESHOLD))
                .append(" degrees")
                .append(explanationEnding());

        return true; // anomaly
    }

    /**
     * Checks if the course difference between two consecutive signals is too large.
     * Modifies the explanationBuilder if needed.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @param explanationBuilder the StringBuilder for explanation string
     * @param df DecimalFormat to use for floating-point numbers
     * @return true if course difference is too large
     */
    private boolean checkForCourseDifference(AISSignal currentSignal, AISSignal pastSignal,
                                             StringBuilder explanationBuilder, DecimalFormat df) {

        if (circularMetric(pastSignal.getCourse(), currentSignal.getCourse()) > COURSE_DIFFERENCE_THRESHOLD) {
            explanationBuilder
                    .append("Course difference between two consecutive signals is too large: ")
                    .append(df.format(circularMetric(pastSignal.getCourse(), currentSignal.getCourse())))
                    .append(" degrees is more than threshold of ")
                    .append(df.format(COURSE_DIFFERENCE_THRESHOLD))
                    .append(" degrees")
                    .append(explanationEnding());

            return true; // anomaly
        }

        return false; // not anomaly
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
