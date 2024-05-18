package sp.pipeline.scorecalculators.utils;

import java.util.LinkedList;
import java.util.Queue;
import lombok.Getter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;

/*
 I couldn't finish this class, but maybe somebody can.
 It can be used to merge smoothly multiple heuristics together
 */
@Getter
public class ZipGenericMapFunction<T> {

    private DataStream<Tuple2<T, ?>> mergedStream;

    /**
     * The initial stream that is the beginning of our merge.
     *
     * @param initialStream - the beginning of our merge
     */
    public ZipGenericMapFunction(DataStream<T> initialStream) {
        this.mergedStream = initialStream.map(x -> Tuple2.of(x, null));
    }

    /**
     * Produces the class with one more neste merge.
     *
     * @param stream - The stream we merge our currently tuple with
     * @return - The current class
     */
    public ZipGenericMapFunction<T> merge(DataStream<T> stream) {
        this.mergedStream = merge(stream, this.mergedStream);
        return this;
    }

    /**
     * The merge operation with the new and the old streams.
     *
     * @param stream1 - the new stream in our pipeline
     * @param stream2 - the currently merged streams
     * @return - the tuple of merged streams between stream1 and stream2
     */
    private DataStream<Tuple2<T, ?>> merge(DataStream<T> stream1, DataStream<Tuple2<T, ?>> stream2) {
        return stream1.connect(stream2).flatMap(new CoFlatMapFunction<>() {

            private final Queue<T> list1 = new LinkedList<>();
            private final Queue<Tuple2<T, ?>> list2 = new LinkedList<>();
            private final Object lock = new Object();

            @Override
            public void flatMap1(T value, Collector<Tuple2<T, ?>> out) throws Exception {
                list1.add(value);
                emitTuple(out);
            }

            @Override
            public void flatMap2(Tuple2<T, ?> value, Collector<Tuple2<T, ?>> out) throws Exception {
                list2.add(value);
                emitTuple(out);
            }

            private void emitTuple(Collector<Tuple2<T, ?>> out) {
                synchronized (lock) {
                    while (!list1.isEmpty() && !list2.isEmpty()) {
                        out.collect(Tuple2.of(list1.poll(), list2.poll()));
                    }
                }
            }
        });

    }
}
