package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class TestAISSignal {

    OffsetDateTime dateTime = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));
    String json = "{\"id\":123,\"speed\":22.5,\"longitude\":130.0,\"latitude\":45.0,\"course\":180.0,\"heading\":90.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"New York\"}";
    AISSignal ais =  new AISSignal(123L, 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0)), "New York");

    @Test
    void testToJSON() throws JsonProcessingException {
        assertEquals(json, ais.toJson());
    }

    @Test
    void testToString() {
        assertEquals(ais, AISSignal.fromJson(json));
    }

    @Test
    void testEquals(){
        assertEquals(ais,
                new AISSignal(123, 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, dateTime, "New York")
        );
        assertNotEquals(ais, null);
        assertNotEquals(ais, new Object());
        assertNotEquals(ais, new AISSignal(0L, 1,1,1,1,1,dateTime,""));
    }

}
