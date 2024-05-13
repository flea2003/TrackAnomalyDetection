package sp.pipeline.scoreCalculators;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.springframework.stereotype.Component;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.pipeline.scoreCalculators.components.heuristic.SignalStatefulMapFunction;
import sp.pipeline.scoreCalculators.components.heuristic.TurningStatefulMapFunction;
import sp.pipeline.scoreCalculators.components.heuristic.SpeedStatefulMapFunction;

@Component
public class SimpleScoreCalculator implements ScoreCalculationStrategy{
    @Override
    public DataStream<AnomalyInformation> setupFlinkAnomalyScoreCalculationPart(DataStream<AISSignal> source) {
        KeyedStream<AISSignal, String> keyedStream = source.keyBy(AISSignal::getShipHash);

        DataStream<AnomalyInformation>signalUpdates = keyedStream.map(new SignalStatefulMapFunction());
        DataStream<AnomalyInformation>speedUpdates = keyedStream.map(new SpeedStatefulMapFunction());
        DataStream<AnomalyInformation>turningUpdates = keyedStream.map(new TurningStatefulMapFunction());

        DataStream<AnomalyInformation>mergedStream = signalUpdates.union(speedUpdates, turningUpdates);
        mergedStream.keyBy(AnomalyInformation::getShipHash).reduce((update1, update2) -> {
            AnomalyInformation anomalyInformation = new AnomalyInformation();
            anomalyInformation.setScore(update1.getScore() + update2.getScore());

            if(anomalyInformation.getScore() == 0.0){
                anomalyInformation.setExplanation("Ship Looks Good");
            }
            else if(anomalyInformation.getScore() == 0.25){
                anomalyInformation.setExplanation("Anomalous Speed");
            }
            else if(anomalyInformation.getScore() == 0.35){
                anomalyInformation.setExplanation("Anomalous Turning");
            }
            else if(anomalyInformation.getScore() == 0.4){
                anomalyInformation.setExplanation("Anomalous Signals");
            }
            else if(anomalyInformation.getScore() == 0.6){
                anomalyInformation.setExplanation("Anomalous Speed and Turning");
            }
            else if(anomalyInformation.getScore() == 0.65){
                anomalyInformation.setExplanation("Anomalous Speed and Signals");
            }
            else if(anomalyInformation.getScore() == 0.75){
                anomalyInformation.setExplanation("Anomalous Turning and Signals");
            }
            else if(anomalyInformation.getScore() == 1.0){
                anomalyInformation.setExplanation("Anomalous Speed, Signals and Turning");
            }

            return anomalyInformation;
        });

        return mergedStream;
    }
}
