package sp.pipeline.scorecalculators.components.heuristic;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.AISSignal;
import sp.dtos.AnomalyInformation;

public class SpeedStatefulMapFunctionTest {

    private SpeedStatefulMapFunction speedStatefulMapFunction;
    private KeyedOneInputStreamOperatorTestHarness<Long, AISSignal, AnomalyInformation> testHarness;

    @BeforeEach
    void setUp() throws Exception {
        speedStatefulMapFunction = new SpeedStatefulMapFunction();

        testHarness = new KeyedOneInputStreamOperatorTestHarness<>(
                new StreamMap<>(speedStatefulMapFunction),
                AISSignal::getId,
                Types.LONG
        );

        testHarness.open();
    }

    @AfterEach
    void tearDown() throws Exception {
        testHarness.close();
        testHarness = null;
        speedStatefulMapFunction = null;
    }

    @Test
    void testOneNonAnomalousSignal() throws Exception {
        // Only one signal is received, and it can't be anomalous on its own.

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        AISSignal aisSignal1 = new AISSignal(1, 20, 10, 10, 20, 20, timestamp1, "Malta");

        testHarness.processElement(aisSignal1, 10);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(1);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's speed is ok.");
    }

    @Test
    void testTwoNonAnomalousSignal() throws Exception {
        // There are two signals but they are not anomalous

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:00Z");
        AISSignal aisSignal1 = new AISSignal(1, 20, 90, 30, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 22, 11, 10, 20, 20, timestamp2, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 60);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(2);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's speed is ok.");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The ship's speed is anomalous.");
    }


    @Test
    void anomalyExpires() throws Exception {
        // The first signal received is not anomalous
        // The second signal received is anomalous since the difference between the computed speed and actual speed
        // is slightly greater than 10
        // The third signal is not anomalous since more than 30 minutes have passed since it was received.

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:30Z");
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T06:01Z");
        AISSignal aisSignal1 = new AISSignal(1, 12.8f, 10, 10, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 12.8f, 11, 10, 20, 20, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal(1, 0.0f, 11, 10, 20, 20, timestamp3, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 60);
        testHarness.processElement(aisSignal3, 91);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(3);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's speed is ok.");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The ship's speed is anomalous.");
        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("The ship's speed is ok.");
    }

    @Test
    void testAccelerationIsTooBig() {
        assertThat(
                speedStatefulMapFunction.isAnomaly(0.0f, 0.0f, 100.0f)
        ).isTrue();
    }

}
