package sp.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AggregatorTest {

    private Aggregator aggregator;

    private final OffsetDateTime t0 = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));
    private final OffsetDateTime t1 = OffsetDateTime.of(2005, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));
    private final OffsetDateTime t2 = OffsetDateTime.of(2006, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() {
        aggregator = new Aggregator();
    }

    @AfterEach
    void tearDown() {
        aggregator = null;
    }

    @Test
    void aggregateSignalsTestShipInfoHasBothAisAndAnomalyException() {
        AnomalyInformation anomalyInfo1 = new AnomalyInformation(0.5f, "", t1, "ship1");
        CurrentShipDetails details1 = new CurrentShipDetails(anomalyInfo1, null);

        AnomalyInformation anomalyInfo2 = new AnomalyInformation(0.25f, "", t0, "ship1");
        AISSignal signal2 = new AISSignal("ship1", 1, 2, 3, 4, 5, t0, "port1");
        ShipInformation shipInfo2 = new ShipInformation("ship1", anomalyInfo2, signal2);

        assertThrows(RuntimeException.class, () -> aggregator.aggregateSignals(details1, shipInfo2.toJson(), "ship1"));
    }

    @Test
    void aggregateSignalsTestEmptyPastInfo() throws JsonProcessingException {
        AnomalyInformation anomalyInfo1 = new AnomalyInformation(0.5f, "", t1, "ship1");
        CurrentShipDetails details1 = new CurrentShipDetails(anomalyInfo1, null);

        AISSignal signal2 = new AISSignal("ship1", 1, 2, 3, 4, 5, t0, "port1");
        ShipInformation shipInfo2 = new ShipInformation("ship1", null, signal2);

        CurrentShipDetails result = aggregator.aggregateSignals(details1, shipInfo2.toJson(), "ship1");

        assertEquals(new CurrentShipDetails(anomalyInfo1, List.of(shipInfo2)), result);
    }

    @Test
    void aggregateSignalsTestUpdatesAisSignal() throws JsonProcessingException {
        AnomalyInformation anomalyInfo1 = new AnomalyInformation(0.5f, "", t1, "ship1");

        AISSignal signal1 = new AISSignal("ship1", 1, 2, 3, 4, 5, t0, "port1");
        ShipInformation shipInfo1 = new ShipInformation("ship1", null, signal1);
        CurrentShipDetails details1 = new CurrentShipDetails(anomalyInfo1, List.of(shipInfo1));

        AnomalyInformation anomalyInfo2 = new AnomalyInformation(0.25f, "", t0, "ship1");
        ShipInformation shipInfo2 = new ShipInformation("ship1", anomalyInfo2, null);

        CurrentShipDetails result = aggregator.aggregateSignals(details1, shipInfo2.toJson(), "ship1");

        assertEquals(new CurrentShipDetails(
                anomalyInfo2, List.of(new ShipInformation("ship1", anomalyInfo2, signal1))
        ), result);

        // also check if information was updated
        assertEquals(anomalyInfo2, result.getPastInformation().get(0).getAnomalyInformation());
    }

    @Test
    void findCorrespondingSignalNotFound() throws JsonProcessingException {
        AnomalyInformation anomalyInfo1 = new AnomalyInformation(0.5f, "", t1, "ship1");

        AISSignal signal1 = new AISSignal("ship1", 1, 2, 3, 4, 5, t2, "port1");
        ShipInformation shipInfo1 = new ShipInformation("ship1", null, signal1);
        CurrentShipDetails details1 = new CurrentShipDetails(anomalyInfo1, List.of(shipInfo1));

        AnomalyInformation anomalyInfo2 = new AnomalyInformation(0.25f, "", t0, "ship1");
        ShipInformation shipInfo2 = new ShipInformation("ship1", anomalyInfo2, null);

        CurrentShipDetails result = aggregator.aggregateSignals(details1, shipInfo2.toJson(), "ship1");

        // nothing changed in the ship details
        assertEquals(new CurrentShipDetails(
                anomalyInfo2, List.of(new ShipInformation("ship1", null, signal1))
        ), result);
    }

    @Test
    void aggregateSignalsTestListContainsMore() throws JsonProcessingException {
        AnomalyInformation anomalyInfo1 = new AnomalyInformation(0.5f, "", t1, "ship1");

        AISSignal signal1 = new AISSignal("ship1", 1, 2, 3, 4, 5, t0, "port1");
        ShipInformation shipInfo1 = new ShipInformation("ship1", null, signal1);
        AISSignal signalUseless = new AISSignal("ship1", 1, 2, 3, 4, 5, t2, "port1");
        ShipInformation shipInfoUseless = new ShipInformation("ship1", null, signalUseless);
        CurrentShipDetails details1 = new CurrentShipDetails(anomalyInfo1, List.of(shipInfo1, shipInfoUseless));

        AnomalyInformation anomalyInfo2 = new AnomalyInformation(0.25f, "", t0, "ship1");
        ShipInformation shipInfo2 = new ShipInformation("ship1", anomalyInfo2, null);

        CurrentShipDetails result = aggregator.aggregateSignals(details1, shipInfo2.toJson(), "ship1");

        assertEquals(new CurrentShipDetails(
                anomalyInfo2, List.of(new ShipInformation("ship1", anomalyInfo2, signal1), shipInfoUseless)
        ), result);

        // also check if information was updated
        assertEquals(anomalyInfo2, result.getPastInformation().get(0).getAnomalyInformation());
    }

    @Test
    void aggregateSignalsTestFailedAssertion1() {
        AnomalyInformation anomalyInfo1 = new AnomalyInformation(0.5f, "", t1, "ship1");

        AISSignal signal1 = new AISSignal("ship1", 1, 2, 3, 4, 5, t0, "port1");
        ShipInformation shipInfo1 = new ShipInformation("ship1", null, signal1);
        CurrentShipDetails details1 = new CurrentShipDetails(anomalyInfo1, List.of(shipInfo1));

        // anomalyInfo2 has wrong hash, so it fails assertion
        AnomalyInformation anomalyInfo2 = new AnomalyInformation(0.25f, "", t0, "ship2");
        ShipInformation shipInfo2 = new ShipInformation("ship1", anomalyInfo2, null);

        assertThrows(AssertionError.class, () ->
                aggregator.aggregateSignals(details1, shipInfo2.toJson(), "ship1")
        );
    }

    @Test
    void aggregateSignalsTestFailedAssertion2() {
        AnomalyInformation anomalyInfo1 = new AnomalyInformation(0.5f, "", t1, "ship1");

        // signal1 has wrong hash, so it fails assertion
        AISSignal signal1 = new AISSignal("ship2", 1, 2, 3, 4, 5, t0, "port1");
        ShipInformation shipInfo1 = new ShipInformation("ship1", null, signal1);
        CurrentShipDetails details1 = new CurrentShipDetails(anomalyInfo1, List.of(shipInfo1));

        AnomalyInformation anomalyInfo2 = new AnomalyInformation(0.25f, "", t0, "ship1");
        ShipInformation shipInfo2 = new ShipInformation("ship1", anomalyInfo2, null);

        assertThrows(AssertionError.class, () ->
                aggregator.aggregateSignals(details1, shipInfo2.toJson(), "ship1")
        );
    }

}