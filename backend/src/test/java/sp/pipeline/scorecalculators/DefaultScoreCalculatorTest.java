package sp.pipeline.scorecalculators;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultScoreCalculatorTest {

    // Tests based on https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/testing/

    @Test
    void testSetupFlinkAnomalyScoreCalculationPart() throws Exception {
        // create initial AISSignal objects
        // ais1 and ais2 are from the same ship
        AISSignal ais1 = new AISSignal("ship1", 1, 2, 3, 4, 5, "time1", "port1");
        AISSignal ais2 = new AISSignal("ship1", 10, 20, 30, 40, 50, "time2", "port1");
        AISSignal ais3 = new AISSignal("ship2", 100, 200, 300, 400, 500, "time3", "port2");

        // prepare flink environment and streams
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<AISSignal> aisStream = env.fromCollection(List.of(ais1, ais2, ais3));

        DefaultScoreCalculator scoreCalculator = new DefaultScoreCalculator();
        DataStream<AnomalyInformation> resultStream = scoreCalculator.setupFlinkAnomalyScoreCalculationPart(aisStream);

        CollectSink.anomalyInfoList.clear();
        resultStream.addSink(new CollectSink());
        env.execute();

        // verify result
        List<AnomalyInformation> result = CollectSink.anomalyInfoList;
        assertTrue(result.containsAll(List.of(
                new AnomalyInformation(2, "", "time2", "ship1"),
                new AnomalyInformation(1, "", "time3", "ship2")
        )));
    }

    private static class CollectSink implements SinkFunction<AnomalyInformation> {
        public static final List<AnomalyInformation> anomalyInfoList = new ArrayList<>();

        @Override
        public synchronized void invoke(AnomalyInformation anomalyInformation, Context context) throws Exception {
            anomalyInfoList.add(anomalyInformation);
        }
    }
}