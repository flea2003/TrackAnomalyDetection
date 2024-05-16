package sp.pipeline.scorecalculators.components.heuristic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.dtos.Timestamp;

public class TurningStatefulMapFunctionTest {

    private TurningStatefulMapFunction turningStatefulMapFunction;

    @Test
    void testOneNonAnomalousSignal() { // More descriptive name

        turningStatefulMapFunction = new TurningStatefulMapFunction();
        Timestamp timestamp1 = new Timestamp("30/12/2024 04:50");
        Timestamp timestamp2 = new Timestamp("30/12/2024 04:52");
        AISSignal aisSignal1 = new AISSignal("1", 20, 10, 10, 20, 20, timestamp1, "Malta");

        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(turningStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 10);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's turning direction is great.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }

    @Test
    void testTwoNonAnomalousSignal() { // More descriptive name

        turningStatefulMapFunction = new TurningStatefulMapFunction();
        Timestamp timestamp1 = new Timestamp("30/12/2024 04:50");
        Timestamp timestamp2 = new Timestamp("30/12/2024 05:30");
        AISSignal aisSignal1 = new AISSignal("1", 12.8f, 10, 10, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal("1", 12.8f, 11, 50, 51, 80, timestamp2, "Malta");
        try {
            OneInputStreamOperatorTestHarness<AISSignal, AnomalyInformation> testHarness =
                new KeyedOneInputStreamOperatorTestHarness<>(new StreamMap<>(turningStatefulMapFunction), x -> "1", Types.STRING);
            testHarness.open();
            testHarness.processElement(aisSignal1, 20);
            testHarness.processElement(aisSignal2, 60);
            var anomalies = testHarness.extractOutputStreamRecords();
            assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's turning direction is great.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(34.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The ship's turning direction is anomalous.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }
    }


    @Test
    void anomalyExpires() { // More descriptive name

        turningStatefulMapFunction = new TurningStatefulMapFunction();
        Timestamp timestamp1 = new Timestamp("30/12/2024 04:50");
        Timestamp timestamp2 = new Timestamp("30/12/2024 05:30");
        Timestamp timestamp3 = new Timestamp("30/12/2024 06:01");
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
            assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The ship's turning direction is great.");
            assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(34.0f);
            assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The ship's turning direction is anomalous.");
            assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
            assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("The ship's turning direction is great.");
        } catch (Exception e) {
            fail("Exception during setup: " + e.getMessage()); // More specific fail message
        }

    }

}
