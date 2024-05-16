package sp.pipeline.scorecalculators.components.heuristic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.time.OffsetDateTime;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

public class SpeedStatefulMapFunctionTest {

    private SpeedStatefulMapFunction speedStatefulMapFunction;

    @Test
    void testOneNonAnomalousSignal() {
        // Only one signal is received, and it can't be anomalous on it's own.

        speedStatefulMapFunction = new SpeedStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        AISSignal aisSignal1 = new AISSignal("1", 20, 10, 10, 20, 20, timestamp1, "Malta");

        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(speedStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 10);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's speed is great.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }

    @Test
    void testTwoNonAnomalousSignal() {
        // There are two signals but they are not anomalous
        speedStatefulMapFunction = new SpeedStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:00Z");
        AISSignal aisSignal1 = new AISSignal("1", 20, 90, 30, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal("1", 22, 11, 10, 20, 20, timestamp2, "Malta");
        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(speedStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 20);
            testHarness.processElement(aisSignal2, 60);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's speed is great.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The ship's speed is anomalous.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }


    @Test
    void anomalyExpires() {
        // The first signal received is not anomalous
        // The second signal received is anomalous since the difference between the computed speed and actual speed
        // is slightly greater than 10
        // The third signal is not anomalous since more than 30 minutes have passed since it was received.
        speedStatefulMapFunction = new SpeedStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        System.out.println(timestamp1);
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        System.out.println(timestamp2);
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T05:33Z");
        System.out.println(timestamp3);
        AISSignal aisSignal1 = new AISSignal("1", 20, 60, 60, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal("1", 22, 11, 10, 20, 20, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal("1", 22, 11, 10, 20, 20, timestamp3, "Malta");
        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(speedStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 20);
            testHarness.processElement(aisSignal2, 60);
            testHarness.processElement(aisSignal3, 91);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's speed is great.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The ship's speed is anomalous.");
            assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("The ship's speed is great.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage());
        }

    }



}
