package dev.system.backend.controllers;

import dev.system.backend.commons.AIS;
import dev.system.backend.commons.AnomalyScore;
import dev.system.backend.services.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShipController {

    private final ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService){
        this.shipService = shipService;
    }

    @GetMapping("ship/{id}")
    public AIS getShipInfo(@PathVariable long id){
        return this.shipService.fetchLastSignalShip(id);
    }

    @GetMapping("anomaly/{id}")
    public AnomalyScore getAnomalyScore(@PathVariable long id) {return this.shipService.fetchLastAnomalyScore(id);}

    @GetMapping("anomaly/window/{id}")
    public List<AnomalyScore> getWindowAnomalyScore(@PathVariable long id, @RequestBody int windowSize){
        return this.shipService.fetchLastAnomalyScoreWindowShip(id, windowSize);
    }
    @GetMapping("ship/window/{id}")
    public List<AIS> getWindowAIS(@PathVariable long id, @RequestBody int windowSize){
        return this.shipService.fetchLastSignalWindowShip(id, windowSize);
    }

    @GetMapping("ship/group")
    public List<AIS> getShipsInfo(@RequestBody List<Long> shipIds){
        return this.shipService.fetchShipsInfo(shipIds);
    }

    @GetMapping("anomaly/group")
    public List<AnomalyScore> getShipsAnomalyScore(@RequestBody List<Long> shipIds){
        return this.shipService.fetchShipsAnomalyScore(shipIds);
    }

    @DeleteMapping("ship/{id}")
    public void removeShip(@PathVariable long id){
        this.shipService.removeShip(id);
    }
}
