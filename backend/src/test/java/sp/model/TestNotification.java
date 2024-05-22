package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AnomalyInformation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestNotification {

    private Notification notification;
    private OffsetDateTime dateTime;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        dateTime =  OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0));
        notification = new Notification(
                new CurrentShipDetails(
                        new AnomalyInformation(1F, "explanation", dateTime, 1L),
                        new AISSignal(1L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA")
                )
        );
    }

    @Test
    void testNotification() {
        Notification notification = new Notification();
        assertThat(notification).isNotNull();
    }

    @Test
    void testConstructor1() throws JsonProcessingException {
        Notification notification2 = new Notification(new CurrentShipDetails(
                new AnomalyInformation(1F, "explanation", dateTime, 1L),
                new AISSignal(1L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA")
        ));

        assertThat(notification2).isEqualTo(notification);
    }
}
