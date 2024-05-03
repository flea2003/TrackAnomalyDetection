package helperObjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestAIS {

    String json = "{\"shipHash\":\"ship123\",\"speed\":22.5,\"longitude\":130.0,\"latitude\":45.0,\"course\":180.0,\"heading\":90.0,\"timestamp\":\"2024-05-03T12:00:00Z\",\"departurePort\":\"New York\"}";
    AIS ais =  new AIS("ship123", 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, "2024-05-03T12:00:00Z", "New York");

    @Test
    void testToJSON() {
        assertEquals(json, ais.toJson());
    }

    @Test
    void testToString() {
        assertEquals(ais, AIS.fromJson(json));
    }

    @Test
    void testInvalidJson() {
        String invalidJson = "{\"shipHash\":\"ship123\"";
        AIS ais = AIS.fromJson(invalidJson);
        assertEquals("", ais.getShipHash());
        assertEquals(-1, ais.getSpeed());
    }

    @Test
    void testToStringActual() {
        AIS ais = new AIS("ship123", 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, "2024-05-03T12:00:00Z", "New York");
        String expectedString = "AIS{shipHash='ship123', speed=22.5, longitude=130.0, latitude=45.0, course=180.0, heading=90.0, timestamp='2024-05-03T12:00:00Z', departurePort='New York'}";
        assertEquals(expectedString, ais.toString());
    }

    @Test
    void testEquals(){
        assertEquals(ais, ais);
        assertNotEquals(ais, null);
        assertNotEquals(ais, new Object());
        assertNotEquals(ais, new AIS("", 1,1,1,1,1,"",""));
    }

}
