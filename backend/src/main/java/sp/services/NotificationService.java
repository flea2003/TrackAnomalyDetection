package sp.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.AnomalyInformation;
import sp.model.NotificationsList;
import sp.model.ShipInformation;
import sp.repositories.NotificationsRepository;

import java.beans.Transient;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationsRepository notificationsRepository;

    @Autowired
    public NotificationService(NotificationsRepository notificationsRepository ) {
        this.notificationsRepository = notificationsRepository;
    }

    @Transactional
    public ShipInformation getNotification(String id) {
        Optional<NotificationsList> allNotifications = notificationsRepository.findById(id);
        if (allNotifications.isPresent()) {
            List<ShipInformation> notificationsList = allNotifications.get().getNotifications();
            if (notificationsList.isEmpty()) throw new EntityNotFoundException("List is empty");
            else return notificationsList.get(notificationsList.size() - 1);
        } else throw new EntityNotFoundException("ListNotification object is not found");
    }

    public void addNotification(ShipInformation notification) {
        if (notificationsRepository.findById(notification.getShipHash()).isEmpty()) {
            notificationsRepository.save(new NotificationsList(notification.getShipHash(), List.of(notification)));
        } else {
            NotificationsList notifications = notificationsRepository.findById(notification.getShipHash()).get();
            notifications.addNotification(notification);
            notificationsRepository.save(notifications);
        }
    }
}
