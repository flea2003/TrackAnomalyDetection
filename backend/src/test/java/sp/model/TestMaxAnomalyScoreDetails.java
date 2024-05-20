package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class TestMaxAnomalyScoreDetails {

    OffsetDateTime dateTime = OffsetDateTime.of(2004, 1, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0));
    String json = "{\"maxAnomalyScore\":12.0,\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\"}";
    MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(12F, OffsetDateTime.of(2004, 1, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)));

    @Test
    void testToJSON() throws JsonProcessingException {
        assertEquals(json, maxInfo.toJson());
    }

    @Test
    void testToString() throws JsonProcessingException {
        assertEquals(maxInfo, MaxAnomalyScoreDetails.fromJson(json));
    }

    @Test
    void testEquals() {
        assertEquals(maxInfo,
                new MaxAnomalyScoreDetails(12F, OffsetDateTime.of(2004, 1, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)))
        );
        assertNotEquals(maxInfo, null);
        assertNotEquals(maxInfo, new Object());
        assertNotEquals(maxInfo, new MaxAnomalyScoreDetails(12.1F, dateTime));
    }
}
