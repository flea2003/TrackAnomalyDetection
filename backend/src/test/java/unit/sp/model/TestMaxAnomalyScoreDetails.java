package unit.sp.model;

import org.junit.jupiter.api.Test;
import sp.model.MaxAnomalyScoreDetails;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestMaxAnomalyScoreDetails {

    OffsetDateTime dateTime = OffsetDateTime.of(2004, 1, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0));
    MaxAnomalyScoreDetails maxInfo = new MaxAnomalyScoreDetails(12F, OffsetDateTime.of(2004, 1, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)));

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
