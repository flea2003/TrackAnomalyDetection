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

import static sp.pipeline.scoreCalculators.components.heuristic.Utils.harvesineDistance;

public class SpeedStatefulMapFunction extends HeuristicStatefulMapFunction {

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
            double globeDistance = harvesineDistance(value.latitude, value.longitude,
                    pastAISSignal.latitude, pastAISSignal.longitude);
            double timeDifference = value.timestamp.difference(pastAISSignal.timestamp);
            double computedSpeed = globeDistance / timeDifference;

            if(computedSpeed > 40){
                lastDetectedAnomalyTime.update(value.timestamp);
            }else if(Math.abs(value.speed - computedSpeed) > 10){
                lastDetectedAnomalyTime.update(value.timestamp);
            }else if((AISSignalValueState.value().speed - value.speed) / timeDifference > 50) {
                lastDetectedAnomalyTime.update(value.timestamp);
            }
            anomalyInformation.setShipHash(value.shipHash);
            anomalyInformation.setCorrespondingTimestamp(value.timestamp);

            if(lastDetectedAnomalyTime.value() != null && value.timestamp.difference(lastDetectedAnomalyTime.value()) <= 300){
                anomalyInformation.setScore(33.0f);
                anomalyInformation.setExplanation("Bad Speed.");
            }else{
                anomalyInformation.setScore(0.0f);
                anomalyInformation.setExplanation("Good Speed.");
            }
        }
        else {
            anomalyInformation.setScore(0.0f);
            anomalyInformation.setExplanation("Good Speed.");
            anomalyInformation.setShipHash(value.shipHash);
            anomalyInformation.setCorrespondingTimestamp(value.timestamp);
        }
        anomalyInformationValueState.update(anomalyInformation);
        AISSignalValueState.update(value);

        System.out.println(anomalyInformation.getExplanation());


        return anomalyInformation;
    }

}
