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
import sp.pipeline.parts.scoring.scorecalculators.components.heuristic.SpeedStatefulMapFunction;

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
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
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
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Speed is too big: 840.06 km/min is faster than threshold of 55.5 km/min.
                        Speed is inaccurate: the approximated speed of 840.06 km/min is different from reported speed of 22 km/min by more than allowed margin of 10 km/min.
                        """
        );
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
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");
        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Speed is inaccurate: the approximated speed of 2.74 km/min is different from reported speed of 12.8 km/min by more than allowed margin of 10 km/min.
                        """
        );
        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo("");
    }

    @Test
    void anomalousAcceleration() throws Exception {
        OffsetDateTime timestamp1 = OffsetDateTime.parse("2024-12-30T04:50Z");
        OffsetDateTime timestamp2 = OffsetDateTime.parse("2024-12-30T04:51Z");
        OffsetDateTime timestamp3 = OffsetDateTime.parse("2024-12-30T04:52Z");
        AISSignal aisSignal1 = new AISSignal(1, 10f, 10, 10, 20, 20, timestamp1, "Malta");
        AISSignal aisSignal2 = new AISSignal(1, 60.001f, 10, 10, 20, 20, timestamp2, "Malta");
        AISSignal aisSignal3 = new AISSignal(1, 110f, 10, 10, 20, 20, timestamp3, "Malta");

        testHarness.processElement(aisSignal1, 20);
        testHarness.processElement(aisSignal2, 60);
        testHarness.processElement(aisSignal3, 61);
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies.size()).isEqualTo(3);

        assertThat(anomalies.get(0).getValue().getScore()).isEqualTo(0.0f);
        assertThat(anomalies.get(0).getValue().getExplanation()).isEqualTo("");

        assertThat(anomalies.get(1).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(1).getValue().getExplanation()).isEqualTo(
                """
                        Speed is inaccurate: the approximated speed of 0 km/min is different from reported speed of 60 km/min by more than allowed margin of 10 km/min.
                        Acceleration is too big: 50 km/min^2 is bigger than threshold of 50 km/min^2.
                        """
        );

        assertThat(anomalies.get(2).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(2).getValue().getExplanation()).isEqualTo(
                """
                        Speed is inaccurate: the approximated speed of 0 km/min is different from reported speed of 110 km/min by more than allowed margin of 10 km/min.
                        """
        );
    }

}
