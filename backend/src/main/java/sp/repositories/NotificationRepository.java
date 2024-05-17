package sp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
