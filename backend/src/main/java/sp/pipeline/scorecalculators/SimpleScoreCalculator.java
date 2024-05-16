package sp.pipeline.scorecalculators;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;
import org.springframework.stereotype.Component;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.pipeline.scorecalculators.components.heuristic.SignalStatefulMapFunction;
import sp.pipeline.scorecalculators.components.heuristic.SpeedStatefulMapFunction;
import sp.pipeline.scorecalculators.components.heuristic.TurningStatefulMapFunction;

@Component
public class SimpleScoreCalculator implements ScoreCalculationStrategy {

    @Override
    public DataStream<AnomalyInformation> setupFlinkAnomalyScoreCalculationPart(DataStream<AISSignal> source) {
        KeyedStream<AISSignal, String> keyedStream = source.keyBy(AISSignal::getShipHash);

        DataStream<AnomalyInformation> signalUpdates = keyedStream.map(new SignalStatefulMapFunction());
        DataStream<AnomalyInformation> speedUpdates = keyedStream.map(new SpeedStatefulMapFunction());
        DataStream<AnomalyInformation> turningUpdates = keyedStream.map(new TurningStatefulMapFunction());

        DataStream<Tuple2<AnomalyInformation, AnomalyInformation>> merged = signalUpdates.connect(speedUpdates)
            .flatMap(new TupleMergeFunction());


        DataStream<Tuple3<AnomalyInformation, AnomalyInformation, AnomalyInformation>> result = turningUpdates.connect(merged)
            .flatMap(new TripleMergeFunction());

        return result.map(x -> {
            AnomalyInformation combined = new AnomalyInformation();
            combined.setScore(x.f0.getScore() + x.f1.getScore() + x.f2.getScore());
            combined.setExplanation(x.f0.getExplanation() + '\n' + x.f1.getExplanation() + '\n' + x.f2.getExplanation());
            combined.setShipHash(x.f0.getShipHash());
            combined.setCorrespondingTimestamp(x.f0.getCorrespondingTimestamp());

            return combined;
        });

    }

    private static class TupleMergeFunction implements CoFlatMapFunction<AnomalyInformation, AnomalyInformation,
        Tuple2<AnomalyInformation, AnomalyInformation>> {
        private AnomalyInformation anomalyInfo1;
        private AnomalyInformation anomalyInfo2;

        @Override
        public void flatMap1(AnomalyInformation value, Collector<Tuple2<AnomalyInformation, AnomalyInformation>> out)
            throws Exception {
            anomalyInfo1 = value;
            emitTuple(out);
        }

        @Override
        public void flatMap2(AnomalyInformation value, Collector<Tuple2<AnomalyInformation, AnomalyInformation>> out)
            throws Exception {
            anomalyInfo2 = value;
            emitTuple(out);
        }

        private void emitTuple(Collector<Tuple2<AnomalyInformation, AnomalyInformation>> out) {
            if (anomalyInfo1 != null && anomalyInfo2 != null) {
                out.collect(new Tuple2<>(anomalyInfo1, anomalyInfo2));
                anomalyInfo2 = null;
                anomalyInfo1 = null;
            }
        }
    }

    private static class TripleMergeFunction implements CoFlatMapFunction<AnomalyInformation,
        Tuple2<AnomalyInformation, AnomalyInformation>,
        Tuple3<AnomalyInformation, AnomalyInformation, AnomalyInformation>> {
        private AnomalyInformation anomalyInfo1;
        private AnomalyInformation anomalyInfo2;
        private AnomalyInformation anomalyInfo3;

        @Override
        public void flatMap1(AnomalyInformation value, Collector<Tuple3<AnomalyInformation,
            AnomalyInformation, AnomalyInformation>> out) throws Exception {
            anomalyInfo1 = value;
            emitTuple(out);
        }

        @Override
        public void flatMap2(Tuple2<AnomalyInformation, AnomalyInformation> value,
                             Collector<Tuple3<AnomalyInformation, AnomalyInformation, AnomalyInformation>> out)
            throws Exception {
            anomalyInfo2 = value.f0;
            anomalyInfo3 = value.f1;
            emitTuple(out);
        }

        private void emitTuple(Collector<Tuple3<AnomalyInformation, AnomalyInformation, AnomalyInformation>> out) {
            if (anomalyInfo1 != null && anomalyInfo2 != null && anomalyInfo3 != null) {
                out.collect(new Tuple3<>(anomalyInfo1, anomalyInfo2, anomalyInfo3));
                anomalyInfo3 = null;
                anomalyInfo2 = null;
                anomalyInfo1 = null;
            }
        }
    }

}
