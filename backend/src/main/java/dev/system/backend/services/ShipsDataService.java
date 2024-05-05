package dev.system.backend.services;

import dev.system.backend.commons.AIS;
import dev.system.backend.commons.AnomalyInformation;
import dev.system.backend.commons.PipelineObject;
import dev.system.backend.commons.Ship;
import dev.system.backend.repositories.ShipRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ShipsDataService {

    private PipelineObject pipelineObject;

    /**
     * Constructor for service class
     *
     * @param pipelineObject object that is responsible for managing and handling the stream of data and
     * anomaly information computation
     */
    @Autowired
    public ShipsDataService(PipelineObject pipelineObject){
        this.pipelineObject = pipelineObject;
    }

    /**
     * Computes the current AIS information for a specified ship
     *
     * @param id the id of a ship
     * @return current AIS information for a specified ship
     */
    public AIS getCurrentAISInformation(long id){
        HashMap<Integer, Integer> map = pipelineObject.returnHashMap();
        return new AIS();
    }

    /**
     * Computes the current anomaly information for a specified ship
     *
     * @param shipId the id of the ship
     * @return anomaly information for a specified ship
     */
    public AnomalyInformation getCurrentAnomalyInformation(long shipId){
        HashMap<Integer, Integer> map = pipelineObject.returnHashMap();
        return new AnomalyInformation(0.0F, "");
    }


    /**
     * Computes the current AIS data for all ships
     *
     * @return the current AIS data for all ships
     */
    public List<AIS> getCurrentAISInformationOfAllShips(){
        HashMap<Integer, Integer> map = pipelineObject.returnHashMap();
        return new ArrayList<>();
    }

    /**
     * Computes the current anomaly information of all ships
     *
     * @return a list of anomaly information objects for all ships
     */
    public List<AnomalyInformation> getCurrentAnomalyInformationOfAllShips(){
        HashMap<Integer, Integer> map = pipelineObject.returnHashMap();
        return new ArrayList<>();
    }
}
