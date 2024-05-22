package sp.pipeline.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonParseException;
import sp.model.*;
import sp.exceptions.NotFoundNotificationException;
import sp.pipeline.parts.aggregation.aggregators.NotificationsAggregator;
import sp.services.NotificationService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class NotificationsAggregatorTest {

    private NotificationService notificationService;

    private NotificationsAggregator notificationsAggregator;

    private String valueJsonLow = "{\"currentAnomalyInformation\":{\"score\":10.0,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"id\":1},\"currentAISSignal\":{\"id\":0,\"speed\":0.0,\"longitude\":0.0,\"latitude\":0.0,\"course\":0.0,\"heading\":0.0,\"timestamp\":null,\"departurePort\":null}, \"maxAnomalyScoreInfo\":{\"maxAnomalyScore\":null,\"correspondingTimestamp\":null}}\n";
    private String valueJsonHigh = "{\"currentAnomalyInformation\":{\"score\":50.0,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"id\":1},\"currentAISSignal\":{\"id\":0,\"speed\":0.0,\"longitude\":0.0,\"latitude\":0.0,\"course\":0.0,\"heading\":0.0,\"timestamp\":null,\"departurePort\":null}, \"maxAnomalyScoreInfo\":{\"maxAnomalyScore\":null,\"correspondingTimestamp\":null}}\n";
    private String key = "1";

    private Notification initialValue = new Notification(null, null, null);
    private Notification oldValueLow = new Notification(new CurrentShipDetails(new AnomalyInformation(10, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails()));
    private Notification oldValueHigh = new Notification(new CurrentShipDetails(new AnomalyInformation(50, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails()));

    @BeforeEach
    void setUp() throws NotFoundNotificationException, JsonProcessingException {
        notificationService = mock(NotificationService.class);
        notificationsAggregator = new NotificationsAggregator(notificationService);
        System.out.println(new CurrentShipDetails(new AnomalyInformation(50, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails()).toJson());
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
    void testAggregateSimpleHigh() throws NotFoundNotificationException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenThrow(NotFoundNotificationException.class);
        assertThat(notificationsAggregator.aggregateSignals(initialValue, valueJsonHigh, 1L)).isEqualTo(oldValueHigh);
        verify(notificationService, times(1)).addNotification(oldValueHigh);
    }


    @Test
    void testAggregateSimpleLow() throws NotFoundNotificationException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenThrow(NotFoundNotificationException.class);
        assertThat(notificationsAggregator.aggregateSignals(initialValue, valueJsonLow, 1L)).isEqualTo(oldValueLow);
        verify(notificationService, times(0)).addNotification(oldValueLow);
    }

    @Test
    void testAggregateComplexLow() throws NotFoundNotificationException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenReturn(oldValueLow);
        assertThat(notificationsAggregator.aggregateSignals(initialValue, valueJsonLow, 1L)).isEqualTo(oldValueLow);
        verify(notificationService, times(0)).addNotification(oldValueLow);
    }

    @Test
    void testAggregateComplexHigh1() throws NotFoundNotificationException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenReturn(oldValueLow);
        assertThat(notificationsAggregator.aggregateSignals(oldValueLow, valueJsonHigh, 1L)).isEqualTo(oldValueHigh);
        verify(notificationService, times(1)).addNotification(oldValueHigh);
    }


    @Test
    void testAggregateComplexHigh2() throws NotFoundNotificationException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenReturn(oldValueLow);
        valueJsonHigh =  "{\"currentAnomalyInformation\":{\"score\":20.0,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"id\":1},\"currentAISSignal\":{\"id\":0,\"speed\":0.0,\"longitude\":0.0,\"latitude\":0.0,\"course\":0.0,\"heading\":0.0,\"timestamp\":null,\"departurePort\":null}}\n";
        assertThat(notificationsAggregator.aggregateSignals(oldValueHigh, valueJsonHigh, 1L)).isEqualTo(new Notification(CurrentShipDetails.fromJson(valueJsonHigh)));
        verify(notificationService, times(0)).addNotification(oldValueHigh);
    }
}
