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

public class SignalStatefulMapFunction extends HeuristicStatefulMapFunction {

    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {

        AnomalyInformation anomalyInformation = new AnomalyInformation();

        AnomalyInformation pastAnomalyInformation = anomalyInformationValueState.value();
        AISSignal pastAISSignal = AISSignalValueState.value();

        if(pastAnomalyInformation != null && pastAISSignal != null){

            double time = value.timestamp.difference(pastAISSignal.timestamp);
            if(time > 10 && harvesineDistance(value.latitude, value.longitude,
                    pastAISSignal.latitude, pastAISSignal.longitude) > time / 60 * 6){
                lastDetectedAnomalyTime.update(value.timestamp);
            }

            anomalyInformation.setShipHash(value.shipHash);
            anomalyInformation.setCorrespondingTimestamp(value.timestamp);

            if(value.timestamp.difference(lastDetectedAnomalyTime.value()) > 30){
                anomalyInformation.setScore(0.4f - pastAnomalyInformation.getScore());
                anomalyInformation.setExplanation("Bad Signals.");
            }else{
                anomalyInformation.setScore(0 - pastAnomalyInformation.getScore());
                anomalyInformation.setExplanation("Good Signals.");
            }
        }
        else {
            anomalyInformation.setScore(0.0f);
            anomalyInformation.setExplanation("Good Signals.");
            anomalyInformation.setShipHash(value.shipHash);
            anomalyInformation.setCorrespondingTimestamp(value.timestamp);
        }
        anomalyInformationValueState.update(anomalyInformation);
        AISSignalValueState.update(value);

        return anomalyInformation;
    }
}
