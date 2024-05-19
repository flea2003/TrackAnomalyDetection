package sp.pipeline.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AnomalyInformation;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;
import sp.pipeline.aggregators.CurrentStateAggregator;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class CurrentStateAggregatorTest {

    private CurrentStateAggregator currentStateAggregator;

    private OffsetDateTime timestamp = OffsetDateTime.of(2015, 4, 18, 1,1,0,0, ZoneOffset.ofHours(0));
    private OffsetDateTime timestamp2 = OffsetDateTime.of(2016, 4, 18, 1,1,0,0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() {
        currentStateAggregator = new CurrentStateAggregator();
    }

    @AfterEach
    void tearDown() {
        currentStateAggregator = null;
    }

    @Test
    void detailsNotInitializedFirstNull() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        CurrentShipDetails details = new CurrentShipDetails(null, signal);
        assertTrue(currentStateAggregator.shipDetailsNotInitialized(details));
    }

    @Test
    void detailsNotInitializedSecondNull() {
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, null);
        assertTrue(currentStateAggregator.shipDetailsNotInitialized(details));
    }

    @Test
    void detailsNotInitializedBothNonNull() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, signal);
        assertFalse(currentStateAggregator.shipDetailsNotInitialized(details));
    }

    @Test
    void testEncapsulatesAnomalyWhenNull() {
        CurrentShipDetails details = new CurrentShipDetails(null, null);

        assertFalse(currentStateAggregator.encapsulatesAnomalyInformation(null, details));
    }

    @Test
    void testEncapsulatesAnomalyWhenNotFinalized() {
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, null);

        assertTrue(currentStateAggregator.encapsulatesAnomalyInformation(info, details));
    }

    @Test
    void testEncapsulatesAnomalyBasedOnTimestampsFalse() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, signal);

        assertFalse(currentStateAggregator.encapsulatesAnomalyInformation(info, details));
    }

    @Test
    void testEncapsulatesAnomalyBasedOnTimestampsTrue() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, signal);

        AnomalyInformation info2 = new AnomalyInformation(0.5f, "explain", timestamp2, 1L);
        assertTrue(currentStateAggregator.encapsulatesAnomalyInformation(info2, details));
    }

    @Test
    void testEncapsulatesAISWhenNull() {
        CurrentShipDetails details = new CurrentShipDetails(null, null);

        assertFalse(currentStateAggregator.encapsulatesAISSignal(null, details));
    }

    @Test
    void testEncapsulatesAISWhenNotFinalized() {
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, null);

        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        assertTrue(currentStateAggregator.encapsulatesAISSignal(signal, details));
    }

    @Test
    void testEncapsulatesAISBasedOnTimestampsFalse() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, signal);

        assertFalse(currentStateAggregator.encapsulatesAISSignal(signal, details));
    }

    @Test
    void testEncapsulatesAISBasedOnTimestampsTrue() {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);
        CurrentShipDetails details = new CurrentShipDetails(info, signal);

        AISSignal signal2 = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp2, "port");
        assertTrue(currentStateAggregator.encapsulatesAISSignal(signal2, details));
    }

    @Test
    void aggregateSignalsWithNewAIS() throws JsonProcessingException {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);

        CurrentShipDetails details = new CurrentShipDetails(info, null);
        ShipInformation shipInfo = new ShipInformation(1L, null, signal);

        CurrentShipDetails result = currentStateAggregator.aggregateSignals(details, shipInfo.toJson());

        assertSame(result, details);
        assertEquals(result.getCurrentAISSignal(), signal);
    }

    @Test
    void aggregateSignalsWithNewAnomalyInfo() throws JsonProcessingException {
        AISSignal signal = new AISSignal(1L, 1f, 2f, 3f, 4f, 5f, timestamp, "port");
        AnomalyInformation info = new AnomalyInformation(0.5f, "explain", timestamp, 1L);

        CurrentShipDetails details = new CurrentShipDetails(null, signal);
        ShipInformation shipInfo = new ShipInformation(1L, info, null);

        CurrentShipDetails result = currentStateAggregator.aggregateSignals(details, shipInfo.toJson());

        assertSame(result, details);
        assertEquals(result.getCurrentAnomalyInformation(), info);
    }

}