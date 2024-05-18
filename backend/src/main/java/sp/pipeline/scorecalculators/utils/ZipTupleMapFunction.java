package sp.pipeline.scorecalculators.utils;

import java.util.LinkedList;
import java.util.Queue;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;
import sp.dtos.AnomalyInformation;

public class ZipTupleMapFunction {

    /**
     * Takes two datastore of AnomalyInformation and produces one datastore of tuples.
     *
     * @param stream1 - first stream that we merge
     * @param stream2 - second stream that we merge
     * @return - the merged stream
     */
    public DataStream<Tuple2<AnomalyInformation, AnomalyInformation>> merge(DataStream<AnomalyInformation> stream1,
                                                                            DataStream<AnomalyInformation> stream2) {
        return stream1.connect(stream2).flatMap(new CoFlatMapFunction<>() {
            private final Queue<AnomalyInformation> list1 = new LinkedList<>();
            private final Queue<AnomalyInformation> list2 = new LinkedList<>();
            private final Integer lock = 10;

            @Override
            public void flatMap1(AnomalyInformation value, Collector<Tuple2<AnomalyInformation, AnomalyInformation>> out) {
                list1.add(value);
                emitTuple(out);
            }

            @Override
            public void flatMap2(AnomalyInformation value, Collector<Tuple2<AnomalyInformation, AnomalyInformation>> out) {
                list2.add(value);
                emitTuple(out);
            }

            private void emitTuple(Collector<Tuple2<AnomalyInformation, AnomalyInformation>> out) {
                synchronized (lock) {
                    while (!list1.isEmpty() && !list2.isEmpty()) {
                        out.collect(Tuple2.of(list1.poll(), list2.poll()));
                    }
                }
            }
        });

    }
}
