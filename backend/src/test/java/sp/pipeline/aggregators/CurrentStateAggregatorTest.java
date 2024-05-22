package sp.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.*;
import sp.pipeline.aggregators.CurrentStateAggregator;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class CurrentStateAggregatorTest {

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

    @Test
    void aggregateSignalsWithNewAIS() throws JsonProcessingException {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(info, null, maxInfo);
        ShipInformation shipInfo = new ShipInformation(1L, null, signal);

        CurrentShipDetails result = aggregator.aggregateSignals(details, shipInfo.toJson());

        assertSame(result, details);
        assertEquals(result.getCurrentAISSignal(), signal);
    }

    @Test
    void aggregateSignalsWithNewAnomalyInfo() throws JsonProcessingException {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(null, signal, maxInfo);
        ShipInformation shipInfo = new ShipInformation(1L, info, null);

        CurrentShipDetails result = aggregator.aggregateSignals(details, shipInfo.toJson());

        assertSame(result, details);
        assertEquals(result.getCurrentAnomalyInformation(), info);
    }

    @Test
    void aggregateSignalsWithUninitializedMaxUpdate() throws JsonProcessingException {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(null, signal, null);
        ShipInformation shipInfo = new ShipInformation(1L, info, null);

        CurrentShipDetails result = aggregator.aggregateSignals(details, shipInfo.toJson());

        assertEquals(result.getMaxAnomalyScoreInfo().getMaxAnomalyScore(), info.getScore());
    }

    @Test
    void aggregateSignalsWithUninitializedMaxNoUpdate() throws JsonProcessingException {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(-1f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(null, signal, null);
        ShipInformation shipInfo = new ShipInformation(1L, info, null);

        CurrentShipDetails result = aggregator.aggregateSignals(details, shipInfo.toJson());

        assertEquals(result.getMaxAnomalyScoreInfo().getMaxAnomalyScore(), 0f);
    }

    @Test
    void aggregateSignalsWithInitializedMaxUpdate() throws JsonProcessingException {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation prevInfo = new AnomalyInformation(1f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(prevInfo, signal, maxInfo);

        AnomalyInformation newInfo = new AnomalyInformation(2F, "new explain", timestamp2, 1L);
        ShipInformation shipInfo = new ShipInformation(1L, newInfo, null);

        CurrentShipDetails result = aggregator.aggregateSignals(details, shipInfo.toJson());

        assertEquals(result.getMaxAnomalyScoreInfo().getMaxAnomalyScore(), newInfo.getScore());
    }

    @Test
    void aggregateSignalsWithInitializedMaxNoUpdate() throws JsonProcessingException {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation prevInfo = new AnomalyInformation(1f, "explain", timestamp, 1L);
        MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(1F, timestamp);
        CurrentShipDetails details = new CurrentShipDetails(prevInfo, signal, maxInfo);

        AnomalyInformation newInfo = new AnomalyInformation(0.5F, "new explain", timestamp2, 1L);
        ShipInformation shipInfo = new ShipInformation(1L, newInfo, null);

        CurrentShipDetails result = aggregator.aggregateSignals(details, shipInfo.toJson());

        assertEquals(result.getMaxAnomalyScoreInfo().getMaxAnomalyScore(), prevInfo.getScore());
    }

}
