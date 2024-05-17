package sp.pipeline.scorecalculators.utils;

import java.util.LinkedList;
import java.util.Queue;
import lombok.Getter;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;

@Getter
public class ZipMapFunction <T>{

    private DataStream<Tuple2<T, ?>> mergedStream;

    public ZipMapFunction(DataStream<T> initialStream){
        this.mergedStream = initialStream.map(x -> Tuple2.of(x, null));
    }

    public ZipMapFunction<T> merge(DataStream<T> stream) {
        this.mergedStream = merge(this.mergedStream.map(x -> x.f0), stream.map(x -> Tuple2.of(x, null)));
        return this;
    }

    private DataStream<Tuple2<T, ?>> merge(DataStream<T>stream1, DataStream<Tuple2<T, ?>>stream2){
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
                synchronized (lock){
                    while(!list1.isEmpty() && !list2.isEmpty()){
                        out.collect(Tuple2.of(list1.poll(), list2.poll()));
                    }
                }
            }
        });

    }
}