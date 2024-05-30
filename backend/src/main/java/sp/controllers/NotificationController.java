package sp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp.exceptions.NotificationNotFoundException;
import sp.model.Notification;
import sp.services.NotificationService;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Constructor for notifications controller class.
     *
     * @param notificationService service class
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Gets all the notifications in the database (this will likely be changed in the future to get a
     * certain portion of them).
     *
     * @return a list of all notifications in the database
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(this.notificationService.getAllNotifications());
    }

    /**
     * Gets a certain notification from the database.
     *
     * @param id notification id
     * @return needed Notification object
     */
    @GetMapping("/notifications/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(this.notificationService.getNotificationById(id));
        } catch (NotificationNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets all notifications for a particular ship.
     *
     * @param id id of the ship
     * @return a list of notifications that correspond to a particular ship
     */
    @GetMapping("/notifications/ship/{id}")
    public ResponseEntity<List<Notification>> getAllNotificationsForShip(@PathVariable Long id) {
        return ResponseEntity.ok(this.notificationService.getAllNotificationForShip(id));
    }
}
