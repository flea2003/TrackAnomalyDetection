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

public class TurningStatefulMapFunctionTest {

    private TurningStatefulMapFunction turningStatefulMapFunction;

    @Test
    void testOneNonAnomalousSignal() {

        turningStatefulMapFunction = new TurningStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        AISSignal aisSignal1 = new AISSignal("1", 20, 10, 10, 20, 20, timestamp1, "Malta");

        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(turningStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 10);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's turning direction is ok.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }

    @Test
    void testTwoNonAnomalousSignal() {

        turningStatefulMapFunction = new TurningStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        AISSignal aisSignal1 = new AISSignal("1", 20, 90, 30, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal("1", 22, 11, 10, 60, 20, timestamp2, "Malta");
        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(turningStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 20);
            testHarness.processElement(aisSignal2, 60);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's turning direction is ok.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(34.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The ship's turning direction is anomalous.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }


    @Test
    void anomalyExpires() {

        turningStatefulMapFunction = new TurningStatefulMapFunction();
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T05:33Z");
        AISSignal aisSignal1 = new AISSignal("1", 12.8f, 10, 10, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal("1", 12.8f, 11, 10, 61, 20, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal("1", 0.0f, 11, 10, 61, 20, timestamp3, "Malta");
        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(turningStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 20);
            testHarness.processElement(aisSignal2, 60);
            testHarness.processElement(aisSignal3, 91);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's turning direction is ok.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(34.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The ship's turning direction is anomalous.");
            assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("The ship's turning direction is ok.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }

    }

}
