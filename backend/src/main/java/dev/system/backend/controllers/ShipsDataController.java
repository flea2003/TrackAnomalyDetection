package dev.system.backend.controllers;

import dev.system.backend.commons.AIS;
import dev.system.backend.commons.AnomalyInformation;
import dev.system.backend.services.ShipsDataService;
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
    @GetMapping("ships/ais/{id}")
    public AIS getCurrentAISInformation(@PathVariable long id){
        return this.shipsDataService.getCurrentAISInformation(id);
    }

    /**
     * Gets the anomaly score object of a specified ship.
     *
     * @param id the id of the ship
     * @return AnomalyInformation object for a specified ship
     */
    @GetMapping("ships/anomaly/{id}")
    public AnomalyInformation getCurrentAnomalyInformation(@PathVariable long id) {return this.shipsDataService.getCurrentAnomalyInformation(id);}

    /**
     * Gets the current AIS data for all ships
     *
     * @return the current AIS data for all ships
     */
    @GetMapping("ships/ais")
    public List<AIS> getCurrentAISInformationOfAllShips(){
        return this.shipsDataService.getCurrentAISInformationOfAllShips();
    }

    /**
     * Gets the current anomaly information of all ships
     *
     * @return a list of anomaly information objects for all ships
     */
    @GetMapping("ships/anomaly")
    public List<AnomalyInformation> getCurrentAnomalyInformationOfAllShips(){
        return this.shipsDataService.getCurrentAnomalyInformationOfAllShips();
    }
}
