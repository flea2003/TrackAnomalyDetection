package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.harvesineDistance;

import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import java.time.Duration;

public class SignalStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final String goodMsg = "The time difference between consecutive AIS signals is ok.";
    private static final String badMsg = "The time difference between consecutive AIS signals is anomalous.";

    /**
     * Performs a stateful map operation that receives an AIS signal and produces an
     * AnomalyInformation based on the predefined heuristics for the interval between
     * two consecutive signals.
     *
     * @param value The input value.
     * @return the computed Anomaly Information object
     * @throws Exception exception from value state descriptors
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        AISSignal pastAISSignal = getAisSignalValueState().value();

        // In the case that our stateful map has encountered signals in the past
        if (pastAISSignal != null) {
            if (isAnomaly(value, pastAISSignal)) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
        }
        return super.setAnomalyInformationResult(value, 33f, badMsg, goodMsg);
    }

    /**
     * Checks if the current AISSignal is considered an anomaly. Takes the past signal to compare with.
     * The signal is considered an anomaly if the time between the signals is significant or the distance
     * between signals is significant.
     *
     * @param value current AIS signal
     * @param pastAISSignal previous AIS signal
     * @return true if the current signal is anomalous, false otherwise
     */
    public boolean isAnomaly(AISSignal value, AISSignal pastAISSignal) {
        double time = Duration.between(pastAISSignal.getTimestamp(), value.getTimestamp()).toMinutes();

        boolean signalTimingIsGood = time < 10;
        boolean shipDidntTravelTooMuch = harvesineDistance(value.getLatitude(), value.getLongitude(),
                pastAISSignal.getLatitude(), pastAISSignal.getLongitude()) / (time / 60) < 6;

        return !signalTimingIsGood && !shipDidntTravelTooMuch;
    }
}
