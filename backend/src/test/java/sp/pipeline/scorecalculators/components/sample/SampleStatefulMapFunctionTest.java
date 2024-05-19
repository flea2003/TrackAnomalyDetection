package sp.pipeline.scorecalculators.components.sample;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.AISSignal;
import sp.dtos.AnomalyInformation;


import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SampleStatefulMapFunctionTest {

    private SampleStatefulMapFunction sampleStatefulMapFunction;
    private KeyedOneInputStreamOperatorTestHarness<Long, AISSignal, AnomalyInformation> testHarness;

    private final OffsetDateTime time1 = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() throws Exception {
        sampleStatefulMapFunction = new SampleStatefulMapFunction();

        testHarness = new KeyedOneInputStreamOperatorTestHarness<>(
                new StreamMap<>(sampleStatefulMapFunction),
                AISSignal::getId,
                TypeInformation.of(new TypeHint<>() {})
        );

        testHarness.open();
    }

    @AfterEach
    void tearDown() throws Exception {
        testHarness.close();
        testHarness = null;
        sampleStatefulMapFunction = null;
    }

    @Test
    void testSimpleMapping() throws Exception {
        // Simple signals. signal1 and signal2 have the same ship hash
        AISSignal signal1 = new AISSignal(1L, 1, 2, 3, 4, 5, time1, "port1");
        AISSignal signal2 = new AISSignal(1L, 10, 20, 30, 40, 50, time1, "port1");
        AISSignal signal3 = new AISSignal(2L, 100, 200, 300, 400, 500, time1, "port2");

        // Process elements
        testHarness.processElement(signal1, 1L);
        testHarness.processElement(signal2, 2L);
        testHarness.processElement(signal3, 3L);

        // Retrieve the results
        List<AnomalyInformation> results = new ArrayList<>();
        testHarness.getOutput().forEach(record -> {
            @SuppressWarnings("unchecked")
            StreamRecord<AnomalyInformation> streamRecord = (StreamRecord<AnomalyInformation>) record;
            results.add(streamRecord.getValue());
        });

        // Verify
        // There should be three results
        assertEquals(3, results.size());

        // Check first result. ship1
        assertEquals(
                new AnomalyInformation(1, "", time1, 1L),
                results.get(0)
        );

        // Check second result. ship1 again
        assertEquals(
                new AnomalyInformation(2, "", time1, 1L),
                results.get(1)
        );

        // Check third result. ship2
        assertEquals(
                new AnomalyInformation(1, "", time1, 2L),
                results.get(2)
        );
    }

}