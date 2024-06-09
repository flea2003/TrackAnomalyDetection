package sp.services;

import org.springframework.stereotype.Service;
import sp.exceptions.NotificationNotFoundException;
import sp.model.Notification;
import sp.pipeline.parts.notifications.NotificationExtractor;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationExtractor notificationExtractor;

    /**
     * Constructor for notification service class.
     *
     * @param notificationExtractor object that accesses the notification data
     */
    public NotificationService(NotificationExtractor notificationExtractor) {
        this.notificationExtractor = notificationExtractor;
    }

    /**
     * Gets all notifications from the "database".
     *
     * @return a list of all notifications
     */
    public List<Notification> getAllNotifications() {
        return notificationExtractor.getAllNotifications();
    }

    /**
     * Gets a particular notification from the database.
     *
     * @param id id of the notification
     * @return notification object
     */
    public Notification getNotificationById(Long id) throws NotificationNotFoundException {
        Notification notification = notificationExtractor.getNotificationById(id);
        if (notification != null) {
            return notification;
        } else throw new NotificationNotFoundException();
    }

    /**
     * Gets all notifications for a particular ship.
     *
     * @param shipID internal id of the ship
     * @return a list of notifications for a ship
     */
    public List<Notification> getAllNotificationForShip(Long shipID) {
        return notificationExtractor
                .getAllNotifications()
                .stream()
                .filter(notification -> notification.getShipID().equals(shipID))
                .toList();
    }
}
