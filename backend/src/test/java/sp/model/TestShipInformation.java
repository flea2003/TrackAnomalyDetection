package sp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class TestShipInformation {

    ShipInformation shipInformation;
    AnomalyInformation anomalyInformation;
    AISSignal aisSignal;

    @BeforeEach
    void setUp() {
        Timestamp timestamp = new Timestamp("01/04/2015 20:19");
        aisSignal = new AISSignal("hash1", 1, 2, 3, 4, 5, timestamp, "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", timestamp, "hash1");
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



}
