package sp.pipeline.scoreCalculators.components.heuristic;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.dtos.Timestamp;

public abstract class HeuristicStatefulMapFunction extends RichMapFunction<AISSignal, AnomalyInformation> {

    transient ValueState<AnomalyInformation> anomalyInformationValueState;
    transient ValueState<AISSignal> AISSignalValueState;
    transient ValueState<Timestamp> lastDetectedAnomalyTime;

    @Override
    public void open(Configuration config) {

        // Setup the state descriptors
        ValueStateDescriptor<Timestamp> lastDetectedAnomalyTimeDescriptor =
                new ValueStateDescriptor<>(
                        "time",
                        TypeInformation.of(new TypeHint<Timestamp>() {})
                );

        ValueStateDescriptor<AISSignal> AISSignalValueStateDescriptor =
                new ValueStateDescriptor<>(
                        "AIS",
                        TypeInformation.of(new TypeHint<AISSignal>() {})
                );

        ValueStateDescriptor<AnomalyInformation> anomalyInformationValueStateDescriptor=
                new ValueStateDescriptor<>(
                        "anomaly",
                        TypeInformation.of(new TypeHint<AnomalyInformation>() {})
                );

        // Initialize the states and set them to be accessible in the map function
        lastDetectedAnomalyTime = getRuntimeContext().getState(lastDetectedAnomalyTimeDescriptor);
        AISSignalValueState = getRuntimeContext().getState(AISSignalValueStateDescriptor);
        anomalyInformationValueState = getRuntimeContext().getState(anomalyInformationValueStateDescriptor);
    }

}
