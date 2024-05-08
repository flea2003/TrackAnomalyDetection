package sp.controllers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sp.dtos.AnomalyInformation;
import sp.model.AISSignal;
import sp.services.ShipsDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShipsDataController {

    private final ShipsDataService shipsDataService;

    /**
     * Constructor for the controller
     *
     * @param shipsDataService service class for the controller
     */
    @Autowired
    public ShipsDataController(ShipsDataService shipsDataService){
        this.shipsDataService = shipsDataService;
    }

    /**
     * Gets the current AIS information of a specified ship.
     *
     * @param id the id of the ship
     * @return AIS class object of the ship
     */
    @GetMapping("/ships/ais/{id}")
    public ResponseEntity<AISSignal> getCurrentAISInformation(@PathVariable String id){
        try{
            return ResponseEntity.ok(this.shipsDataService.getCurrentAISInformation(id));
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets the current AIS data for all ships
     *
     * @return the current AIS data for all ships
     */
    @GetMapping("/ships/ais")
    public List<AISSignal> getCurrentAISInformationOfAllShips(){
        return this.shipsDataService.getCurrentAISInformationOfAllShips();
    }

    /**
     * Gets the current anomaly information of all ships
     *
     * @return a list of anomaly information objects for all ships
     */
    @GetMapping("/ships/anomaly")
    public ResponseEntity<List<AnomalyInformation>> getCurrentAnomalyInformationOfAllShips(){
        return ResponseEntity.ok(this.shipsDataService.getCurrentAnomalyInformationOfAllShips());
    }
}
