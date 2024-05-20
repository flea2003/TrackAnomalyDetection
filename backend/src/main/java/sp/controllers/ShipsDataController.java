package sp.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.model.CurrentShipDetails;
import sp.services.ShipsDataService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
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
        } catch (PipelineException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves the current ship details for all ships in the system.
     *
     * @return a list of CurrentShipDetails objects for all ships
     */
    @GetMapping("/ships/details")
    public ResponseEntity<List<CurrentShipDetails>> getCurrentShipDetails() {
        try {
            return ResponseEntity.ok(this.shipsDataService.getCurrentShipDetails());
        } catch (PipelineException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
