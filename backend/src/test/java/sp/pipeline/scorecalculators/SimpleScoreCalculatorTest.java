package sp.pipeline.scorecalculators;

import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import sp.model.AISSignal;
import sp.dtos.AnomalyInformation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SimpleScoreCalculatorTest {

    // Tests based on https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/testing/

    private final OffsetDateTime time1 = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));
    private final OffsetDateTime time2 = OffsetDateTime.of(2004, 1, 27, 1,15,0,0, ZoneOffset.ofHours(0));
    private final OffsetDateTime time3 = OffsetDateTime.of(2004, 1, 27, 1,17,0,0, ZoneOffset.ofHours(0));


    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
        new MiniClusterWithClientResource(
            new MiniClusterResourceConfiguration.Builder()
                .setNumberSlotsPerTaskManager(2)
                .setNumberTaskManagers(1)
                .build());

    @Test
    void testSetupFlinkAnomalyScoreCalculationPart() throws Exception {
        // create initial AISSignal objects
        // ais1 and ais2 are from the same ship
        AISSignal ais1 = new AISSignal(1, 1, 2, 3, 4, 5, time1, "port1");
        AISSignal ais2 = new AISSignal(1, 5, 20, 30, 20, 10, time2, "port1");
        AISSignal ais3 = new AISSignal(1, 5, 20, 70, 0, 50, time3, "port1");
        AISSignal ais4 = new AISSignal(2, 3, 200, 300, 400, 500, time3, "port2");

        // prepare flink environment and streams
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<AISSignal> aisStream = env.fromData(List.of(ais1, ais2, ais3, ais4));

        ScoreCalculationStrategy scoreCalculator = new SimpleScoreCalculator();
        DataStream<AnomalyInformation> resultStream = scoreCalculator.setupFlinkAnomalyScoreCalculationPart(aisStream);

        CollectSink.anomalyInfoList.clear();
        resultStream.addSink(new CollectSink());
        env.execute();
        env.close();

        // verify result
        List<AnomalyInformation> result = CollectSink.anomalyInfoList;

        assertThat(result).containsAll(List.of(
            new AnomalyInformation(100f, "The time difference between consecutive AIS signals is anomalous." +
                "The ship's speed is anomalous." +
                "The ship's turning direction is anomalous.", time3, (long)1),
            new AnomalyInformation(0.0f, "The time difference between consecutive AIS signals is ok." +
                "The ship's speed is ok." +
                "The ship's turning direction is ok.", time3, (long)2)
        ));

    }

    private static class CollectSink implements SinkFunction<AnomalyInformation> {
        public static final List<AnomalyInformation> anomalyInfoList = new ArrayList<>();

        @Override
        public synchronized void invoke(AnomalyInformation anomalyInformation, Context context) {
            anomalyInfoList.add(anomalyInformation);
        }
    }
}