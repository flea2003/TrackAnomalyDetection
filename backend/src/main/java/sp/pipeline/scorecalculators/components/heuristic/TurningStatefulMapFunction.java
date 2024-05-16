package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.circularMetric;

import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final String goodMsg = "The ship's turning direction is great.";
    private static final String badMsg = "The ship's turning direction is anomalous.";

    /**
     * Performs a stateful map operation that receives an AIS signal and produces an
     * AnomalyInformation based on the predefined heuristics for the turning direction of the ship.
     *
     * @param value The input value.
     * @return the computed Anomaly Information object
     * @throws Exception - exception from value state descriptors
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        // A 511 heading means that no heading is reported, so we just set it to be equal to the heading value of the ship
        if (value.getHeading() == 511) {
            value.setHeading(value.getCourse());
        }
        AnomalyInformation anomalyInformation = new AnomalyInformation();
        AnomalyInformation pastAnomalyInformation = getAnomalyInformationValueState().value();
        AISSignal pastAISSignal = getAisSignalValueState().value();
        // In the case that our stateful map has encountered signals in the past
        if (pastAnomalyInformation != null && pastAISSignal != null) {
            // If any of these heuristics hold, then we update our lastDetectedAnomalyTime:
            // 1. Check if the absolute difference modulo difference between consecutive headings is greater than 40.
            // 2. Check if the absolute difference modulo difference between consecutive courses is greater than 40.
            double headingDifference = circularMetric(pastAISSignal.getHeading(), value.getHeading());
            double corseDifference = circularMetric(pastAISSignal.getCourse(), value.getCourse());
            if (headingDifference >= 40 || corseDifference >= 40) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
        }
        return super.setAnomalyInformationResult(anomalyInformation, value, 34f, badMsg, goodMsg);
    }
}
