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
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is ok.\n");
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
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is ok.\n");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is ok.\n");
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
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is ok.\n");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Time between two signals is too large: 11 minutes is more than threshold 10 minutes,  and ship travelled too much between signals: 37448.13 is more than threshold 6.0.
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
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is ok.\n");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Time between two signals is too large: 11 minutes is more than threshold 10 minutes,  and ship travelled too much between signals: 597.3 is more than threshold 6.0.
                        """
        );
        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is ok.\n");
    }

//    @Test
//    void anomalyExpiresEdgeCases() throws Exception {
//        // This test is for killing the mutant that changes the boundary of 30 minutes.
//
//        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
//        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:20Z"); // 30 minutes later (edge)
//        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T05:50Z"); // 30 minutes later (edge)
//        OffsetDateTime timestamp4 = OffsetDateTime.parse("2024-12-30T06:20Z"); // 30 minutes later (edge)
//        OffsetDateTime timestamp5 = OffsetDateTime.parse("2024-12-30T06:21Z"); // 1 minutes later (edge)
//
//        AISSignal aisSignal1 = new AISSignal(1, 12.8f, 10, 10, 20, 20, timestamp1, "Malta");
//        AISSignal aisSignal2 = new AISSignal(1, 12.8f, 11, 10, 20, 20, timestamp2, "Malta");
//        AISSignal aisSignal3 = new AISSignal(1, 12.8f, 12, 10, 20, 20, timestamp3, "Malta");
//        AISSignal aisSignal4 = new AISSignal(1, 12.8f, 12, 10, 20, 20, timestamp4, "Malta");
//        AISSignal aisSignal5 = new AISSignal(1, 12.8f, 12, 10, 20, 20, timestamp5, "Malta");
//
//        testHarness.processElement(aisSignal1, 1);
//        testHarness.processElement(aisSignal2, 2);
//        testHarness.processElement(aisSignal3, 3);
//        testHarness.processElement(aisSignal4, 4);
//        testHarness.processElement(aisSignal5, 5);
//        var anomalies = testHarness.extractOutputStreamRecords();
//
//        assertThat(anomalies.size()).isEqualTo(5);
//
//        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
//        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is ok.\n");
//
//        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(33.0f);
//        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is anomalous.");
//
//        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(33.0f);
//        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is anomalous.");
//
//        assertThat(anomalies.get(3).getValue().getScore()).isEqualTo(33.0f);
//        assertThat(anomalies.get(3).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is anomalous.");
//
//        assertThat(anomalies.get(4).getValue().getScore()).isEqualTo(0.0f);
//        assertThat(anomalies.get(4).getValue().getExplanation()).isEqualTo("The time difference between consecutive AIS signals is ok.");
//    }

}
