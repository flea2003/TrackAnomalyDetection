package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.circularMetric;

import sp.model.AISSignal;
import sp.model.AnomalyInformation;

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final String goodMsg = "The ship's turning direction is ok.";
    private static final String badMsg = "The ship's turning direction is anomalous.";
    private static final int NO_HEADING = 511;

    /**
     * Performs a stateful map operation that receives an AIS signal and produces an
     * AnomalyInformation based on the predefined heuristics for the turning direction of the ship.
     *
     * @param value The input value.
     * @return the computed Anomaly Information object
     * @throws Exception exception from value state descriptors
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        float valueHeading = value.getHeading();
        float valueCourse = value.getCourse();

        // A 511 heading means that no heading is reported, so we just take it to be equal to the heading value of the ship
        if (valueHeading == NO_HEADING) {
            valueHeading = valueCourse;
        }

        AISSignal pastAISSignal = getAisSignalValueState().value();

        // In the case that our stateful map has encountered signals in the past
        if (pastAISSignal != null) {
            float pastValueHeading = getAisSignalValueState().value().getHeading();
            float pastValueCourse = getAisSignalValueState().value().getCourse();

            if (pastValueHeading == NO_HEADING) {
                pastValueHeading = pastValueCourse;
            }

            boolean headingDifferenceIsGood = circularMetric(pastValueHeading, valueHeading) < 40;
            boolean courseDifferenceIsGood = circularMetric(pastValueCourse, valueCourse) < 40;


            if (!headingDifferenceIsGood || !courseDifferenceIsGood) {
                getLastDetectedAnomalyTime().update(value.getTimestamp());
            }
        }
        return super.setAnomalyInformationResult(value, 34f, badMsg, goodMsg);
    }
}
