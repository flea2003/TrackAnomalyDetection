package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.harvesineDistance;

import java.time.Duration;
import sp.model.AISSignal;

public class SignalStatefulMapFunction extends HeuristicStatefulMapFunction {

    @Override
    public boolean isAnomaly(AISSignal currentSignal, AISSignal pastSignal) {
        double time = Duration.between(pastSignal.getTimestamp(), currentSignal.getTimestamp()).toMinutes();

        boolean signalTimingIsGood = time < 10;
        boolean shipDidntTravelTooMuch = harvesineDistance(currentSignal.getLatitude(), currentSignal.getLongitude(),
                pastSignal.getLatitude(), pastSignal.getLongitude()) / (time / 60) < 6;

        return (!signalTimingIsGood && !shipDidntTravelTooMuch);
    }

    @Override
    public float getAnomalyScore() {
        return 33f;
    }

    @Override
    public String getAnomalyExplanation(AISSignal currentSignal, AISSignal pastSignal) {
        return "The time difference between consecutive AIS signals is anomalous.";
    }

    @Override
    public String getNonAnomalyExplanation() {
        return "The time difference between consecutive AIS signals is ok.";
    }
}
