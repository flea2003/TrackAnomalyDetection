package sp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp.model.Notification;
import sp.services.NotificationService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Constructor for notifications controller class
     *
     * @param notificationService service class
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Gets all the notifications in the database (this will likely be changed in the future to get a
     * certain portion of them)
     *
     * @return a list of all notifications in the database
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        try {
            return ResponseEntity.ok(this.notificationService.getAllNotifications());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets a certain notification from the database
     *
     * @return needed Notification object
     */
    @GetMapping("/notifications/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(this.notificationService.getNotificationById(id));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets all notifications for a particular ship
     *
     * @param shipHash hash of the ship
     * @return a list of notifications that correspond to a particular ship
     */
    @GetMapping("/notifications/ship")
    public ResponseEntity<List<Notification>> getAllNotificationsForShip(@RequestParam String shipHash) {
        try {
            return ResponseEntity.ok(this.notificationService.getAllNotificationForShip(shipHash));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
