package dev.system.backend.services;

import dev.system.backend.commons.AIS;
import dev.system.backend.commons.AnomalyScore;
import dev.system.backend.commons.Ship;
import dev.system.backend.repositories.ShipRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipService {
    private ShipRepository shipRepository;

    @Autowired
    public ShipService(ShipRepository shipRepository){
        this.shipRepository = shipRepository;
    }


    public Ship fetchShipInstance(long id){
        Ship returnValue = this.shipRepository.findById(id).orElse(null);
        if(returnValue == null){
            throw new EntityNotFoundException("Ship with ID"+id+"does not exist");
        }
        return returnValue;
    }


    public AIS fetchLastSignalShip(long shipId){
        Ship ship = this.shipRepository.findById(shipId).orElse(null);
        if(ship == null){
            throw new EntityNotFoundException("Ship with ID: "+shipId+" does not exist");
        }
        AIS returnValue = ship.getLastSignal();
        return returnValue;
    }

    public AnomalyScore fetchLastAnomalyScore(long shipId){
        Ship ship = this.shipRepository.findById(shipId).orElse(null);
        if(ship == null){
            throw new EntityNotFoundException("Ship with ID: "+shipId+" does not exist");
        }
        AnomalyScore returnValue = ship.getLastAnomalyScore();
        return returnValue;
    }

    public List<AIS> fetchLastSignalWindowShip(long shipId, int windowSize){
        Ship ship = this.shipRepository.findById(shipId).orElse(null);
        if(ship==null){
            throw new EntityNotFoundException("Ship with ID: "+shipId+" does not exist");
        }
        List<AIS> returnValue;
        try{
            returnValue = ship.getWindowedSignalHistory(windowSize);
        }catch (IndexOutOfBoundsException e){
            returnValue = null;
        }
        return returnValue;
    }

    public List<AnomalyScore> fetchLastAnomalyScoreWindowShip(long shipId, int windowSize){
        Ship ship = this.shipRepository.findById(shipId).orElse(null);
        if(ship==null){
            throw new EntityNotFoundException("Ship with ID: "+shipId+" does not exist");
        }
        List<AnomalyScore> returnValue;
        try{
            returnValue = ship.getWindowedAnomalyScoreHistory(windowSize);
        }catch (IndexOutOfBoundsException e){
            returnValue = null;
        }
        return returnValue;
    }

    public void removeShip(long shipId){
        try{this.shipRepository.deleteById(shipId);}
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }

    public List<AIS> fetchShipsInfo(List<Long> shipIds){
        List<AIS> returnValue;
        try{
            List<Ship> ships = this.shipRepository.findAllById(shipIds);
            returnValue = ships.stream().map(x -> x.getLastSignal()).toList();
        }catch (IllegalArgumentException e){
            returnValue = null;
        }
        return returnValue;
    }

    public List<AnomalyScore> fetchShipsAnomalyScore(List<Long> shipIds){
        List<AnomalyScore> returnValue;
        try{
            List<Ship> ships = this.shipRepository.findAllById(shipIds);
            returnValue = ships.stream().map(x -> x.getLastAnomalyScore()).toList();
        }catch (IllegalArgumentException e){
            returnValue = null;
        }
        return returnValue;
    }
}
