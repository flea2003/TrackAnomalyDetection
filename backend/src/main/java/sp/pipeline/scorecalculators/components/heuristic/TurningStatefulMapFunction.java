package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.circularMetric;

import sp.model.AISSignal;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    @Override
    public boolean isAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        currentSignal.updateHeading(); // in case no heading was reported

        boolean headingDifferenceIsGood = circularMetric(pastSignal.getHeading(), currentSignal.getHeading()) < 40;
        boolean courseDifferenceIsGood = circularMetric(pastSignal.getCourse(), currentSignal.getCourse()) < 40;

        return (!headingDifferenceIsGood || !courseDifferenceIsGood);
    }

    @Override
    public float getAnomalyScore() {
        return 34f;
    }

    @Override
    public String getAnomalyExplanation(AISSignal currentSignal, AISSignal pastSignal) {
        return "The ship's turning direction is anomalous.";
    }

    @Override
    public String getNonAnomalyExplanation() {
        return "The ship's turning direction is ok.";
    }
}
