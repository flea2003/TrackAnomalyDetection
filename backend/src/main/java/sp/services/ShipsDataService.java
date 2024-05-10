package sp.services;

import sp.dtos.AnomalyInformation;
import sp.dtos.AISSignal;
import sp.model.CurrentShipDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.pipeline.AnomalyDetectionPipeline;

import java.util.HashMap;
import java.util.List;

@Service
public class ShipsDataService {

    private AnomalyDetectionPipeline anomalyDetectionPipeline;

    /**
     * Constructor for service class
     *
     * @param anomalyDetectionPipeline object that is responsible for managing and handling the stream of data and
     * anomaly information computation
     */
    @Autowired
    public ShipsDataService(AnomalyDetectionPipeline anomalyDetectionPipeline){
        this.anomalyDetectionPipeline = anomalyDetectionPipeline;
        anomalyDetectionPipeline.runPipeline();
    }

    /**
     * Computes the current AIS information for a specified ship
     *
     * @param id the id of a ship
     * @return current AIS information for a specified ship
     */
    public AISSignal getCurrentAISInformation(String id){
        HashMap<String, CurrentShipDetails> map = anomalyDetectionPipeline.getCurrentScores();
        System.out.println("Map is: " + map);
        return null;
    }

    /**
     * Computes the current anomaly information for a specified ship
     *
     * @param shipId the id of the ship
     * @return anomaly information for a specified ship
     */
    public AnomalyInformation getCurrentAnomalyInformation(String shipId){
        return null;
    }


    /**
     * Computes the current AIS data for all ships
     *
     * @return the current AIS data for all ships
     */
    public List<AISSignal> getCurrentAISInformationOfAllShips(){
        return null;
    }

    /**
     * Computes the current anomaly information of all ships
     *
     * @return a list of anomaly information objects for all ships
     */
    public List<AnomalyInformation> getCurrentAnomalyInformationOfAllShips(){
        return null;
    }
}
