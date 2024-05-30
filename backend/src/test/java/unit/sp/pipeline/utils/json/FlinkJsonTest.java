package unit.sp.pipeline.utils.json;

import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.AISSignal;
import sp.pipeline.utils.json.FlinkJson;
import sp.pipeline.utils.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FlinkJsonTest {

    StreamExecutionEnvironment env;
    AISSignal ais;

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(2)
                            .setNumberTaskManagers(1)
                            .build());

    @BeforeEach
    void setUp() {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        StringCollectSink.result.clear();
        AISCollectSink.result.clear();

        ais = new AISSignal(2, 3, 200, 300, 400, 500,
                OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0)),
                "port2");
    }

    @AfterEach
    void tearDown() {
        env = null; // needs to be closed before asserting the result
        StringCollectSink.result.clear();
        AISCollectSink.result.clear();

        ais = null;
    }

    @Test
    void constructorTest() {
        FlinkJson flinkJson = new FlinkJson();
        assertNotNull(flinkJson);
    }

    @Test
    void testToJson() throws Exception {
        DataStream<AISSignal> aisStream = env.fromData(List.of(ais));
        DataStream<String> resultStream = FlinkJson.serialize(aisStream);
        resultStream.addSink(new StringCollectSink());
        env.execute();
        env.close();

        // verify result
        List<String> result = StringCollectSink.result;
        assertThat(result).containsExactly(JsonMapper.toJson(ais));
    }

    @Test
    void testFromJson() throws Exception {
        DataStream<String> aisStream = env.fromData(List.of(
                JsonMapper.toJson(ais), // good AIS signal
                "bad AIS signal here, the result should ignore it"
        ));
        DataStream<AISSignal> resultStream = FlinkJson.deserialize(aisStream, AISSignal.class);
        resultStream.addSink(new AISCollectSink());
        env.execute();
        env.close();

        // verify result
        List<AISSignal> result = AISCollectSink.result;
        assertThat(result).containsExactly(ais);
    }

    @Test
    void testToJsonBad() throws Exception {
        DataStream<TestObject> aisStream = env.fromData(List.of(
                new TestObject()
        ));
        DataStream<String> resultStream = FlinkJson.serialize(aisStream);
        resultStream.addSink(new StringCollectSink());
        env.execute();
        env.close();

        // verify result
        List<String> result = StringCollectSink.result;
        assertThat(result).isEmpty();
    }

    private static class StringCollectSink implements SinkFunction<String> {
        public static final List<String> result = new ArrayList<>();

        @Override
        public synchronized void invoke(String anomalyInformation, Context context) {
            result.add(anomalyInformation);
        }
    }

    private static class AISCollectSink implements SinkFunction<AISSignal> {
        public static final List<AISSignal> result = new ArrayList<>();

        @Override
        public synchronized void invoke(AISSignal anomalyInformation, Context context) {
            result.add(anomalyInformation);
        }
    }

    private static class TestObject {
        private final int id;

        public TestObject() {
            id = 1;
        }

        @Override
        public String toString() {return Integer.toString(id);}
    }

}