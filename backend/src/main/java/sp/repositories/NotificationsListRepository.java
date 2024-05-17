package sp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp.model.NotificationsList;

@Repository
public interface NotificationsListRepository extends JpaRepository<NotificationsList, String> {
}
