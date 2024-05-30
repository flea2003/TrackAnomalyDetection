package unit.sp.pipeline.parts.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.exceptions.NotificationNotFoundException;
import sp.model.*;
import sp.pipeline.parts.notifications.NotificationsAggregator;
import sp.services.NotificationService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class NotificationsAggregatorTest {

    private NotificationService notificationService;

    private NotificationsAggregator notificationsAggregator;
    private CurrentShipDetails valueLow = new CurrentShipDetails(new AnomalyInformation(10, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails());
    private CurrentShipDetails valueHigh = new CurrentShipDetails(new AnomalyInformation(50, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails());
    private String key = "1";

    private Notification initialValue = new Notification(null, null, null);
    private Notification oldValueLow = new Notification(new CurrentShipDetails(new AnomalyInformation(10, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails()));
    private Notification oldValueHigh = new Notification(new CurrentShipDetails(new AnomalyInformation(50, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails()));

    @BeforeEach
    void setUp() throws NotificationNotFoundException, JsonProcessingException {
        notificationService = mock(NotificationService.class);
        notificationsAggregator = new NotificationsAggregator(notificationService);
    }

    @Test
    void testController() {
        assertThat(notificationsAggregator).isNotNull();
        assertThat(notificationsAggregator.getNotificationThreshold()).isEqualTo(30);
    }

    @Test
    void testAggregateSimpleHigh() throws NotificationNotFoundException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenThrow(NotificationNotFoundException.class);
        assertThat(notificationsAggregator.aggregateSignals(initialValue, valueHigh, 1L)).isEqualTo(oldValueHigh);
        verify(notificationService, times(1)).addNotification(oldValueHigh);
    }


    @Test
    void testAggregateSimpleLow() throws NotificationNotFoundException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenThrow(NotificationNotFoundException.class);
        assertThat(notificationsAggregator.aggregateSignals(initialValue, valueLow, 1L)).isEqualTo(oldValueLow);
        verify(notificationService, times(0)).addNotification(oldValueLow);
    }

    @Test
    void testAggregateComplexLow() throws NotificationNotFoundException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenReturn(oldValueLow);
        assertThat(notificationsAggregator.aggregateSignals(initialValue, valueLow, 1L)).isEqualTo(oldValueLow);
        verify(notificationService, times(0)).addNotification(oldValueLow);
    }

    @Test
    void testAggregateComplexHigh1() throws NotificationNotFoundException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenReturn(oldValueLow);
        assertThat(notificationsAggregator.aggregateSignals(oldValueLow, valueHigh, 1L)).isEqualTo(oldValueHigh);
        verify(notificationService, times(1)).addNotification(oldValueHigh);
    }


    @Test
    void testAggregateComplexHigh2() throws NotificationNotFoundException, JsonProcessingException {
        when(notificationService.getNewestNotificationForShip(1L)).thenReturn(oldValueLow);
        valueHigh = new CurrentShipDetails(new AnomalyInformation(20, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails());;
        assertThat(notificationsAggregator.aggregateSignals(oldValueHigh, valueHigh, 1L)).isEqualTo(new Notification(valueHigh));
        verify(notificationService, times(0)).addNotification(oldValueHigh);
    }
}

