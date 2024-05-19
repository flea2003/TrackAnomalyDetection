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

import static org.junit.jupiter.api.Assertions.*;

class DefaultScoreCalculatorTest {

    // Tests based on https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/testing/

    private final OffsetDateTime time1 = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));

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
        AISSignal ais1 = new AISSignal(1L, 1, 2, 3, 4, 5, time1, "port1");
        AISSignal ais2 = new AISSignal(1L, 10, 20, 30, 40, 50, time1, "port1");
        AISSignal ais3 = new AISSignal(2L, 100, 200, 300, 400, 500, time1, "port2");

        // prepare flink environment and streams
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<AISSignal> aisStream = env.fromData(List.of(ais1, ais2, ais3));

        DefaultScoreCalculator scoreCalculator = new DefaultScoreCalculator();
        DataStream<AnomalyInformation> resultStream = scoreCalculator.setupFlinkAnomalyScoreCalculationPart(aisStream);

        CollectSink.anomalyInfoList.clear();
        resultStream.addSink(new CollectSink());
        env.execute();
        env.close();

        // verify result
        List<AnomalyInformation> result = CollectSink.anomalyInfoList;
        assertTrue(result.containsAll(List.of(
                new AnomalyInformation(2, "", time1, 1L),
                new AnomalyInformation(1, "", time1, 2L)
        )));
    }

    private static class CollectSink implements SinkFunction<AnomalyInformation> {
        public static final List<AnomalyInformation> anomalyInfoList = new ArrayList<>();

        @Override
        public synchronized void invoke(AnomalyInformation anomalyInformation, Context context) {
            anomalyInfoList.add(anomalyInformation);
        }
    }
}