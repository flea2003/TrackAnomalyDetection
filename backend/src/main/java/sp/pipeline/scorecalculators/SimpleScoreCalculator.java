package sp.pipeline.scorecalculators;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.springframework.stereotype.Component;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.pipeline.scorecalculators.components.heuristic.SignalStatefulMapFunction;
import sp.pipeline.scorecalculators.components.heuristic.SpeedStatefulMapFunction;
import sp.pipeline.scorecalculators.components.heuristic.TurningStatefulMapFunction;
import sp.pipeline.scorecalculators.utils.ZipMapFunction;

@Component
public class SimpleScoreCalculator implements ScoreCalculationStrategy {

    @Override
    public DataStream<AnomalyInformation> setupFlinkAnomalyScoreCalculationPart(DataStream<AISSignal> source) {
        KeyedStream<AISSignal, String> keyedStream = source.keyBy(AISSignal::getShipHash);

        DataStream<AnomalyInformation> signalUpdates = keyedStream.map(new SignalStatefulMapFunction());
        DataStream<AnomalyInformation> speedUpdates = keyedStream.map(new SpeedStatefulMapFunction());
        DataStream<AnomalyInformation> turningUpdates = keyedStream.map(new TurningStatefulMapFunction());

        ZipMapFunction<AnomalyInformation> zip = new ZipMapFunction(signalUpdates);
        zip.merge(speedUpdates).merge(turningUpdates);
        return zip.getMergedStream().map(this::consume);
    }

    private AnomalyInformation consume(Tuple2<AnomalyInformation, ?>input){
        AnomalyInformation first = input.f0;
        var second = input.f1;
        AnomalyInformation anomalyInformationSecond;
        if(second instanceof AnomalyInformation){
            anomalyInformationSecond = consume((Tuple2<AnomalyInformation, ?>)second);
        }else{
            anomalyInformationSecond = new AnomalyInformation(0f, "",
                first.getCorrespondingTimestamp(), first.getShipHash());
        }
        return new AnomalyInformation(first.getScore() + anomalyInformationSecond.getScore(),
            first.getExplanation() + anomalyInformationSecond.getExplanation(),
            first.getCorrespondingTimestamp(),
            first.getShipHash());
    }

}
