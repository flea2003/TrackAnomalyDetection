package sp.pipeline.scorecalculators.components.heuristic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;

public class SignalStatefulMapFunctionTest {

    private SignalStatefulMapFunction signalStatefulMapFunction;

    @Test
    void testOneNonAnomalousSignal() {
        // There is only one signal so it is not anomalous
        signalStatefulMapFunction = new SignalStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        AISSignal aisSignal1 = new AISSignal("1", 20, 10, 10, 20, 20, timestamp1, "Malta");

        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(signalStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 10);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is great.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }

    @Test
    void testTwoNonAnomalousSignal() {
        // There are two signals but they are not anomalous
        signalStatefulMapFunction = new SignalStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:00Z");
        AISSignal aisSignal1 = new AISSignal("1", 20, 90, 30, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal("1", 22, 11, 10, 20, 20, timestamp2, "Malta");
        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(signalStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 20);
            testHarness.processElement(aisSignal2, 40);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is great.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is great.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }


    @Test
    void secondAnomalousSignal() {
        // The second signal is anomalous and the first one isn't
        // Reason: the difference is time of the signal is big and the globe distance is enormous
        signalStatefulMapFunction = new SignalStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        AISSignal aisSignal1 = new AISSignal("1", 20, 60, 60, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal("1", 22, 11, 10, 20, 20, timestamp2, "Malta");
        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(signalStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 20);
            testHarness.processElement(aisSignal2, 31);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is great.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is anomalous.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }

    @Test
    void anomalyExpires() {
        // The second signal is anomalous because the time difference between signals is big and the distance travelled also
        // The third signal is not anomalous since more than 30 minutes past from the previous signal.
        signalStatefulMapFunction = new SignalStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T05:33Z");
        AISSignal aisSignal1 = new AISSignal("1", 12.8f, 10, 10, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal("1", 12.8f, 11, 10, 20, 20, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal("1", 0.0f, 11, 10, 20, 20, timestamp3, "Malta");
        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(signalStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 20);
            testHarness.processElement(aisSignal2, 31);
            testHarness.processElement(aisSignal3, 63);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is great.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is anomalous.");
            assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is great.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }


}
