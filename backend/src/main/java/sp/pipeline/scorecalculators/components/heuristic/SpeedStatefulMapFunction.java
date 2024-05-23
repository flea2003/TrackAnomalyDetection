package sp.pipeline.scorecalculators.components.heuristic;

import sp.model.AISSignal;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.*;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final double SPEED_THRESHOLD = 55.5;
    private static final double ACCELERATION_THRESHOLD = 50;
    private static final double REPORTED_SPEED_ACCURACY_MARGIN = 10;

    AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        String explanation = "";
        boolean isAnomaly = false;

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

    private double computeSpeed(AISSignal currentSignal, AISSignal pastSignal) {
        double time = (double) timeDiffInMinutes(currentSignal, pastSignal);
        return getDistanceTravelled(currentSignal, pastSignal) / (time + 0.00001);
    }

    private double reportedSpeedDifference(AISSignal currentSignal, AISSignal pastSignal) {
        return Math.abs(currentSignal.getSpeed() - computeSpeed(currentSignal, pastSignal));
    }

    private double computedAcceleration(AISSignal currentSignal, AISSignal pastSignal) {
        double speedDiff = currentSignal.getSpeed() - pastSignal.getSpeed();
        return speedDiff / (timeDiffInMinutes(currentSignal, pastSignal) + 0.00001);
    }

    @Override
    float getAnomalyScore() {
        return 33f;
    }

    @Override
    String getNonAnomalyExplanation() {
        return "The ship's speed is ok" + explanationEnding();
    }

    private boolean speedIsTooFast(AISSignal currentSignal, AISSignal pastSignal) {
        return computeSpeed(currentSignal, pastSignal) > SPEED_THRESHOLD;
    }

    private boolean reportedSpeedIsNotAccurate(AISSignal currentSignal, AISSignal pastSignal) {
        return reportedSpeedDifference(currentSignal, pastSignal) > REPORTED_SPEED_ACCURACY_MARGIN;
    }

    private boolean accelerationTooBig(AISSignal currentSignal, AISSignal pastSignal) {
        return computedAcceleration(currentSignal, pastSignal) > ACCELERATION_THRESHOLD;
    }
}
