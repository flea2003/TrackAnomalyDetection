package sp.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAISSignal {

    OffsetDateTime dateTime = OffsetDateTime.of(2004, 01, 27, 1,1,0,0, ZoneOffset.ofHours(0));
    String json = "{\"shipHash\":\"ship123\",\"speed\":22.5,\"longitude\":130.0,\"latitude\":45.0,\"course\":180.0,\"heading\":90.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"New York\"}";
    AISSignal ais =  new AISSignal("ship123", 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, OffsetDateTime.of(2004, 01, 27, 1,1,0,0, ZoneOffset.ofHours(0)), "New York");

    @Test
    void testToJSON() throws JsonProcessingException {
        assertEquals(json, ais.toJson());
    }

    @Test
    void testToString() throws JsonProcessingException {
        assertEquals(ais, AISSignal.fromJson(json));
    }


    @Test
    void testEquals(){
        assertEquals(ais, ais);
        assertNotEquals(ais, null);
        assertNotEquals(ais, new Object());
        assertNotEquals(ais, new AISSignal("", 1,1,1,1,1,dateTime,""));
    }
}
