package sp.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.AnomalyInformation;
import sp.model.Notification;
import sp.model.NotificationsList;
import sp.model.ShipInformation;
import sp.repositories.NotificationRepository;
import sp.repositories.NotificationsListRepository;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    public NotificationService(NotificationRepository notificationRepository) {
       this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Notification getNotification(Long id) {
        return notificationRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }
}
