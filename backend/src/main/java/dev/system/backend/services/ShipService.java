package dev.system.backend.services;

import dev.system.backend.models.AIS;
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

    public AIS fetchAISSignal(String hash){
        AIS returnValue = this.shipRepository.findById(hash).orElse(null);
        if(returnValue == null){
            throw new EntityNotFoundException("Ship with ID: "+hash+" does not exist");
        }
        return returnValue;
    }

    public List<AIS> fetchAISSignals(List<String> hashList){
        List<AIS> returnValue;
        try{
            returnValue = this.shipRepository.findAllById(hashList);
        }
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
            returnValue = null;
        }
        return returnValue;
    }

    public void removeShip(String hash){
        try{this.shipRepository.deleteById(hash);}
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }
}
