package sp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp.model.Notification;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * SQL query that extracts a list of notifications for a particular ship.
     *
     * @param shipID id value of the ship
     * @return list of notifications that correspond to that ship
     */
    List<Notification> findNotificationByShipID(Long shipID);
}
