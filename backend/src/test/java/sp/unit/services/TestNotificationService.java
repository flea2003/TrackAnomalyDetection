package sp.unit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.*;
import sp.exceptions.NotificationNotFoundException;
import sp.pipeline.parts.notifications.NotificationExtractor;
import sp.services.NotificationService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TestNotificationService {

    NotificationExtractor notificationExtractor;
    NotificationService notificationService;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;
    private OffsetDateTime dateTime =  OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() throws NotificationNotFoundException, JsonProcessingException {
        notificationExtractor = mock(NotificationExtractor.class);
        notification1 = new Notification(
                new CurrentShipDetails(
                        new AnomalyInformation(1F, "explanation", dateTime, 0L),
                        new AISSignal(0L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
                        new MaxAnomalyScoreDetails()
                )
        );

        notification2 = new Notification(
                new CurrentShipDetails(
                        new AnomalyInformation(1F, "explanation", dateTime, 2L),
                        new AISSignal(2L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
                        new MaxAnomalyScoreDetails()
                )
        );

        notification3 = new Notification(
                new CurrentShipDetails(
                        new AnomalyInformation(1F, "explanation", dateTime, 2L),
                        new AISSignal(2L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
                        new MaxAnomalyScoreDetails()
                )
        );

        when(notificationExtractor.getNotificationById(0L)).thenReturn(notification1);
        when(notificationExtractor.getNotificationById(1L)).thenReturn(notification2);
        when(notificationExtractor.getAllNotifications()).thenReturn(List.of(notification1, notification2, notification3));
        when(notificationExtractor.getNotificationById(4L)).thenReturn(null);
        notificationService = new NotificationService(notificationExtractor);
    }

    @Test
    void testController() {
        assertThat(notificationService).isNotNull();
    }
    @Test
    void testGetNotificationValid() throws NotificationNotFoundException {
        assertThat(notificationService.getNotificationById(0L)).isEqualTo(notification1);
    }

    @Test
    void testGetNotificationException() throws NotificationNotFoundException {
        assertThrows(NotificationNotFoundException.class, () -> notificationService.getNotificationById(4L));
    }


    @Test
    void testGetAllNotificationsForShip() {
        List<Notification> list = notificationService.getAllNotificationForShip(2L);
        assertThat(list).isNotNull();
        assertThat(list).isEqualTo(List.of(notification2, notification3));
    }

    @Test
    void getAllNotifications() throws NotificationNotFoundException {
        assertThat(notificationService.getAllNotifications()).isEqualTo(List.of(notification1, notification2, notification3));
    }
}
