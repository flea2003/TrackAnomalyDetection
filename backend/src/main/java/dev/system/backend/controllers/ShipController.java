package dev.system.backend.controllers;

import dev.system.backend.models.AIS;
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
    public AIS getShipAISInfo(@PathVariable String hash){
        return this.shipService.fetchAISSignal(hash);
    }

    @GetMapping("ship/list")
    public List<AIS> getShipsAISInfo(@RequestBody List<String> listHashes){
        return this.shipService.fetchAISSignals(listHashes);
    }

    @DeleteMapping("ship/{id}")
    public void removeShip(@PathVariable String id){
        this.shipService.removeShip(id);
    }
}
