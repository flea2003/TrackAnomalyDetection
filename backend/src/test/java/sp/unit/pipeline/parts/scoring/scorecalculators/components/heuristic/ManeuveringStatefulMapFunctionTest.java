package sp.unit.pipeline.parts.scoring.scorecalculators.components.heuristic;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.pipeline.parts.scoring.scorecalculators.components.heuristic.ManeuveringStatefulMapFunction;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManeuveringStatefulMapFunctionTest {

    private ManeuveringStatefulMapFunction maneuveringStatefulMapFunction;
    private KeyedOneInputStreamOperatorTestHarness<Long, AISSignal, AnomalyInformation> testHarness;

    @BeforeEach
    void setUp() throws Exception {
        maneuveringStatefulMapFunction = new ManeuveringStatefulMapFunction();

        testHarness = new KeyedOneInputStreamOperatorTestHarness<>(
                new StreamMap<>(maneuveringStatefulMapFunction),
                AISSignal::getId,
                Types.LONG
        );

        testHarness.open();
    }

    @AfterEach
    void tearDown() throws Exception {
        testHarness.close();
        testHarness = null;
        maneuveringStatefulMapFunction = null;
    }

    @Test
    void shipIsAnomalousAfterTenManeuvers() throws Exception {
        // prepare the list
        List<AISSignal> signals = getAlternatingSignals(12, List.of(-20f, 21f));
        signals.add(new AISSignal(
                1L, 0f, 0f, 0f, 0f, 100f,
                OffsetDateTime.parse("2024-12-30T05:02Z"), "port"));

        // execute the testHarness
        for (int i = 0; i < signals.size(); i++) {
            testHarness.processElement(signals.get(i), i);
        }

        // assert the result
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies).hasSize(13);

        // first 11 should not be anomalies
        for (int i = 0; i < 11; i++) {
            assertThat(anomalies.get(i).getValue().getScore()).isEqualTo(0f);
        }

        // the 12th one is an anomaly
        assertThat(anomalies.get(11).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(11).getValue().getExplanation()).isEqualTo(
                "Maneuvering is too frequent: 11 strong turns (turns of more than 40 degrees) during the last 60 minutes is more than threshold of 10 turns.\n");

        // the 13th one is not anomaly (happens one hour later)
        assertThat(anomalies.get(12).getValue().getScore()).isEqualTo(0f);
    }

    @Test
    void testConcatenationOfHeadingDifferences() throws Exception {
        // prepare the list
        // the signals in this list will have the headings as follows:
        // -20f, 0f, 21f, 20f, 21f,  -20f, 0f, 21f, 20f, 21f,  -20f, 0f, 21f, 20f, 21f,
        // -20f, 0f, 21f, 20f, 21f,  -20f, 0f, 21f, 20f, 21f,  -20f, 0f, 21f
        List<AISSignal> signals = getAlternatingSignals(28, List.of(-20f, 0f, 21f, 20f, 21f));

        // execute the testHarness
        for (int i = 0; i < signals.size(); i++) {
            testHarness.processElement(signals.get(i), i);
        }

        // assert the result
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies).hasSize(28);

        // first 27 should not be anomalies
        for (int i = 0; i < 27; i++) {
            assertThat(anomalies.get(i).getValue().getScore()).isEqualTo(0f);
        }

        // the 28th one is an anomaly
        assertThat(anomalies.get(27).getValue().getScore()).isEqualTo(25f);
        assertThat(anomalies.get(27).getValue().getExplanation()).isEqualTo(
                "Maneuvering is too frequent: 11 strong turns (turns of more than 40 degrees) during the last 60 minutes is more than threshold of 10 turns.\n");
    }

    @Test
    void noHeadingsShouldBeSkipped() throws Exception {
        // The list will have alternating headings between 511 (NO_HEADING value) and 50
        List<AISSignal> signals = getAlternatingSignals(12, List.of(511f, 50f));

        // execute the testHarness
        for (int i = 0; i < signals.size(); i++) {
            testHarness.processElement(signals.get(i), i);
        }

        // assert the result
        var anomalies = testHarness.extractOutputStreamRecords();

        assertThat(anomalies).hasSize(12);

        // all should not be anomalies (since 511 skipped)
        for (int i = 0; i < 12; i++) {
            assertThat(anomalies.get(i).getValue().getScore()).isEqualTo(0f);
        }
    }

    // helper method to prepare AIS signals that are the same but which have the alternating heading
    private List<AISSignal> getAlternatingSignals(int count, List<Float> alternatingHeadings) {
        List<AISSignal> signals = new ArrayList<>();
        OffsetDateTime timestamp = OffsetDateTime.parse("2024-12-30T04:00Z");

        for (int i = 0; i < count; i++) {
            timestamp = timestamp.plusMinutes(1); // increase timestamp by 1 minute from previous one
            float curHeading = alternatingHeadings.get(i % alternatingHeadings.size());

            AISSignal signal = new AISSignal(1L, 0f, 0f, 0f, 0f, curHeading, timestamp, "port");
            signals.add(signal);
        }
        return signals;
    }

    // test only in the past hour
    // test connections of maneuvers

}