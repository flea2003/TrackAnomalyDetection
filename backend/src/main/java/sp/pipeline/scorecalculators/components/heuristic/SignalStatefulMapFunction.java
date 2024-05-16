package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.harvesineDistance;

import java.time.Duration;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

public class SignalStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final String goodMsg = "The time difference between consecutive AIS signals is great.";
    private static final String badMsg = "The time difference between consecutive AIS signals is anomalous.";

    /**
     * Performs a stateful map operation that receives an AIS signal and produces an
     * AnomalyInformation based on the predefined heuristics for the interval between
     * two consecutive signals.
     *
     * @param value The input value.
     * @return the computed Anomaly Information object
     * @throws Exception - exception from value state descriptors
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        AnomalyInformation anomalyInformation = new AnomalyInformation();
        AnomalyInformation pastAnomalyInformation = getAnomalyInformationValueState().value();
        AISSignal pastAISSignal = getAisSignalValueState().value();

        // In the case that our stateful map has encountered signals in the past
        if (pastAnomalyInformation != null && pastAISSignal != null) {

            double time = Duration.between(getAisSignalValueState().value().getTimestamp(), value.getTimestamp()).toMinutes();
            // If the difference between this and the last signal is more than 10 minutes and
            // the distance average speed it takes to travel from the last point to the current one
            // is greater than 6 km/h, then we update the last detected anomaly time.
            if (time > 10 && harvesineDistance(value.getLatitude(), value.getLongitude(),
                    pastAISSignal.getLatitude(), pastAISSignal.getLongitude()) > time / 60 * 6) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
        }
        return super.setAnomalyInformationResult(anomalyInformation, value, 33f, badMsg, goodMsg);
    }
}
