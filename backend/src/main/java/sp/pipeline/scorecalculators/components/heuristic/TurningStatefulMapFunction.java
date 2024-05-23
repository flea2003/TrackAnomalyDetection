package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.circularMetric;

import sp.model.AISSignal;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    private final static float HEADING_DIFFERENCE_THRESHOLD = 40;
    private final static float COURSE_DIFFERENCE_THRESHOLD = 40;

    @Override
    public boolean isAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        currentSignal.updateHeading(); // in case no heading was reported

        return (headingDiffTooBig(pastSignal, currentSignal)
                || courseDiffTooBig(pastSignal, currentSignal));
    }

    private boolean headingDiffTooBig(AISSignal pastSignal, AISSignal currentSignal) {
        return circularMetric(pastSignal.getHeading(), currentSignal.getHeading()) > HEADING_DIFFERENCE_THRESHOLD;
    }

    private boolean courseDiffTooBig(AISSignal pastSignal, AISSignal currentSignal) {
        return circularMetric(pastSignal.getCourse(), currentSignal.getCourse()) > COURSE_DIFFERENCE_THRESHOLD;
    }

    @Override
    public float getAnomalyScore() {
        return 34f;
    }

    @Override
    public String getAnomalyExplanation(AISSignal currentSignal, AISSignal pastSignal) {
        String result = "";

        if (headingDiffTooBig(pastSignal, currentSignal)) {
            result += "Heading changed too much: " + df.format(circularMetric(pastSignal.getHeading(), currentSignal.getHeading()))
                    + " is more than threshold " + df.format(HEADING_DIFFERENCE_THRESHOLD)
                    + explanationEnding();
        }

        if (courseDiffTooBig(pastSignal, currentSignal)) {
            result += "Course changed too much: " + df.format(circularMetric(pastSignal.getCourse(), currentSignal.getCourse()))
                    + " is more than threshold " + df.format(COURSE_DIFFERENCE_THRESHOLD)
                    + explanationEnding();
        }

        return result;
    }

    @Override
    public String getNonAnomalyExplanation() {
        return "The ship's turning direction is ok" + explanationEnding();
    }
}
