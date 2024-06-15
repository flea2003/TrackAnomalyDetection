package sp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sp.dtos.TrajectoryObject;
import sp.exceptions.DatabaseException;
import sp.exceptions.NotExistingShipException;
import sp.model.CurrentShipDetails;
import sp.services.ShipsDataService;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ShipsDataController {

    private final ShipsDataService shipsDataService;

    /**
     * Constructor for the controller.
     *
     * @param shipsDataService service class for the controller
     */
    @Autowired
    public ShipsDataController(ShipsDataService shipsDataService) {
        this.shipsDataService = shipsDataService;
    }

    /**
     * Retrieves the current ship details for a particular ship.
     *
     * @param id the id of the ship
     * @return CurrentShipDetails object for a specified ship
     */
    @GetMapping("/ships/details/{id}")
    public ResponseEntity<CurrentShipDetails> getIndividualCurrentShipDetails(
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(this.shipsDataService.getIndividualCurrentShipDetails(id));
        } catch (NotExistingShipException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves the current ship details for all ships in the system.
     *
     * @return a list of CurrentShipDetails objects for all ships
     */
    @GetMapping("/ships/details")
    public ResponseEntity<List<CurrentShipDetails>> getCurrentShipDetails() {
        return ResponseEntity.ok(this.shipsDataService.getCurrentShipDetailsOfAllShips());
    }

    /**
     * Retrieves all the CurrentShipDetails of a corresponding ship.
     *
     * @param id the id of the ships that we retrieve the information of
     * @return a list of CurrentShipDetails of the corresponding ship, or
     *      500 error code in case the sql query fails.
     */
    @GetMapping("/ships/history/{id}")
    public ResponseEntity<List<CurrentShipDetails>> getHistoryOfShip(
        @PathVariable Long id
    ) {
        try {
            return ResponseEntity.ok(this.shipsDataService.getHistoryOfShip(id));
        } catch (DatabaseException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves sampled list of the CurrentShipDetails of a corresponding ship.
     *
     * @param id the id of the ships that we retrieve the information of
     * @return a list of CurrentShipDetails of the corresponding ship, or
     *      500 error code in case the sql query fails.
     */
    @GetMapping("/ships/history/sampled/{id}")
    public ResponseEntity<List<TrajectoryObject>> getSampledHistoryOfShip(
            @PathVariable Long id
    ) {
        try {
            return ResponseEntity.ok(this.shipsDataService.getSubsampledHistoryOfShip(id));
        } catch (DatabaseException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
