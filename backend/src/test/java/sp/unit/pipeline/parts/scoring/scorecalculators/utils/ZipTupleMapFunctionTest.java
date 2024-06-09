package sp.unit.pipeline.parts.scoring.scorecalculators.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.ClassRule;
import org.junit.Test;
import sp.model.AnomalyInformation;
import sp.pipeline.parts.scoring.scorecalculators.utils.ZipTupleMapFunction;


public class ZipTupleMapFunctionTest {

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
        new MiniClusterWithClientResource(
            new MiniClusterResourceConfiguration.Builder()
                .setNumberSlotsPerTaskManager(1)
                .setNumberTaskManagers(1)
                .build());

    @Test
    public void testSetupFlinkAnomalyScoreCalculationPart() throws Exception {
        // create initial AISSignal objects
        // ais1 and ais2 are from the same ship
        OffsetDateTime time1 = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));
        OffsetDateTime time2 = OffsetDateTime.of(2004, 1, 27, 1,15,0,0, ZoneOffset.ofHours(0));

        AnomalyInformation ai1 = new AnomalyInformation(33f, "bad", time1, (long) 1);
        AnomalyInformation ai2 = new AnomalyInformation(0f, "good", time1, (long) 1);
        AnomalyInformation ai3 = new AnomalyInformation(66f, "super bad", time2, (long) 1);
        AnomalyInformation ai4 = new AnomalyInformation(33f, "bad", time2, (long) 1);

        // prepare flink environment and streams
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<AnomalyInformation>anomalyStream1 = env.fromData(List.of(ai1, ai3));
        DataStream<AnomalyInformation>anomalyStream2 = env.fromData(List.of(ai2, ai4));

        ZipTupleMapFunction zip = new ZipTupleMapFunction();
        DataStream<Tuple2<AnomalyInformation, AnomalyInformation>> resultStream = zip.merge(anomalyStream1, anomalyStream2);

        CollectSink.anomalyInfoList.clear();
        resultStream.addSink(new CollectSink());
        env.execute();
        env.close();

        // verify result
        List<Tuple2<AnomalyInformation, AnomalyInformation>> result = ZipTupleMapFunctionTest.CollectSink.anomalyInfoList;

        assertThat(result).contains(
            new Tuple2<>(
                new AnomalyInformation(33f, "bad", time1, (long) 1),
                new AnomalyInformation(0f, "good", time1, (long) 1)));

        assertThat(result).contains(
            new Tuple2<>(
                new AnomalyInformation(66f, "super bad", time2, (long) 1),
                new AnomalyInformation(33f, "bad", time2, (long) 1)));

    }

    private static class CollectSink implements SinkFunction<Tuple2<AnomalyInformation, AnomalyInformation>> {
        public static final List<Tuple2<AnomalyInformation, AnomalyInformation>> anomalyInfoList = new ArrayList<>();

        @Override
        public synchronized void invoke(Tuple2<AnomalyInformation, AnomalyInformation> anomalyInformation, Context context) {
            anomalyInfoList.add(anomalyInformation);
        }
    }
}