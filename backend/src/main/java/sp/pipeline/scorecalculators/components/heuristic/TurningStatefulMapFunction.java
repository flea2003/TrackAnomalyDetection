package sp.pipeline.scorecalculators.components.heuristic;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.circularMetric;

import sp.model.AISSignal;
import sp.dtos.AnomalyInformation;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final String goodMsg = "The ship's turning direction is ok.";
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
            boolean headingDifferenceIsGood = circularMetric(pastAISSignal.getHeading(), value.getHeading()) < 40;
            boolean courseDifferenceIsGood = circularMetric(pastAISSignal.getCourse(), value.getCourse()) < 40;

            if (!headingDifferenceIsGood || !courseDifferenceIsGood) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
        }
        return super.setAnomalyInformationResult(anomalyInformation, value, 34f, badMsg, goodMsg);
    }
}
