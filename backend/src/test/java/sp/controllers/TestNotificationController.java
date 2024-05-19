package sp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sp.exceptions.NotFoundNotificationException;
import sp.model.Notification;
import sp.services.NotificationService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestNotificationController {


    private NotificationService notificationService;
    private NotificationController notificationController;
    private Notification notification1;
    private Notification notification2;
    private Notification notification3;

    @BeforeEach
    void setUp() throws NotFoundNotificationException {
        notification1 = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)),
                1L, 0, 1);
        notification2 = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)),
                2L, 0, 1);
        notification3 = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)),
                2L, 0, 2);
        notificationService = mock(NotificationService.class);

        when(notificationService.getNotificationById(0L)).thenReturn(notification1);
        when(notificationService.getNotificationById(1L)).thenReturn(notification2);
        when(notificationService.getAllNotifications()).thenReturn(List.of(notification1, notification2, notification3));
        when(notificationService.getAllNotificationForShip(2L)).thenReturn(List.of(notification2, notification3));
        when(notificationService.getNotificationById(4L)).thenThrow(NotFoundNotificationException.class);
        notificationController = new NotificationController(notificationService);
    }

    @Test
    void testController() {
        assertThat(notificationController).isNotNull();
    }

    @Test
    void testGetAllNotifications() {
        assertThat(notificationController.getAllNotifications()).isEqualTo(ResponseEntity.ok(List.of(notification1, notification2, notification3)));
    }

    @Test
    void testGetNotificationByIdProper() throws NotFoundNotificationException {
        assertThat(notificationController.getNotificationById(0L)).isEqualTo(ResponseEntity.ok(notification1));
    }

    @Test
    void testGetNotificationByIdThrows() throws NotFoundNotificationException {
        assertThat(notificationController.getNotificationById(4L)).isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Test
    void testGetNotificationForAShip() throws NotFoundNotificationException {
        assertThat(notificationController.getAllNotificationsForShip(2L)).isEqualTo(ResponseEntity.ok(List.of(notification2, notification3)));
    }
}
