package sp.pipeline.scorecalculators.components.heuristic;

import sp.model.AISSignal;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.*;

public class SignalStatefulMapFunction extends HeuristicStatefulMapFunction {

    private final static long SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES = 10;
    private final static double TRAVELLED_DISTANCE_THRESHOLD = 6;

    @Override
    public boolean isAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        return (signalsNotFrequent(currentSignal, pastSignal) && shipTravelledMuch(currentSignal, pastSignal));
    }

    @Override
    public float getAnomalyScore() {
        return 33f;
    }

    @Override
    public String getAnomalyExplanation(AISSignal currentSignal, AISSignal pastSignal) {
        String result = "";

        if (signalsNotFrequent(currentSignal, pastSignal)) {
            result += "Time between two signals is too large: " + df.format(timeDiffInMinutes(currentSignal, pastSignal))
                    + " minutes is more than threshold " + SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES + " minutes"
                    + explanationEnding();
        }

        if (shipTravelledMuch(currentSignal, pastSignal)) {
            result += "Ship travelled too much between signals: " + df.format(distanceDividedByHours(currentSignal, pastSignal))
                    + " is more than threshold " + TRAVELLED_DISTANCE_THRESHOLD
                    + explanationEnding();
        }

        return result;
    }

    @Override
    public String getNonAnomalyExplanation() {
        return "The time difference between consecutive AIS signals is ok" + explanationEnding();
    }

    private boolean signalsNotFrequent(AISSignal currentSignal, AISSignal pastSignal) {
        return timeDiffInMinutes(currentSignal, pastSignal) > SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES;
    }

    private boolean shipTravelledMuch(AISSignal currentSignal, AISSignal pastSignal) {
        return distanceDividedByHours(currentSignal, pastSignal) > TRAVELLED_DISTANCE_THRESHOLD;
    }

    private double distanceDividedByHours(AISSignal currentSignal, AISSignal pastSignal) {
        return getDistanceTravelled(currentSignal, pastSignal) / timeDiffInHours(currentSignal, pastSignal);
    }
}
