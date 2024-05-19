package sp.services;

import org.aspectj.weaver.ast.Not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotFoundNotificationException;
import sp.model.Notification;
import sp.repositories.NotificationRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TestNotificationService {

    NotificationRepository notificationRepository;
    NotificationService notificationService;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;
    private AnomalyInformation anomalyInformation1;


    @BeforeEach
    void setUp() throws NotFoundNotificationException {
        notificationRepository = mock(NotificationRepository.class);
        notification1 = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)),
                1L, 0, 0);
        notification2 = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)),
                2L, 0, 1);
        notification3 = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 28, 1, 2, 0, 0, ZoneOffset.ofHours(0)),
                2L, 0, 2);
        notificationService = mock(NotificationService.class);
        anomalyInformation1 = new AnomalyInformation(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0)), 1L);

        when(notificationRepository.findById(0L)).thenReturn(Optional.of(notification1));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification2));
        when(notificationRepository.findAll()).thenReturn(List.of(notification1, notification2, notification3));
        when(notificationRepository.findNotificationByShipID(2L)).thenReturn(List.of(notification2, notification3));
        when(notificationRepository.findNotificationByShipID(3L)).thenReturn(List.of());
        when(notificationRepository.findById(4L)).thenReturn(Optional.empty());
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void testController() {
        assertThat(notificationService).isNotNull();
    }
    @Test
    void testGetNotificationValid() throws NotFoundNotificationException {
        assertThat(notificationService.getNotificationById(0L)).isEqualTo(notification1);
    }

    @Test
    void testGetNotificationException() throws NotFoundNotificationException {
        assertThrows(NotFoundNotificationException.class, () -> notificationService.getNotificationById(4L));
    }

    @Test
    void testAddNotification1() {
        notificationService.addNotification(notification1);
        verify(notificationRepository, times(1)).save(notification1);
    }

    @Test
    void testAddNotification2() {
        notificationService.addNotification(anomalyInformation1);
        verify(notificationRepository, times(1)).save(notification1);
    }

    @Test
    void testGetAllNotificationsForShip() {
        List<Notification> list = notificationService.getAllNotificationForShip(2L);
        assertThat(list).isNotNull();
        assertThat(list).isEqualTo(List.of(notification2, notification3));
    }

    @Test
    void testGetNewestNotificationForShipValid() throws NotFoundNotificationException {
        Notification notification = notificationService.getNewestNotificationForShip(2L);
        assertThat(notification).isEqualTo(notification3);
    }

    @Test
    void testGetNewestNotificationForShipValidEqualTime() throws NotFoundNotificationException {
        Notification notification4 = new Notification(1F, "explanation",
                OffsetDateTime.of(2004, 01, 27, 0, 3, 0, 0, ZoneOffset.ofHours(0)),
                2L, 0, 0);
        when(notificationRepository.findNotificationByShipID(2L)).thenReturn(List.of(notification2, notification4));


        Notification notification = notificationService.getNewestNotificationForShip(2L);
        assertThat(notification).isEqualTo(notification2);
    }

    @Test
    void testGetNewestNotificationForShipException() throws NotFoundNotificationException {
        assertThrows(NotFoundNotificationException.class, () -> notificationService.getNewestNotificationForShip(3L));
    }

    @Test
    void getAllNotifications() throws NotFoundNotificationException {
        assertThat(notificationService.getAllNotifications()).isEqualTo(List.of(notification1, notification2, notification3));
    }


}
