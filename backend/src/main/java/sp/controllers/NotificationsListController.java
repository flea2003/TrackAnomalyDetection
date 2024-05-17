package sp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.model.Notification;
import sp.services.NotificationService;
import sp.services.NotificationsListService;

import java.util.List;

@RestController
public class NotificationsListController {

    private final NotificationsListService notificationsListService;

    /**
     * Constructor for the controller.
     *
     * @param notificationsListService service class for the controller
     */
    @Autowired
    public NotificationsListController(NotificationsListService notificationsListService) {
        this.notificationsListService = notificationsListService;
    }



    /**
     * Gets the current AIS information of a specified ship.
     *
     * @param id the id of the ship
     * @return AIS class object of the ship
     */
    @GetMapping("/notifications/ships/{id}/newest")
    public ResponseEntity<List<Notification>> getAllNotifications(@PathVariable String id) {
        try {
            return ResponseEntity.ok(this.notificationsListService.getAllNotifications(id));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets the current AIS information of a specified ship.
     *
     * @param id the id of the ship
     * @return AIS class object of the ship
     */
    @GetMapping("/notifications/ships/{id}/newest")
    public ResponseEntity<Notification> getNewestNotification(@PathVariable String id) {
        try {
            return ResponseEntity.ok(this.notificationsListService.getNewestNotification(id));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
