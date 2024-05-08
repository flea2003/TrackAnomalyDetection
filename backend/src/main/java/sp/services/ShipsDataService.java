package sp.services;

import sp.dtos.AnomalyInformation;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.pipeline.AnomalyDetectionPipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param shipId the id of a ship
     * @return current AIS information for a specified ship
     */
    public AISSignal getCurrentAISInformation(String shipId) throws Exception{
        HashMap<String, CurrentShipDetails> map = anomalyDetectionPipeline.getCurrentScores();
        CurrentShipDetails shipDetails = map.get(shipId);

        if(shipDetails == null){
            throw new Exception("Couldn't find such ship");
        }

        List<AISSignal> pastSignals = shipDetails.getPastSignals();

        int size = pastSignals.size();
        if(size == 0) {
            throw new Exception("There are no past signals");
        }
        return pastSignals.get(size - 1);
    }

    /**
     * Computes the current anomaly information for a specified ship
     *
     * @param shipId the id of the ship
     * @return anomaly information for a specified ship
     */
    public AnomalyInformation getCurrentAnomalyInformation(String shipId) throws Exception{
        HashMap<String, CurrentShipDetails> map = anomalyDetectionPipeline.getCurrentScores();
        CurrentShipDetails shipDetails = map.get(shipId);

        if(shipDetails == null){
            throw new Exception("Couldn't find such ship");
        }

        return new AnomalyInformation(shipDetails.getScore(), shipId);
    }


    /**
     * Computes the current AIS data for all ships
     *
     * @return the current AIS data for all ships
     */
    public List<AISSignal> getCurrentAISInformationOfAllShips(){
        HashMap<String, CurrentShipDetails> map = anomalyDetectionPipeline.getCurrentScores();
        List<AISSignal>aisInformation = new ArrayList<>();

        for(Map.Entry<String, CurrentShipDetails>entry : map.entrySet()){
            aisInformation.addAll(entry.getValue().getPastSignals());
        }

        return aisInformation;
    }

    /**
     * Computes the current anomaly information of all ships
     *
     * @return a list of anomaly information objects for all ships
     */
    public List<AnomalyInformation> getCurrentAnomalyInformationOfAllShips(){
        HashMap<String, CurrentShipDetails> map = anomalyDetectionPipeline.getCurrentScores();
        List<AnomalyInformation>anomalyInformation = new ArrayList<>();

        for(Map.Entry<String, CurrentShipDetails>entry : map.entrySet()){
            anomalyInformation.add(new AnomalyInformation(entry.getValue().getScore(), entry.getKey()));
        }

        return anomalyInformation;
    }
}
