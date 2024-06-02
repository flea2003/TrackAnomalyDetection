package sp.unit.pipeline.parts.scoring.scorecalculators.components.heuristic;

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
import sp.pipeline.parts.scoring.scorecalculators.components.heuristic.TurningStatefulMapFunction;

public class TurningStatefulMapFunctionTest {

    private TurningStatefulMapFunction turningStatefulMapFunction;
    private KeyedOneInputStreamOperatorTestHarness<Long, AISSignal, AnomalyInformation> testHarness;

    @BeforeEach
    void setUp() throws Exception {
        turningStatefulMapFunction = new TurningStatefulMapFunction();

        testHarness = new KeyedOneInputStreamOperatorTestHarness<>(
                new StreamMap<>(turningStatefulMapFunction),
                AISSignal::getId,
                Types.LONG
        );

        testHarness.open();
    }

    @AfterEach
    void tearDown() throws Exception {
        testHarness.close();
        testHarness = null;
        turningStatefulMapFunction = null;
    }

    @Test
    void testOneNonAnomalousSignal() throws Exception {

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

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        AISSignal aisSignal1 = new AISSignal(1, 20, 90, 30, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 22, 11, 10, 61, 20, timestamp2, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 60);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(2);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Course difference between two consecutive signals is too large: 41 degrees is more than threshold of 40 degrees.
                        """
        );
    }


    @Test
    void anomalyExpires() throws Exception {

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T05:01Z");
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T05:33Z");
        AISSignal aisSignal1 = new AISSignal(1, 12.8f, 10, 10, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 12.8f, 11, 10, 61, 20, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal(1, 0.0f, 11, 10, 61, 20, timestamp3, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 60);
        testHarness.processElement(aisSignal3, 91);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(3);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Course difference between two consecutive signals is too large: 41 degrees is more than threshold of 40 degrees.
                        """
        );
        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("");
    }

    @Test
    void testHeading511() throws Exception {
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T06:00Z");
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T07:00Z");

        // Heading 511 in aisSignal1 and aisSignal3
        AISSignal aisSignal1 = new AISSignal(1, 20, 10, 10, 20, 511, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 20, 10, 10, 20, 22, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal(1, 20, 10, 10, 20, 511, timestamp2, "Malta");

        testHarness.processElement(aisSignal1, 10);
        testHarness.processElement(aisSignal2, 12);
        testHarness.processElement(aisSignal3, 13);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(3);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("Heading was not given (value 511).\n");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(0f);
        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("Heading was not given (value 511).\n");
    }

    @Test
    void testAnomalyTimeThreshold() throws Exception {
        // non-anomaly signal is still considered as an anomaly up to after 30 minutes
        // after the last anomaly
        // This tests is a boundary check testing for this.

        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T03:59Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T04:00Z"); // anomalous signal
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T04:30Z"); // 30 minutes after anomalous signal
        OffsetDateTime timestamp4 = OffsetDateTime.parse("2024-12-30T04:31Z"); // 31 minutes after anomalous signal

        AISSignal aisSignal1 = new AISSignal(1, 20, 10, 10, 20, 10, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 20, 10, 10, 160, 10, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal(1, 20, 10, 10, 160, 10, timestamp3, "Malta");
        AISSignal aisSignal4 = new AISSignal(1, 20, 10, 10, 160, 10, timestamp4, "Malta");

        testHarness.processElement(aisSignal1, 1);
        testHarness.processElement(aisSignal2, 2);
        testHarness.processElement(aisSignal3, 3);
        testHarness.processElement(aisSignal4, 4);

        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(4);

        // third one is anomaly since it's 30 minutes after last detected anomaly
        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo(
                """
                        Course difference between two consecutive signals is too large: 140 degrees is more than threshold of 40 degrees.
                        """
        );

        // fourth one is non-anomaly since it's >30 minutes after last detected anomaly
        assertThat(anomalies.get(3).getValue().getScore()).isEqualTo(0f);
        assertThat(anomalies.get(3).getValue().getExplanation()).isEqualTo("");
    }

    @Test
    void testHeadingDifference() throws Exception {
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T04:51Z");

        // signals only differ in headings
        AISSignal aisSignal1 = new AISSignal(1, 20, 10, 10, 20, 0, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 20, 10, 10, 20, 41, timestamp2, "Malta");

        testHarness.processElement(aisSignal1, 1);
        testHarness.processElement(aisSignal2, 2);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(2);
        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0f);
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                "Heading difference between two consecutive signals is too large: 41 degrees is more than threshold of 40 degrees.\n"
        );
    }

}
