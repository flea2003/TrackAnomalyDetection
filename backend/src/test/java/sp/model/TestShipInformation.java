package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.pipeline.utils.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestShipInformation {

    ShipInformation shipInformation;
    AnomalyInformation anomalyInformation;
    AISSignal aisSignal;
    OffsetDateTime dateTime = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() {
        aisSignal = new AISSignal(1L, 1, 2, 3, 4, 5, dateTime, "port");
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", dateTime, 1L);
        shipInformation = new ShipInformation(1L, anomalyInformation, aisSignal);
    }

    @Test
    void testFromJson2() throws JsonProcessingException {
        ShipInformation reconstructed = JsonMapper.fromJson(JsonMapper.toJson(shipInformation), ShipInformation.class);
        assertThat(reconstructed).isEqualTo(shipInformation);
    }

    @Test
    void testToJsonNullAISSignal() throws JsonProcessingException {
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", dateTime, 123L);
        shipInformation = new ShipInformation(123L, anomalyInformation, null);

        String json = JsonMapper.toJson(shipInformation);
        // check if conversion to both sides resulted in the same object
        assertEquals(shipInformation, JsonMapper.fromJson(json, ShipInformation.class));
    }

    @Test
    void testToJsonNullAnomalyInformation() throws JsonProcessingException {
        long shipHash = 123L;

        aisSignal = new AISSignal(shipHash, 1, 2, 3, 4, 5, dateTime, "port");
        shipInformation = new ShipInformation(shipHash, null, aisSignal);

        String json = JsonMapper.toJson(shipInformation);
        // check if conversion to both sides resulted in the same object
        assertEquals(shipInformation, JsonMapper.fromJson(json, ShipInformation.class));
    }
}
