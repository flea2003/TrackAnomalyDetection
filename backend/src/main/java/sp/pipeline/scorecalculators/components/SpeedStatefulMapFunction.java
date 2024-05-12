package sp.pipeline.scoreCalculators.components;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.dtos.Timestamp;

import java.time.Duration;

public class SpeedStatefulMapFunction extends RichMapFunction<AISSignal, AnomalyInformation> {

    private transient ValueState<AISSignal> AISSignalValueState;
    private transient ValueState<AnomalyInformation> anomalyInformationValueState;
    private transient ValueState<AnomalyInformation> lastDetectedAnomalyTime;

    /**
     * The method initializes the state.
     * @param config The configuration containing the parameters attached to the contract.
     */
    @Override
    public void open(Configuration config) {

        // Setup the time-to-live for the state (30 minutes)
        StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.fromDuration(Duration.ofMinutes(30)))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();

        // Setup the state descriptors
        ValueStateDescriptor<Float> anomalyDescriptor =
                new ValueStateDescriptor<>(
                        "score",
                        TypeInformation.of(new TypeHint<Float>() {})
                );

        ValueStateDescriptor<Float> speedDescriptor =
                new ValueStateDescriptor<>(
                        "speed",
                        TypeInformation.of(new TypeHint<Float>() {})
                );

        // Set time to live for both states
        anomalyDescriptor.enableTimeToLive(ttlConfig);
        speedDescriptor.enableTimeToLive(ttlConfig);

        // Initialize the states and set them to be accessible in the map function
        anomalyScore = getRuntimeContext().getState(anomalyDescriptor);
        speed = getRuntimeContext().getState(speedDescriptor);
    }

    /**
     * Performs a stateful map operation from an incoming AISSignal to an Anomaly Information object.
     * @param value The input value.
     * @return the computed Anomaly Information object
     * @throws Exception
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {

        AnomalyInformation anomalyInformation = new AnomalyInformation();

        AnomalyInformation pastAnomalyInformation = anomalyInformationValueState.value();
        AISSignal pastAISSignal = AISSignalValueState.value();


        if(pastAnomalyInformation != null && pastAISSignal != null){
            Boolean anomalyDetected = false;

            double globeDistance = harvesineDistance(value.latitude, value.longitude,
                    pastAISSignal.latitude, pastAISSignal.longitude);
            double timeDifference = value.timestamp.difference(pastAISSignal.timestamp);
            double computedSpeed = globeDistance / timeDifference;

            if(computedSpeed > 40){
                anomalyDetected = true;
            }else if(Math.abs(value.speed - computedSpeed) > 10){

            }else{

            }
        }
        else {
            anomalyInformation.setScore(0);
            anomalyInformation.setDescription("");
            anomalyInformation.setShipHash(value.shipHash);
            anomalyInformation.setCorrespondingTimestamp(value.timestamp);
        }
        anomalyInformationValueState.update(anomalyInformation);
        AISSignalValueState.update(value);

        return anomalyInformation;
    }

    public double harvesineDistance(float lat1, float lon1, float lat2, float lon2){
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dlon = lon2Rad - lon1Rad;
        double dlat = lat2Rad - lat1Rad;

        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double R = 6371;

        return R * c;
    }
}
