package sp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.AnomalyInformation;
import sp.model.NotificationsList;
import sp.model.ShipInformation;
import sp.repositories.NotificationsRepository;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationsRepository notificationsRepository;

    @Autowired
    public NotificationService(NotificationsRepository notificationsRepository ) {
        this.notificationsRepository = notificationsRepository;
    }

    public ShipInformation getNotification(String id) {
        List<ShipInformation> allNotifications = notificationsRepository.getById(id).getNotifications();
        if (allNotifications.size() == 0)
            return new ShipInformation(id, new AnomalyInformation(0F, "NOT_COMPUTED",
                    null, null), null);

        else return allNotifications.get(allNotifications.size() - 1);
    }

    public void addNotification(ShipInformation notification) {
        NotificationsList notifications = notificationsRepository.getById(notification.getShipHash());
        notifications.addNotification(notification);
        notificationsRepository.save(notifications);
    }
}
