package sp.repositories;

import org.aspectj.weaver.ast.Not;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp.model.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * SQL query that extracts a list of notifications for a particular ship
     *
     * @param shipHash hash value of the ship
     * @return list of notifications that correspond to that ship
     */
    List<Notification> findByShipHash(String shipHash);
}
