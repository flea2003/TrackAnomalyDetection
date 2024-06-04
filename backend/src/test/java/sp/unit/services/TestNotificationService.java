package sp.unit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sp.model.*;
import sp.exceptions.NotificationNotFoundException;
import sp.repositories.NotificationRepository;
import sp.services.NotificationService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TestNotificationService {

    NotificationRepository notificationRepository;
    NotificationService notificationService;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;
    private OffsetDateTime dateTime =  OffsetDateTime.of(2004, 01, 27, 1, 2, 0, 0, ZoneOffset.ofHours(0));

    @BeforeEach
    void setUp() throws NotificationNotFoundException, JsonProcessingException {
        notificationRepository = mock(NotificationRepository.class);
        notification1 = new Notification(
                new CurrentShipDetails(
                        new AnomalyInformation(1F, "explanation", dateTime, 0L),
                        new AISSignal(0L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
                        new MaxAnomalyScoreDetails()
                )
        );

        notification2 = new Notification(
                new CurrentShipDetails(
                        new AnomalyInformation(1F, "explanation", dateTime, 1L),
                        new AISSignal(1L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
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
    void testGetNotificationValid() throws NotificationNotFoundException {
        assertThat(notificationService.getNotificationById(0L)).isEqualTo(notification1);
    }

    @Test
    void testGetNotificationException() throws NotificationNotFoundException {
        assertThrows(NotificationNotFoundException.class, () -> notificationService.getNotificationById(4L));
    }

    @Test
    void testAddNotification1() {
        notificationService.addNotification(notification1);
        verify(notificationRepository, times(1)).save(notification1);
    }


    @Test
    void testGetAllNotificationsForShip() {
        List<Notification> list = notificationService.getAllNotificationForShip(2L);
        assertThat(list).isNotNull();
        assertThat(list).isEqualTo(List.of(notification2, notification3));
    }

    @Test
    void testGetNewestNotificationForShipValid() throws NotificationNotFoundException, JsonProcessingException {
        Notification notification = notificationService.getNewestNotificationForShip(2L);
        assertThat(notification).isEqualTo(notification3);
    }

    @Test
    void testGetNewestNotificationForShipValidEqualTime() throws NotificationNotFoundException, JsonProcessingException {
        Notification notification4 = new Notification(
                new CurrentShipDetails(
                        new AnomalyInformation(1F, "explanation", OffsetDateTime.of(2004, 01, 27, 0, 3, 0, 0, ZoneOffset.ofHours(0)), 2L),
                        new AISSignal(2L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
                        new MaxAnomalyScoreDetails()
                )
        );
        when(notificationRepository.findNotificationByShipID(2L)).thenReturn(List.of(notification2, notification4));
        Notification notification = notificationService.getNewestNotificationForShip(2L);
        assertThat(notification).isEqualTo(notification2);
    }

    @Test
    void testGetNewestNotificationForShipValidLaterTime() throws NotificationNotFoundException, JsonProcessingException {
        Notification notification4 = new Notification(
                new CurrentShipDetails(
                        new AnomalyInformation(1F, "explanation", OffsetDateTime.of(2005, 01, 27, 0, 3, 0, 0, ZoneOffset.ofHours(0)), 2L),
                        new AISSignal(2L, 0F, 0F, 0F, 0F, 0F, dateTime, "KLAIPEDA"),
                        new MaxAnomalyScoreDetails()
                )
        );
        when(notificationRepository.findNotificationByShipID(2L)).thenReturn(List.of(notification2, notification4));
        Notification notification = notificationService.getNewestNotificationForShip(2L);
        assertThat(notification).isEqualTo(notification4);
    }

    @Test
    void testGetNewestNotificationForShipException() throws NotificationNotFoundException {
        assertThrows(NotificationNotFoundException.class, () -> notificationService.getNewestNotificationForShip(3L));
    }

    @Test
    void getAllNotifications() throws NotificationNotFoundException {
        assertThat(notificationService.getAllNotifications()).isEqualTo(List.of(notification1, notification2, notification3));
    }

    @Test
    void testMarkNotificationAsRead() throws NotificationNotFoundException {
        assertDoesNotThrow(() -> notificationService.markNotificationAsRead(0L));
        notification1.setRead(true);
        verify(notificationRepository, times(1)).save(notification1);
    }

    @Test
    void testMarkNotificationAsReadFail() throws NotificationNotFoundException {
        assertThrows(NotificationNotFoundException.class, () -> notificationService.markNotificationAsRead(4L));
        verify(notificationRepository, times(0)).save(notification3);
    }
}
