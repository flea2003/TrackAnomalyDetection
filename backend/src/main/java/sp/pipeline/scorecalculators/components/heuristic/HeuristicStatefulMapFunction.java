package sp.pipeline.scorecalculators.components.heuristic;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import sp.model.AnomalyInformation;
import sp.model.AISSignal;

import static sp.pipeline.scorecalculators.components.heuristic.Tools.timeDiffInMinutes;

@Getter
public abstract class HeuristicStatefulMapFunction extends RichMapFunction<AISSignal, AnomalyInformation> {

    // last processed AIS signal
    private transient ValueState<AISSignal> aisSignalValueState;

    // the anomaly information for the last detected anomaly
    // this is sometimes the same as anomalyInformationValueState, but sometimes different
    private transient ValueState<AnomalyInformation> lastDetectedAnomalyValueState;

    // decimal format for writing floating point numbers in explanations
    DecimalFormat df = new DecimalFormat("#.##");

    abstract AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal);
    abstract float getAnomalyScore();
    abstract String getNonAnomalyExplanation();

    String explanationEnding() {
        return "." + System.lineSeparator();
    }

    @Override
    public void open(Configuration config) {
        aisSignalValueState = getValueState("aisSignal", new TypeHint<>() {});
        lastDetectedAnomalyValueState = getValueState("lastDetectedAnomaly", new TypeHint<>() {});
    }

    private <T> ValueState<T> getValueState(String name, TypeHint<T> typeHint) {
        ValueStateDescriptor<T> descriptor =
                new ValueStateDescriptor<>(
                        name,
                        TypeInformation.of(typeHint)
                );

        return getRuntimeContext().getState(descriptor);
    }

    /**
     * Performs a stateful map operation that receives an AIS signal and produces an
     * AnomalyInformation based on the predefined heuristics for the speed of the ship.
     *
     * @param value The input value.
     * @return the computed Anomaly Information object
     * @throws Exception - exception from value state descriptors
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        checkCurrentSignal(value);

        AnomalyInformation anomalyInfo;
        AnomalyInformation lastDetectedAnomaly = getLastDetectedAnomalyValueState().value();

        if (isLastDetectedAnomalyRecent(lastDetectedAnomaly, value.getTimestamp())) {
            anomalyInfo = new AnomalyInformation(
                    lastDetectedAnomaly.getScore(), lastDetectedAnomaly.getExplanation(),
                    value.getTimestamp(), value.getId()
            );
        } else {
            anomalyInfo = new AnomalyInformation(
                    0f, getNonAnomalyExplanation(), value.getTimestamp(), value.getId()
            );
        }

        // save the AIS signal in the value state for the upcoming signal
        this.getAisSignalValueState().update(value);

        return anomalyInfo;
    }

    private boolean isLastDetectedAnomalyRecent(AnomalyInformation recentAnomaly, OffsetDateTime currentTime) {
        if (recentAnomaly == null) {
            return false;
        }

        return timeDiffInMinutes(recentAnomaly.getCorrespondingTimestamp(), currentTime) <= 30;
    }

    private void checkCurrentSignal(AISSignal currentSignal) throws IOException {
        AISSignal pastSignal = getAisSignalValueState().value();

        // only check if there was a signal in the past
        if (pastSignal == null) {
            return;
        }

        AnomalyScoreWithExplanation result = checkForAnomaly(currentSignal, pastSignal);

        if (result.isAnomaly()) {
            AnomalyInformation anomalyInfo = new AnomalyInformation(
                    result.getAnomalyScore(), result.getExplanation(),
                    currentSignal.getTimestamp(), currentSignal.getId()
            );

            this.lastDetectedAnomalyValueState.update(anomalyInfo);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class AnomalyScoreWithExplanation {
        private boolean isAnomaly;
        private float anomalyScore;
        private String explanation;
    }
}
