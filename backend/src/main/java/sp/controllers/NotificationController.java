package sp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.model.Notification;
import sp.services.NotificationService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {
    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Gets the current AIS information of a specified ship.
     *
     * @return AIS class object of the ship
     */
    @GetMapping("/notifications/ships")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        try {
            return ResponseEntity.ok(this.notificationService.getAllNotifications());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets the current AIS information of a specified ship.
     *
     * @return AIS class object of the ship
     */
    @GetMapping("/notifications/ships")
    public ResponseEntity<Notification> getNotification(@RequestParam("id") long id) {
        try {
            return ResponseEntity.ok(this.notificationService.getNotification(id));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
