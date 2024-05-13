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

public class TurningStatefulMapFunction extends HeuristicStatefulMapFunction {

    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {

        if(value.heading == 511){
            value.setHeading(value.course);
        }

        AnomalyInformation anomalyInformation = new AnomalyInformation();

        AnomalyInformation pastAnomalyInformation = anomalyInformationValueState.value();
        AISSignal pastAISSignal = AISSignalValueState.value();

        if(pastAnomalyInformation != null && pastAISSignal != null){

            double headingDifference = 180.0 - Math.abs(180 - (pastAISSignal.heading % 360 - value.heading % 360 + 360) % 360);
            double corseDifference = 180.0 - Math.abs(180 - (pastAISSignal.course % 360 - value.course % 360 + 360) % 360);

            if(headingDifference >= 40 || corseDifference >= 40){
                lastDetectedAnomalyTime.update(value.timestamp);
            }

            anomalyInformation.setShipHash(value.shipHash);
            anomalyInformation.setCorrespondingTimestamp(value.timestamp);

            if(lastDetectedAnomalyTime.value() != null && value.timestamp.difference(lastDetectedAnomalyTime.value()) <= 300){
                anomalyInformation.setScore(34.0f);
                anomalyInformation.setExplanation("Bad Turning");
            }else{
                anomalyInformation.setScore(0.0f);
                anomalyInformation.setExplanation("Good Turning");
            }
        }
        else {
            anomalyInformation.setScore(0.0f);
            anomalyInformation.setExplanation("Good Turning");
            anomalyInformation.setShipHash(value.shipHash);
            anomalyInformation.setCorrespondingTimestamp(value.timestamp);
        }
        anomalyInformationValueState.update(anomalyInformation);
        AISSignalValueState.update(value);

        System.out.println(anomalyInformation.getExplanation());

        return anomalyInformation;
    }
}
