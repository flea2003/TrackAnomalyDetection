package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;

public class SignalStatefulMapFunctionTest {

    private SignalStatefulMapFunction signalStatefulMapFunction;
    private KeyedOneInputStreamOperatorTestHarness<Long, AISSignal, AnomalyInformation> testHarness;

    @BeforeEach
    void setUp() throws Exception {
        signalStatefulMapFunction = new SignalStatefulMapFunction();

        testHarness = new KeyedOneInputStreamOperatorTestHarness<>(
                new StreamMap<>(signalStatefulMapFunction),
                AISSignal::getId,
                Types.LONG
        );

        testHarness.open();
    }

    @AfterEach
    void tearDown() throws Exception {
        testHarness.close();
        testHarness = null;
        signalStatefulMapFunction = null;
    }

    @Test
    void testOneNonAnomalousSignal() throws Exception {
        // There is only one signal so it is not anomalous

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        AISSignal aisSignal1 = new AISSignal(1, 20, 10, 10, 20, 20, timestamp1, "Malta");

        testHarness.processElement(aisSignal1, 10);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(1);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
    }

    @Test
    void testTwoNonAnomalousSignal() throws Exception {
        // There are two signals but they are not anomalous

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T04:59Z");
        AISSignal aisSignal1 = new AISSignal(1, 20, 90, 30, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 22, 11, 10, 20, 20, timestamp2, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 40);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(2);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("");
    }


    @Test
    void secondAnomalousSignal() throws Exception {
        // The second signal is anomalous and the first one isn't
        // Reason: the difference is time of the signal is big and the globe distance is enormous

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        AISSignal aisSignal1 = new AISSignal(1, 20, 60, 60, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 22, 11, 10, 20, 20, timestamp2, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 31);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(2);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Time between two consecutive signals is too large: 11 minutes is more than threshold of 10 minutes, and ship's speed (between two signals) is too large: 37448.13 km/h is more than threshold of 6.0 km/h.
                        """
        );
    }

    @Test
    void anomalyExpires() throws Exception {
        // The second signal is anomalous because the time difference between signals is big and the distance travelled also
        // The third signal is not anomalous since more than 30 minutes past from the previous signal.

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T05:33Z");
        AISSignal aisSignal1 = new AISSignal(1, 12.8f, 10, 10, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 12.8f, 11, 10, 20, 20, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal(1, 0.0f, 11, 10, 20, 20, timestamp3, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 31);
        testHarness.processElement(aisSignal3, 63);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(3);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Time between two consecutive signals is too large: 11 minutes is more than threshold of 10 minutes, and ship's speed (between two signals) is too large: 597.3 km/h is more than threshold of 6.0 km/h.
                        """
        );
        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("");
    }

    @Test
    void testSignalTimeDiffBoundary() throws Exception {
        // boundary test for the constant SIGNAL_TIME_DIFF_THRESHOLD_IN_MINUTES

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:00Z");
        AISSignal aisSignal1 = new AISSignal(1, 12.8f, 10, 10, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 12.8f, 11, 10, 20, 20, timestamp2, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 31);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(2);

        // second signal is not an anomaly (happens only after 10 minutes after the first one)
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(0f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("");
    }

}
