package sp.pipeline.scorecalculators.components.heuristic;

import sp.model.AISSignal;

import java.text.DecimalFormat;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.*;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final double SPEED_THRESHOLD = 55.5;
    private static final double ACCELERATION_THRESHOLD = 50;
    private static final double REPORTED_SPEED_ACCURACY_MARGIN = 10;

    public boolean isAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        return speedIsTooFast(currentSignal, pastSignal)
                || reportedSpeedIsNotAccurate(currentSignal, pastSignal)
                || accelerationTooBig(currentSignal, pastSignal);
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
    public float getAnomalyScore() {
        return 33f;
    }

    @Override
    public String getAnomalyExplanation(AISSignal currentSignal, AISSignal pastSignal) {
        String result = "";

        if (speedIsTooFast(currentSignal, pastSignal)) {
            result += "Too fast: " + df.format(computeSpeed(currentSignal, pastSignal))
                    + " is faster than threshold " + df.format(SPEED_THRESHOLD)
                    + explanationEnding();
        }

        if (reportedSpeedIsNotAccurate(currentSignal, pastSignal)) {
            result += "Speed is inaccurate: " + df.format(computeSpeed(currentSignal, pastSignal))
                    + " is different from reported speed of " + df.format(currentSignal.getSpeed())
                    + " by more than allowed margin " + df.format(REPORTED_SPEED_ACCURACY_MARGIN)
                    + explanationEnding();
        }

        if (accelerationTooBig(currentSignal, pastSignal)) {
            result += "Acceleration too big: " + df.format(computedAcceleration(currentSignal, pastSignal))
                    + " is bigger than threshold " + df.format(ACCELERATION_THRESHOLD)
                    + explanationEnding();
        }

        return result;
    }

    @Override
    public String getNonAnomalyExplanation() {
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
