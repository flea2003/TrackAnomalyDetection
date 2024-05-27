package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

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

    /*
        We have three value states, one for last processed aisSignal, one for the
        one for last processed anomaly information and one for the last time when
        we have detected an anomaly.
     */
    private transient ValueState<AnomalyInformation> anomalyInformationValueState;
    private transient ValueState<AISSignal> aisSignalValueState;
    private transient ValueState<OffsetDateTime> lastDetectedAnomalyTime;

    /**
     * Initializes the heuristic score calculator. More precisely, initializes the value
     * states for anomaly information, last AIS signal and last detected anomaly timestamp.
     *
     * @param config the properties for Flink environment
     */
    @Override
    public void open(Configuration config) {

        // Set up the state descriptors
        ValueStateDescriptor<OffsetDateTime> lastDetectedAnomalyTimeDescriptor =
                new ValueStateDescriptor<>(
                        "time",
                        TypeInformation.of(new TypeHint<>() {})
                );

        ValueStateDescriptor<AISSignal> aisSignalValueStateDescriptor =
                new ValueStateDescriptor<>(
                        "AIS",
                        TypeInformation.of(new TypeHint<>() {})
                );

        ValueStateDescriptor<AnomalyInformation> anomalyInformationValueStateDescriptor =
                new ValueStateDescriptor<>(
                        "anomaly",
                        TypeInformation.of(new TypeHint<>() {})
                );

        // Initialize the states and set them to be accessible in the map function
        lastDetectedAnomalyTime = getRuntimeContext().getState(lastDetectedAnomalyTimeDescriptor);
        aisSignalValueState = getRuntimeContext().getState(aisSignalValueStateDescriptor);
        anomalyInformationValueState = getRuntimeContext().getState(anomalyInformationValueStateDescriptor);
    }

    /**
     * Logic for setting the result of the anomaly detected by each heuristic.
     *
     * @param value the AIS signal that was processed
     * @param anomalyScore the score that each heuristic can set
     * @param badMsg the explanation in case of anomaly
     * @param goodMsg the message in case that it is not an anomaly
     * @return the computed AnomalyInformation
     * @throws Exception Exception generated by the getValue() of the stateDescriptors, probably should be caught :D
     */
    public AnomalyInformation setAnomalyInformationResult(AISSignal value,
                                                          Float anomalyScore, String badMsg, String goodMsg) throws Exception {
        // The AIS signal is considered an anomaly only if the difference between the current time and the last detected anomaly
        // time is less than 30 minutes
        AnomalyInformation anomalyInformation;
        if (getLastDetectedAnomalyTime().value() != null
                && Duration.between(getLastDetectedAnomalyTime().value(), value.getTimestamp()).toMinutes() <= 30) {
            anomalyInformation = new AnomalyInformation(anomalyScore, badMsg, value.getTimestamp(), value.getId());
        } else {
            anomalyInformation = new AnomalyInformation(0f, goodMsg, value.getTimestamp(), value.getId());
        }
        getAnomalyInformationValueState().update(anomalyInformation);
        getAisSignalValueState().update(value);
        return anomalyInformation;
    }

}
