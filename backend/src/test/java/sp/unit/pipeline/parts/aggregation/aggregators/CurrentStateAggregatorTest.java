package sp.unit.pipeline.parts.aggregation.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.*;
import sp.pipeline.utils.OffsetDateTimeSerializer;
import sp.pipeline.parts.aggregation.aggregators.CurrentStateAggregator;
import sp.pipeline.parts.scoring.scorecalculators.ScoreCalculationStrategy;
import sp.pipeline.parts.scoring.scorecalculators.SimpleScoreCalculator;
import org.apache.flink.api.common.ExecutionConfig;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CurrentStateAggregatorTest {

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(2)
                            .setNumberTaskManagers(1)
                            .build());


    private CurrentStateAggregator aggregator;

    private final OffsetDateTime timestamp = OffsetDateTime.of(2015, 4, 18, 1,1,0,0, ZoneOffset.ofHours(0));
    private final OffsetDateTime timestamp2 = OffsetDateTime.of(2016, 4, 18, 1,1,0,0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() {
        aggregator = new CurrentStateAggregator();
    }

    @AfterEach
    void tearDown() {
        aggregator = null;
    }

    @Test
    void detailsNotInitializedFirstNull() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(null, signal, maxInfo);
        assertTrue(aggregator.shipDetailsNotInitialized(details));
    }

    @Test
    void detailsNotInitializedSecondNull() {
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(info, null, maxInfo);
        assertTrue(aggregator.shipDetailsNotInitialized(details));
    }

    @Test
    void detailsNotInitializedAllNonNull() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(info, signal, maxInfo);
        assertFalse(aggregator.shipDetailsNotInitialized(details));
    }

    @Test
    void testEncapsulatesAnomalyWhenNull() {
        CurrentShipDetails details = new CurrentShipDetails(null, null, null);

        assertFalse(aggregator.encapsulatesAnomalyInformation(null, details));
    }

    @Test
    void testEncapsulatesAnomalyWhenNotFinalized() {
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, null, null);

        assertTrue(aggregator.encapsulatesAnomalyInformation(info, details));
    }

    @Test
    void testEncapsulatesAnomalyBasedOnTimestampsFalse() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(info, signal, maxInfo);

        assertFalse(aggregator.encapsulatesAnomalyInformation(info, details));
    }

    @Test
    void testEncapsulatesAnomalyBasedOnTimestampsTrue() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(info, signal, maxInfo);

        AnomalyInformation info2 = new AnomalyInformation(0.5f, "explain", timestamp2, 1L);
        assertTrue(aggregator.encapsulatesAnomalyInformation(info2, details));
    }

    @Test
    void testEncapsulatesAISWhenNull() {
        CurrentShipDetails details = new CurrentShipDetails(null, null, null);

        assertFalse(aggregator.encapsulatesAISSignal(null, details));
    }

    @Test
    void testEncapsulatesAISWhenNotFinalized() {
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(info, null, maxInfo);

        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        assertTrue(aggregator.encapsulatesAISSignal(signal, details));
    }

    @Test
    void testEncapsulatesAISBasedOnTimestampsFalse() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(info, signal, maxInfo);

        assertFalse(aggregator.encapsulatesAISSignal(signal, details));
    }

    @Test
    void testEncapsulatesAISBasedOnTimestampsTrue() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(info, signal, maxInfo);

        AISSignal signal2 = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp2, "port");
        assertTrue(aggregator.encapsulatesAISSignal(signal2, details));
    }


    private static class CollectSink implements SinkFunction<CurrentShipDetails> {
        public static final List<CurrentShipDetails> detailsList = new ArrayList<>();
        @Override
        public synchronized void invoke(CurrentShipDetails details, Context context) {
            detailsList.add(details);
        }
    }

    /**
     * Sends the provided signals to the mapping function and returns the last element of the returned stream.
     *
     * @param signalsToSend the signals to send to the mapping function
     * @return the last element of the returned stream
     * @throws Exception if the mapping function fails
     */
    CurrentShipDetails runTheMapping(List<ShipInformation> signalsToSend) throws Exception {
        // Set up a simple flink environment
        Configuration config = new Configuration();
        config.setString("pipeline.default-kryo-serializers",
                "class:java.time.OffsetDateTime,serializer:" + OffsetDateTimeSerializer.class.getName());
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);

        DataStream<ShipInformation> detailsStream = env.fromData(signalsToSend);

        DataStream<CurrentShipDetails> resultStream = detailsStream.keyBy(ShipInformation::getShipId).map(
                new CurrentStateAggregator()
        );
        CollectSink.detailsList.clear();
        resultStream.addSink(new CollectSink());
        env.execute();
        env.close();

        return CollectSink.detailsList.get(CollectSink.detailsList.size() - 1);
    }

    @Test
    void aggregateSignalsWithAnomalyFirst() throws Exception {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        ShipInformation shipInfoAnomaly = new ShipInformation(1L, info, null);
        ShipInformation shipInfoAIS = new ShipInformation(1L, null, signal);

        MaxAnomalyScoreDetails expectedMaxInfo = new MaxAnomalyScoreDetails(0.5F, timestamp);

        CurrentShipDetails receivedDetails = runTheMapping(List.of(shipInfoAnomaly, shipInfoAIS));
        CurrentShipDetails expectedDetails = new CurrentShipDetails(info, signal, expectedMaxInfo);
        assertThat(receivedDetails).isEqualTo(expectedDetails);
    }

    @Test
    void aggregateSignalsWithAISFirst() throws Exception {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        ShipInformation shipInfoAnomaly = new ShipInformation(1L, info, null);
        ShipInformation shipInfoAIS = new ShipInformation(1L, null, signal);

        MaxAnomalyScoreDetails expectedMaxInfo = new MaxAnomalyScoreDetails(0.5F, timestamp);

        CurrentShipDetails receivedDetails = runTheMapping(List.of(shipInfoAIS, shipInfoAnomaly));
        CurrentShipDetails expectedDetails = new CurrentShipDetails(info, signal, expectedMaxInfo);
        assertThat(receivedDetails).isEqualTo(expectedDetails);
    }

    /**
     * First sends a hight anomaly score, then sends a signal with a lower anomaly score
     * @throws Exception in case of failure
     */
    @Test
    void aggregateSignalsWithNonHighestMaxScore() throws Exception {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");

        AnomalyInformation info1 = new AnomalyInformation(1f, "explain", timestamp, 1L);
        ShipInformation shipInfoAnomaly1 = new ShipInformation(1L, info1, null);

        AnomalyInformation info2 = new AnomalyInformation(0.5f, "explain", timestamp2, 1L);
        ShipInformation shipInfoAnomaly2 = new ShipInformation(1L, info2, null);

        ShipInformation shipInfoAIS = new ShipInformation(1L, null, signal);

        MaxAnomalyScoreDetails expectedMaxInfo = new MaxAnomalyScoreDetails(1F, timestamp);

        CurrentShipDetails receivedDetails = runTheMapping(List.of(shipInfoAIS, shipInfoAnomaly1, shipInfoAnomaly2));
        CurrentShipDetails expectedDetails = new CurrentShipDetails(info2, signal, expectedMaxInfo);
        assertThat(receivedDetails).isEqualTo(expectedDetails);
    }
}
