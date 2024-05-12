package sp.controllers;

import java.util.List;
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
import sp.services.ShipsDataService;


@RestController
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
     * Gets the current AIS information of a specified ship.
     *
     * @param id the id of the ship
     * @return AIS class object of the ship
     */
    @GetMapping("/ships/ais/{id}")
    public ResponseEntity<AISSignal> getCurrentAISInformation(@PathVariable String id) {
        try {
            return ResponseEntity.ok(this.shipsDataService.getCurrentAISInformation(id));
        } catch (NotExistingShipException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (PipelineException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets the anomaly score object of a specified ship.
     *
     * @param id the id of the ship
     * @return AnomalyInformation object for a specified ship
     */
    @GetMapping("/ships/anomaly/{id}")
    public ResponseEntity<AnomalyInformation> getCurrentAnomalyInformation(@PathVariable String id) {
        try {
            return ResponseEntity.ok(this.shipsDataService.getCurrentAnomalyInformation(id));
        } catch (NotExistingShipException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (PipelineException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets the current AIS data for all ships.
     *
     * @return the current AIS data for all ships
     */
    @GetMapping("/ships/ais")
    public ResponseEntity<List<AISSignal>> getCurrentAISInformationOfAllShips() {
        try {
            return ResponseEntity.ok(this.shipsDataService.getCurrentAISInformationOfAllShips());
        } catch (PipelineException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets the current anomaly information of all ships.
     *
     * @return a list of anomaly information objects for all ships
     */
    @GetMapping("/ships/anomaly")
    public ResponseEntity<List<AnomalyInformation>> getCurrentAnomalyInformationOfAllShips() {
        try {
            return ResponseEntity.ok(this.shipsDataService.getCurrentAnomalyInformationOfAllShips());
        } catch (PipelineException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
