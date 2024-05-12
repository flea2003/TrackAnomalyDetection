//package sp.pipeline.scoreCalculators.components;
//
//import org.apache.flink.api.common.functions.RichMapFunction;
//import org.apache.flink.api.common.state.ListStateDescriptor;
//import org.apache.flink.api.common.state.StateTtlConfig;
//import org.apache.flink.api.common.state.ValueState;
//import org.apache.flink.api.common.state.ValueStateDescriptor;
//import org.apache.flink.api.common.time.Time;
//import org.apache.flink.api.common.typeinfo.TypeHint;
//import org.apache.flink.api.common.typeinfo.TypeInformation;
//import org.apache.flink.configuration.Configuration;
//import sp.dtos.AISSignal;
//import sp.dtos.AnomalyInformation;
//
//import java.time.Duration;
//
//public class SpeedStatefulMapFunction extends RichMapFunction<AISSignal, AnomalyInformation> {
//
//    private transient ValueState<Float> anomalyScore;
//
//    private transient ValueState<Float> speed;
//
//    /**
//     * The method initializes the state.
//     * @param config The configuration containing the parameters attached to the contract.
//     */
//    @Override
//    public void open(Configuration config) {
//
//        // Setup the time-to-live for the state (30 minutes)
//        StateTtlConfig ttlConfig = StateTtlConfig
//                .newBuilder(Time.fromDuration(Duration.ofMinutes(30)))
//                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
//                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
//                .build();
//
//        // Setup the state descriptors
//        ValueStateDescriptor<Float> anomalyDescriptor =
//                new ValueStateDescriptor<>(
//                        "score",
//                        TypeInformation.of(new TypeHint<Float>() {})
//                );
//
//        ValueStateDescriptor<Float> speedDescriptor =
//                new ValueStateDescriptor<>(
//                        "speed",
//                        TypeInformation.of(new TypeHint<Float>() {})
//                );
//
//        // Set time to live for both states
//        anomalyDescriptor.enableTimeToLive(ttlConfig);
//        speedDescriptor.enableTimeToLive(ttlConfig);
//
//        // Initialize the states and set them to be accessible in the map function
//        anomalyScore = getRuntimeContext().getState(anomalyDescriptor);
//        speed = getRuntimeContext().getState(speedDescriptor);
//    }
//
//    /**
//     * Performs a stateful map operation from an incoming AISSignal to an Anomaly Information object.
//     * @param value The input value.
//     * @return the computed Anomaly Information object
//     * @throws Exception
//     */
//    @Override
//    public AnomalyInformation map(AISSignal value) throws Exception {
//
//        AnomalyInformation anomalyInformation = new AnomalyInformation();
//
//        if(speed == null){
//            anomalyScore.update(0);
//            speed.update(value.speed);
//            anomalyInformation.setScore(0);
//            anomalyInformation.setExplanation("");
//            anomalyInformation.setCorrespondingTimestamp(value.timestamp);
//            anomalyInformation.setShipHash(value.shipHash);
//            return anomalyInformation;
//        }
//        else{
//
//        }
//    }
//}
