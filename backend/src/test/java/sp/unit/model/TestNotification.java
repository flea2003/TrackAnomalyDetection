package sp.unit.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.*;

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
                        new AISSignal(1L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
                        new MaxAnomalyScoreDetails()
                )
        );
    }

    @Test
    void testNotification() {
        Notification notification = new Notification();
        assertThat(notification).isNotNull();
    }

    @Test
    void testConstructor1() {
        Notification notification2 = new Notification(new CurrentShipDetails(
                new AnomalyInformation(1F, "explanation", dateTime, 1L),
                new AISSignal(1L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
                new MaxAnomalyScoreDetails()
        ));

        // Since we cannot assert that IDs are equal (since we assign unique IDs for each one), assert by parts
        assertThat(notification2.getCurrentShipDetails()).isEqualTo(notification.getCurrentShipDetails());
    }

    @Test
    void testGedSerde() throws JsonProcessingException {
        assertThat(Notification.getSerde()).isExactlyInstanceOf(Jackson2Serde.class);
        assertThat(Notification.getSerde()).isExactlyInstanceOf(Jackson2Serde.class);
    }
}
