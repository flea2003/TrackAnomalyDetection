package sp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp.dtos.AnomalyInformation;
import sp.model.NotificationsList;

import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<NotificationsList, String> {
}
