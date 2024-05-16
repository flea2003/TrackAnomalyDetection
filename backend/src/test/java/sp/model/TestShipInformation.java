package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestShipInformation {

    ShipInformation shipInformation;
    AnomalyInformation anomalyInformation;
    AISSignal aisSignal;
    OffsetDateTime dateTime = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() {
        aisSignal = new AISSignal("hash1", 1, 2, 3, 4, 5, dateTime, "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", dateTime, "hash1");
        shipInformation = new ShipInformation("hash1",anomalyInformation, aisSignal);
    }

    @Test
    void testToJson() throws JsonProcessingException {
        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"hash1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"shipHash\":\"hash1\"},\"aisSignal\":{\"shipHash\":\"hash1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testFromJson1() throws JsonProcessingException {
        assertThat(ShipInformation.fromJson("{\"shipHash\":\"hash1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"shipHash\":\"hash1\"},\"aisSignal\":{\"shipHash\":\"hash1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"port\"}}")).isEqualTo(shipInformation);
    }

    @Test
    void testFromJson2() throws JsonProcessingException {
        assertThat(ShipInformation.fromJson(shipInformation.toJson())).isEqualTo(shipInformation);
    }

    @Test
    void testFromJsonException() {
        // bad JSON string has field "shiPppHash"
        String badJson = "{\"shiPppHash\":\"hash1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"hash1\"},\"aisSignal\":{\"shipHash\":\"hash1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}";
        assertThrows(RuntimeException.class, () -> ShipInformation.fromJson(badJson));
    }

    @Test
    void testToJsonEmptyHash() throws JsonProcessingException {
        String shipHash = "";

        aisSignal = new AISSignal(shipHash, 1, 2, 3, 4, 5, dateTime, "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", dateTime, shipHash);
        shipInformation = new ShipInformation(shipHash,anomalyInformation, aisSignal);

        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"\"},\"aisSignal\":{\"shipHash\":\"\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testToJsonBlankHash() throws JsonProcessingException {
        String shipHash = "   ";

        aisSignal = new AISSignal(shipHash, 1, 2, 3, 4, 5, dateTime, "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", dateTime, shipHash);
        shipInformation = new ShipInformation(shipHash,anomalyInformation, aisSignal);

        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"   \",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"   \"},\"aisSignal\":{\"shipHash\":\"   \",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testToJsonNullAISSignal() throws JsonProcessingException {
        String shipHash = "ship1";

        anomalyInformation = new AnomalyInformation(0.5F, "explanation", dateTime, shipHash);
        shipInformation = new ShipInformation(shipHash, anomalyInformation, null);

        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"ship1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"ship1\"},\"aisSignal\":null}");
    }

    @Test
    void testToJsonNullAnomalyInformation() throws JsonProcessingException {
        String shipHash = "ship1";

        aisSignal = new AISSignal(shipHash, 1, 2, 3, 4, 5, dateTime, "port");
        shipInformation = new ShipInformation(shipHash, null, aisSignal);

        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"ship1\",\"anomalyInformation\":null,\"aisSignal\":{\"shipHash\":\"ship1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testToJsonAISSignalWrongHash() {
        aisSignal = new AISSignal("wrongShip", 1, 2, 3, 4, 5, dateTime, "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", dateTime, "ship1");
        shipInformation = new ShipInformation("ship1", anomalyInformation, aisSignal);

        assertThrows(AssertionError.class, () -> shipInformation.toJson());
    }

    @Test
    void testToJsonAnomalyInfoWrongHash() {
        aisSignal = new AISSignal("ship1", 1, 2, 3, 4, 5, dateTime, "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", dateTime, "wrongShip");
        shipInformation = new ShipInformation("ship1", anomalyInformation, aisSignal);

        assertThrows(AssertionError.class, () -> shipInformation.toJson());
    }

}
