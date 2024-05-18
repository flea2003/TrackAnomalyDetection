package sp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AnomalyInformation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestNotification {

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)),
                "hash1", 0, 0);
    }

    @Test
    void testNotification() {
        Notification notification = new Notification();
        assertThat(notification).isNotNull();
    }

    @Test
    void testConstructor1() {
        Notification notification2 = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)),
                "hash1", 0, 0);

        assertThat(notification2).isEqualTo(notification);
    }


    @Test
    void testConstructor2() {
        AnomalyInformation anomalyInformation = new AnomalyInformation(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)), "hash1");
        Notification notification2 = new Notification(anomalyInformation);

        assertThat(notification2).isEqualTo(notification);
    }



}
