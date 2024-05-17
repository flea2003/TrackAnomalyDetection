package sp.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotFoundNotificationException;
import sp.model.Notification;
import sp.repositories.NotificationRepository;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    /**
     * Constructor for notification service class.
     *
     * @param notificationRepository repository for all notifications
     */
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Gets all notifications from the database.
     *
     * @return a list of all notifications from the database
     */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    /**
     * Gets a particular notification from the database.
     *
     * @param id id of the notification
     * @return notification object
     */
    public Notification getNotificationById(Long id) throws NotFoundNotificationException {
        return notificationRepository.findById(id).orElseThrow(NotFoundNotificationException::new);
    }

    /**
     * Method for adding a new notification to the database.
     *
     * @param notification notification object
     */
    public void addNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    /**
     * Temporary method until there is no timestamp database.
     *
     * @param anomalyInformation object that corresponds to the new notification
     */
    public void addNotification(AnomalyInformation anomalyInformation) {
        notificationRepository.save(new Notification(anomalyInformation));
    }

    public List<Notification> getAllNotificationForShip(String shipHash) {
        return notificationRepository.findByShipHash(shipHash);
    }

    /**
     * Method used by the notification pipeline: it fetches the newest notification for a particular ship.
     *
     * @param shipHash hash of the ship
     * @return notification object of the newest ship
     */
    @Transactional
    public Notification getNewestNotificationForShip(String shipHash) throws NotFoundNotificationException {

        List<Notification> allNotifications = notificationRepository.findByShipHash(shipHash);
        if (allNotifications.isEmpty()) throw new NotFoundNotificationException();

        Notification result = allNotifications.get(0);
        for (Notification notification : allNotifications) {
            if (notification.getCorrespondingTimestamp().isAfter(result.getCorrespondingTimestamp())) {
                result = notification;
            }
        }

        return result;
    }
}
