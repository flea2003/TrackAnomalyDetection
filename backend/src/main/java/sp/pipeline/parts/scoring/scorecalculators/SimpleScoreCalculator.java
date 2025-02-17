package sp.pipeline.parts.scoring.scorecalculators;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.springframework.stereotype.Component;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.pipeline.parts.scoring.scorecalculators.components.heuristic.ManeuveringStatefulMapFunction;
import sp.pipeline.parts.scoring.scorecalculators.components.heuristic.SignalStatefulMapFunction;
import sp.pipeline.parts.scoring.scorecalculators.components.heuristic.SpeedStatefulMapFunction;
import sp.pipeline.parts.scoring.scorecalculators.components.heuristic.TurningStatefulMapFunction;
import sp.pipeline.parts.scoring.scorecalculators.utils.ZipTupleMapFunction;

@Component
public class SimpleScoreCalculator implements ScoreCalculationStrategy {

    /**
     * Sets up the score calculation part.
     *
     * @param source the source stream of incoming AIS signals
     * @return the computer stream
     */
    @Override
    public DataStream<AnomalyInformation> setupFlinkAnomalyScoreCalculationPart(DataStream<AISSignal> source) {
        KeyedStream<AISSignal, Long> keyedStream = source.keyBy(AISSignal::getId);

        DataStream<AnomalyInformation> signalUpdates = keyedStream.map(new SignalStatefulMapFunction());
        DataStream<AnomalyInformation> speedUpdates = keyedStream.map(new SpeedStatefulMapFunction());
        DataStream<AnomalyInformation> turningUpdates = keyedStream.map(new TurningStatefulMapFunction());
        DataStream<AnomalyInformation> maneuveringUpdates = keyedStream.map(new ManeuveringStatefulMapFunction());

        ZipTupleMapFunction zip = new ZipTupleMapFunction();
        return consume(zip.merge(
                consume(zip.merge(
                        consume(zip.merge(signalUpdates, speedUpdates)),
                        turningUpdates)),
                maneuveringUpdates));
    }

    /**
     * Consumer function which creates a datastore of anomalies from a stream of tuples.
     *
     * @param input the stream of tuples
     * @return the computed stream
     */
    private DataStream<AnomalyInformation> consume(DataStream<Tuple2<AnomalyInformation, AnomalyInformation>> input) {
        return input.map(x -> new AnomalyInformation(x.f0.getScore() + x.f1.getScore(),
            x.f0.getExplanation() + x.f1.getExplanation(),
            x.f0.getCorrespondingTimestamp(),
            x.f0.getId()));
    }

}
