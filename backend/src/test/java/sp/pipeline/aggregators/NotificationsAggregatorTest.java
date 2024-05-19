package sp.pipeline.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityExistsException;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonParseException;
import sp.controllers.NotificationController;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotFoundNotificationException;
import sp.model.Notification;
import sp.services.NotificationService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class NotificationsAggregatorTest {

    private NotificationService notificationService;

    private NotificationsAggregator notificationsAggregator;

    private String valueJsonLow = "{\"score\":10,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"id\":1}";
    private String valueJsonHigh = "{\"score\":50,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"id\":1}";
    private String key = "1";

    private AnomalyInformation initialValue = new AnomalyInformation(0F, null, null, 1L);
    private AnomalyInformation oldValueLow = new AnomalyInformation(10, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)), 1L);
    private AnomalyInformation oldValueHigh = new AnomalyInformation(60, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)), 1L);

    @BeforeEach
    void setUp() throws NotFoundNotificationException {
        this.notificationService = mock(NotificationService.class);
        notificationService = mock(NotificationService.class);
    }

    @Test
    void testController() {
        assertThat(notificationsAggregator).isNotNull();
        assertThat(notificationsAggregator.getNotificationThreshold()).isEqualTo(30);
    }

    @Test
    void wrongJson() {
        assertThrows(JsonParseException.class, () -> notificationsAggregator.aggregateSignals(oldValueHigh, "bad value", 1L));
    }

    @Test
    void testAggregateSimple() throws NotFoundNotificationException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenThrow(EntityExistsException.class);
        notificationsAggregator = new NotificationsAggregator(notificationService);
        assertThat(notificationsAggregator.aggregateSignals(initialValue, valueJsonHigh, 1L)).isEqualTo(AnomalyInformation.fromJson(valueJsonHigh));
        verify(notificationService).addNotification(new Notification(AnomalyInformation.fromJson(valueJsonHigh)));
    }



}
