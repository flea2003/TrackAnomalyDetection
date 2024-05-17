package sp.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.AnomalyInformation;
import sp.model.Notification;
import sp.model.NotificationsList;
import sp.repositories.NotificationRepository;
import sp.repositories.NotificationsListRepository;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationsListService {
    private final NotificationsListRepository notificationsListRepository;

    @Autowired
    public NotificationsListService(NotificationsListRepository notificationsListRepository) {
        this.notificationsListRepository = notificationsListRepository;
    }

    @Transactional
    public Notification getNewestNotification(String id) {
        Optional<NotificationsList> allNotifications = notificationsListRepository.findById(id);
        if (allNotifications.isPresent()) {
            List<Notification> notificationsList = allNotifications.get().getNotifications();
            if (notificationsList.isEmpty()) throw new EntityNotFoundException("List is empty");
            else return notificationsList.get(notificationsList.size() - 1);
        } else throw new EntityNotFoundException("NotificationsList object is not found");
    }

    public void addNotification(Notification notification) {
        System.out.println("Gavau addNotification service klaseje:" + notification);
        if (notificationsListRepository.findById(notification.getShipHash()).isEmpty()) {
            notificationsListRepository.save(new NotificationsList(notification.getShipHash(), List.of(notification)));
        } else {
            NotificationsList notifications = notificationsListRepository.findById(notification.getShipHash()).get();
            notifications.addNotification(notification);
            notificationsListRepository.save(notifications);
        }
    }

    /**
     * Temproary method until there is no timestamp database
     * @param anomalyInformation
     */
    public void addNotification(AnomalyInformation anomalyInformation) {
        System.out.println("Gavau addNotification service klaseje:" + anomalyInformation);
        if (notificationsListRepository.findById(anomalyInformation.getShipHash()).isEmpty()) {
            notificationsListRepository.save(new NotificationsList(anomalyInformation.getShipHash(),
                    List.of(new Notification(anomalyInformation))));
        } else {
            NotificationsList notifications = notificationsListRepository.findById(anomalyInformation.getShipHash()).get();
            notifications.addNotification(new Notification(anomalyInformation));
            notificationsListRepository.save(notifications);
        }
    }

    public List<Notification> getAllNotifications(String hash) {
        return notificationsListRepository.findById(hash).get().getNotifications();
        // TODO deal with exceptions later
    }
}
