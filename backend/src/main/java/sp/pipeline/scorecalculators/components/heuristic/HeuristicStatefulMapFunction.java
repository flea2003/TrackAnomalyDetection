package sp.pipeline.scorecalculators.components.heuristic;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import lombok.Getter;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import sp.model.AnomalyInformation;
import sp.model.AISSignal;

@Getter
public abstract class HeuristicStatefulMapFunction extends RichMapFunction<AISSignal, AnomalyInformation> {

    // last processed AIS signal
    private transient ValueState<AISSignal> aisSignalValueState;

    // the anomaly information for the last detected anomaly
    // this is sometimes the same as anomalyInformationValueState, but sometimes different
    private transient ValueState<AnomalyInformation> lastDetectedAnomalyValueState;

    public abstract boolean isAnomaly(AISSignal currentSignal, AISSignal pastSignal);
    public abstract float getAnomalyScore();
    public abstract String getAnomalyExplanation(AISSignal currentSignal, AISSignal pastSignal);
    public abstract String getNonAnomalyExplanation();

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

        // save the signal in the value state for the upcoming signal
        this.getAisSignalValueState().update(value);

        return anomalyInfo;
    }

    private boolean isLastDetectedAnomalyRecent(AnomalyInformation recentAnomaly, OffsetDateTime currentTime) {
        if (recentAnomaly == null) {
            return false;
        }

        long timeDiffInMinutes = Duration.between(recentAnomaly.getCorrespondingTimestamp(), currentTime).toMinutes();
        return timeDiffInMinutes <= 30;
    }

    private void checkCurrentSignal(AISSignal currentSignal) throws IOException {
        AISSignal pastSignal = getAisSignalValueState().value();

        // only check if there was a signal in the past
        if (pastSignal == null) {
            return;
        }

        if (isAnomaly(currentSignal, pastSignal)) {
            AnomalyInformation anomalyInfo = new AnomalyInformation(
                    getAnomalyScore(), getAnomalyExplanation(currentSignal, pastSignal),
                    currentSignal.getTimestamp(), currentSignal.getId()
            );

            this.lastDetectedAnomalyValueState.update(anomalyInfo);
        }
    }

}
