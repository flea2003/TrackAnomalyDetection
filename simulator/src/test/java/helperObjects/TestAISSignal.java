package helperObjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAISSignal {

    String json = "{\"shipHash\":\"ship123\",\"speed\":22.5,\"longitude\":130.0,\"latitude\":45.0,\"course\":180.0,\"heading\":90.0,\"timestamp\":\"2024-05-03T12:00:00Z\",\"departurePort\":\"New York\"}";
    AISSignal ais =  new AISSignal("ship123", 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, "2024-05-03T12:00:00Z", "New York");

    @Test
    void testToJSON() throws JsonProcessingException {
        assertEquals(json, ais.toJson());
    }

    @Test
    void testToString() throws JsonProcessingException {
        assertEquals(ais, AISSignal.fromJson(json));
    }

    @Test
    void testInvalidJson() {
        String invalidJson = "{\"shipHash\":\"ship123\"";
        assertThrows(JsonProcessingException.class, () -> AISSignal.fromJson(invalidJson));
    }

    @Test
    void testToStringActual() {
        AISSignal ais = new AISSignal("ship123", 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, "2024-05-03T12:00:00Z", "New York");
        String expectedString = "AISSignal(shipHash=ship123, speed=22.5, longitude=130.0, latitude=45.0, course=180.0, heading=90.0, timestamp=2024-05-03T12:00:00Z, departurePort=New York)";
        assertEquals(expectedString, ais.toString());
    }

    @Test
    void testEquals(){
        assertEquals(ais, ais);
        assertNotEquals(ais, null);
        assertNotEquals(ais, new Object());
        assertNotEquals(ais, new AISSignal("", 1,1,1,1,1,"",""));
    }

    @Test
    void testHashCode() {
        assertEquals(ais.hashCode(), ais.hashCode());
    }

    @Test
    void getShipHash() {
        assertEquals(ais.getShipHash(), "ship123");
    }

    @Test
    void testGetSpeed() {
        assertEquals(ais.getSpeed(), 22.5f);
    }

    @Test
    void testGetLongitude() {
        assertEquals(ais.getLongitude(), 130.0f);
    }

    @Test
    void testGetLatitude() {
        assertEquals(ais.getLatitude(), 45.0f);
    }

    @Test
    void testGetCourse() {
        assertEquals(ais.getCourse(), 180.0f);
    }

    @Test
    void testGetTimestamp() {
        assertEquals(ais.getTimestamp(), "2024-05-03T12:00:00Z");
    }

    @Test
    void testGetDeparturePort() {
        assertEquals(ais.getDeparturePort(), "New York");
    }

    @Test
    void testFromJsonWithEmptyJson() throws JsonProcessingException {
        String emptyJson = "{}";
        AISSignal ais = AISSignal.fromJson(emptyJson);
        assertNull(ais.getShipHash());
        assertEquals(0, ais.getSpeed());
    }

    @Test
    void testFromJsonWithPartialData() throws JsonProcessingException {
        String partialJson = "{\"shipHash\":\"ship123\", \"speed\":25.0}";
        AISSignal ais = AISSignal.fromJson(partialJson);
        assertEquals("ship123", ais.getShipHash());
        assertEquals(25.0, ais.getSpeed(), 0.01);
        assertEquals(0, ais.getLongitude()); // Default value since not specified
    }

}
