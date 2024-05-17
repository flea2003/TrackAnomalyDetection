package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class TestShipInformation {

    ShipInformation shipInformation;
    AnomalyInformation anomalyInformation;
    AISSignal aisSignal;
    OffsetDateTime dateTime = OffsetDateTime.of(2004, 01, 27, 1,1,0,0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() {
        aisSignal = new AISSignal("hash1", 1, 2, 3, 4, 5, dateTime, "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", -1F, dateTime, "hash1");
        shipInformation = new ShipInformation("hash1",anomalyInformation, aisSignal);
    }

    @Test
    void testToJson() throws JsonProcessingException {
        assertThat(shipInformation.toJson()).isEqualTo("{\"shipHash\":\"hash1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"maxAnomalyScore\":-1.0,\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"shipHash\":\"hash1\"},\"aisSignal\":{\"shipHash\":\"hash1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"port\"}}");
    }

    @Test
    void testFromJson1() throws JsonProcessingException {
        assertThat(ShipInformation.fromJson("{\"shipHash\":\"hash1\",\"anomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"maxAnomalyScore\":-1.0,\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"shipHash\":\"hash1\"},\"aisSignal\":{\"shipHash\":\"hash1\",\"speed\":1.0,\"longitude\":2.0,\"latitude\":3.0,\"course\":4.0,\"heading\":5.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"port\"}}")).isEqualTo(shipInformation);
    }

    @Test
    void testFromJson2() throws JsonProcessingException {
        assertThat(ShipInformation.fromJson(shipInformation.toJson())).isEqualTo(shipInformation);
    }

}
