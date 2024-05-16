package sp.pipeline.scorecalculators.components;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.operators.KeyedProcessOperator;
import org.apache.flink.streaming.api.operators.StreamFlatMap;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SampleStatefulMapFunctionTest {

    private SampleStatefulMapFunction sampleStatefulMapFunction;
    private KeyedOneInputStreamOperatorTestHarness<String, AISSignal, AnomalyInformation> testHarness;

    @BeforeEach
    void setUp() throws Exception {
        sampleStatefulMapFunction = new SampleStatefulMapFunction();

        testHarness = new KeyedOneInputStreamOperatorTestHarness<>(
                new StreamMap<>(sampleStatefulMapFunction),
                AISSignal::getShipHash,
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
        AISSignal signal1 = new AISSignal("ship1", 1, 2, 3, 4, 5, "time1", "port1");
        AISSignal signal2 = new AISSignal("ship1", 10, 20, 30, 40, 50, "time2", "port1");
        AISSignal signal3 = new AISSignal("ship2", 100, 200, 300, 400, 500, "time3", "port2");

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
                new AnomalyInformation(1, "", "time1", "ship1"),
                results.get(0)
        );

        // Check second result. ship1 again
        assertEquals(
                new AnomalyInformation(2, "", "time2", "ship1"),
                results.get(1)
        );

        // Check third result. ship2
        assertEquals(
                new AnomalyInformation(1, "", "time3", "ship2"),
                results.get(2)
        );
    }

}