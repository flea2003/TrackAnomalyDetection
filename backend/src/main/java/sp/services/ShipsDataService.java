package sp.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;
import sp.pipeline.AnomalyDetectionPipeline;


@Service
public class ShipsDataService {

    private final AnomalyDetectionPipeline anomalyDetectionPipeline;

    /**
     * Constructor for service class.
     *
     * @param anomalyDetectionPipeline object that is responsible for managing and handling the stream of data and
     *     anomaly information computation
     */
    @Autowired
    public ShipsDataService(AnomalyDetectionPipeline anomalyDetectionPipeline) {
        this.anomalyDetectionPipeline = anomalyDetectionPipeline;
        anomalyDetectionPipeline.runPipeline();
    }

    /**
     * Computes the current AIS information for a specified ship.
     *
     * @param shipId the id of a ship
     * @return current AIS information for a specified ship
     */
    public AISSignal getCurrentAISInformation(String shipId) throws NotExistingShipException, PipelineException {
        HashMap<String, CurrentShipDetails> shipsInfo = anomalyDetectionPipeline.getCurrentScores();
        CurrentShipDetails shipDetails = shipsInfo.get(shipId);

        if (shipDetails == null) {
            throw new NotExistingShipException("Couldn't find such ship.");
        }

        List<ShipInformation> pastSignals = shipDetails.getPastInformation();

        int size = pastSignals.size();
        return pastSignals.get(size - 1).getAisSignal();
    }

    /**
     * Computes the current anomaly information for a specified ship.
     *
     * @param shipId the id of the ship
     * @return anomaly information for a specified ship
     */
    public AnomalyInformation getCurrentAnomalyInformation(String shipId)
            throws NotExistingShipException, PipelineException {

        HashMap<String, CurrentShipDetails> shipsInfo = anomalyDetectionPipeline.getCurrentScores();
        CurrentShipDetails shipDetails = shipsInfo.get(shipId);

        if (shipDetails == null) {
            throw new NotExistingShipException("Couldn't find such ship.");
        }

        return shipDetails.getAnomalyInformation();

    }


    /**
     * Computes the current AIS data for all ships.
     *
     * @return the current AIS data for all ships
     */
    public List<AISSignal> getCurrentAISInformationOfAllShips() throws PipelineException {
        HashMap<String, CurrentShipDetails> shipsInfo = anomalyDetectionPipeline.getCurrentScores();
        List<AISSignal> aisInformation = new ArrayList<>();

        for (CurrentShipDetails currentShipDetails : shipsInfo.values()) {
            List<ShipInformation> pastSignals = currentShipDetails.getPastInformation();
            if (pastSignals != null && !pastSignals.isEmpty()) {
                if (pastSignals.get(pastSignals.size() - 1).getAisSignal() == null) {
                    System.out.println("NULL AIS VALUE");
                }
                aisInformation.add(pastSignals.get(pastSignals.size() - 1).getAisSignal());
            }
        }

        return aisInformation;
    }

    /**
     * Computes the current anomaly information of all ships.
     *
     * @return a list of anomaly information objects for all ships
     */
    public List<AnomalyInformation> getCurrentAnomalyInformationOfAllShips() throws PipelineException {
        HashMap<String, CurrentShipDetails> shipsInfo = anomalyDetectionPipeline.getCurrentScores();
        List<AnomalyInformation> anomalyInformation = new ArrayList<>();

        for (Map.Entry<String, CurrentShipDetails> entry : shipsInfo.entrySet()) {
            if (shipsInfo.get(entry.getKey()).getAnomalyInformation() == null) {
                System.out.println("NULL ANOMALY VALUE");
            }
            anomalyInformation.add(shipsInfo.get(entry.getKey()).getAnomalyInformation());
        }

        return anomalyInformation;
    }
}
