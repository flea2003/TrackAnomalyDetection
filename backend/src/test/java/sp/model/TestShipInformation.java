package sp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestShipInformation {

    ShipInformation shipInformation;
    AnomalyInformation anomalyInformation;
    AISSignal aisSignal;

    @BeforeEach
    void setUp() {
        aisSignal = new AISSignal("hash1", 1, 2, 3, 4, 5, "timestamp1", "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", "12/12/12", "hash1");
        shipInformation = new ShipInformation("hash1",anomalyInformation, aisSignal);
    }

    @Test
    void testToJson() {
        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"hash1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"hash1\"},\"aisSignal\":{\"shipHash\":\"hash1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testFromJson1() {
        assertThat(ShipInformation.fromJson("{\"shipHash\":\"hash1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"hash1\"},\"aisSignal\":{\"shipHash\":\"hash1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}")).isEqualTo(shipInformation);
    }

    @Test
    void testFromJson2() {
        assertThat(ShipInformation.fromJson(shipInformation.toJson())).isEqualTo(shipInformation);
    }

    @Test
    void testFromJsonException() {
        // bad JSON string has field "shiPppHash"
        String badJson = "{\"shiPppHash\":\"hash1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"hash1\"},\"aisSignal\":{\"shipHash\":\"hash1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}";
        assertThrows(RuntimeException.class, () -> ShipInformation.fromJson(badJson));
    }

    @Test
    void testToJsonEmptyHash() {
        String shipHash = "";

        aisSignal = new AISSignal(shipHash, 1, 2, 3, 4, 5, "timestamp1", "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", "12/12/12", shipHash);
        shipInformation = new ShipInformation(shipHash,anomalyInformation, aisSignal);

        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"\"},\"aisSignal\":{\"shipHash\":\"\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testToJsonBlankHash() {
        String shipHash = "   ";

        aisSignal = new AISSignal(shipHash, 1, 2, 3, 4, 5, "timestamp1", "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", "12/12/12", shipHash);
        shipInformation = new ShipInformation(shipHash,anomalyInformation, aisSignal);

        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"   \",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"   \"},\"aisSignal\":{\"shipHash\":\"   \",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testToJsonNullAISSignal() {
        String shipHash = "ship1";

        anomalyInformation = new AnomalyInformation(0.5F, "explanation", "12/12/12", shipHash);
        shipInformation = new ShipInformation(shipHash, anomalyInformation, null);

        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"ship1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"ship1\"},\"aisSignal\":null}");
    }

    @Test
    void testToJsonNullAnomalyInformation() {
        String shipHash = "ship1";

        aisSignal = new AISSignal(shipHash, 1, 2, 3, 4, 5, "timestamp1", "port");
        shipInformation = new ShipInformation(shipHash, null, aisSignal);

        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"ship1\",\"anomalyInformation\":null,\"aisSignal\":{\"shipHash\":\"ship1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"timestamp1\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testToJsonAISSignalWrongHash() {
        aisSignal = new AISSignal("wrongShip", 1, 2, 3, 4, 5, "timestamp1", "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", "12/12/12", "ship1");
        shipInformation = new ShipInformation("ship1", anomalyInformation, aisSignal);

        assertThrows(AssertionError.class, () -> shipInformation.toJson());
    }

    @Test
    void testToJsonAnomalyInfoWrongHash() {
        aisSignal = new AISSignal("ship1", 1, 2, 3, 4, 5, "timestamp1", "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", "12/12/12", "wrongShip");
        shipInformation = new ShipInformation("ship1", anomalyInformation, aisSignal);

        assertThrows(AssertionError.class, () -> shipInformation.toJson());
    }

}
