package sp.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sp.exceptions.NotificationNotFoundException;
import sp.model.Notification;
import sp.repositories.NotificationRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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
    public Notification getNotificationById(Long id) throws NotificationNotFoundException {
        Optional<Notification> notification = notificationRepository.findById(id);
        if (notification.isPresent()) {
            return notification.get();
        } else throw new NotificationNotFoundException();
    }

    /**
     * Method for adding a new notification to the database.
     *
     * @param notification notification object
     * @return the added notification
     */
    public Notification addNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * Gets all notifications for a particular ship.
     *
     * @param shipID internal id of the ship
     * @return a list of notifications for a ship
     */
    public List<Notification> getAllNotificationForShip(Long shipID) {
        return notificationRepository.findNotificationByShipID(shipID);
    }

    /**
     * Method used by the notification pipeline: it fetches the newest notification for a particular ship.
     *
     * @param shipID id of the ship
     * @return notification object of the newest ship
     */
    @Transactional
    public Notification getNewestNotificationForShip(Long shipID) throws NotificationNotFoundException {

        List<Notification> allNotifications = notificationRepository.findNotificationByShipID(shipID);
        if (allNotifications.isEmpty()) throw new NotificationNotFoundException();

        Notification result = allNotifications.get(0);
        for (Notification notification : allNotifications) {

            OffsetDateTime currentTime = notification.getCurrentShipDetails().getCurrentAnomalyInformation()
                    .getCorrespondingTimestamp();
            OffsetDateTime oldestTime = result.getCurrentShipDetails().getCurrentAnomalyInformation()
                    .getCorrespondingTimestamp();

            if (currentTime.isAfter(oldestTime) || currentTime.isEqual(oldestTime)) {
                result = notification;
            }
        }

        return result;
    }
}
