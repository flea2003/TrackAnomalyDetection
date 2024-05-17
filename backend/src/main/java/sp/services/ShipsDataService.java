package sp.services;

import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
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
        try {
            AISSignal currentAISSignal = anomalyDetectionPipeline.getCurrentAISSignals().get(shipId);

            if (currentAISSignal == null) {
                throw new NotExistingShipException("Couldn't find such ship.");
            }

            return currentAISSignal;

        } catch (Exception e) {
            if (e instanceof NotExistingShipException) {
                throw (NotExistingShipException) e;
            } else if (e instanceof PipelineException) {
                throw (PipelineException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Computes the current anomaly information for a specified ship.
     *
     * @param shipId the id of the ship
     * @return anomaly information for a specified ship
     */
    public AnomalyInformation getCurrentAnomalyInformation(String shipId)
            throws NotExistingShipException, PipelineException {

        try {
            AnomalyInformation currentAnomalyInfo = anomalyDetectionPipeline.getCurrentScores().get(shipId);

            if (currentAnomalyInfo == null) {
                throw new NotExistingShipException("Couldn't find such ship.");
            }

            return currentAnomalyInfo;
        } catch (Exception e) {
            if (e instanceof NotExistingShipException) {
                throw (NotExistingShipException) e;
            } else if (e instanceof PipelineException) {
                throw (PipelineException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Computes the current AIS data for all ships.
     *
     * @return the current AIS data for all ships
     */
    public List<AISSignal> getCurrentAISInformationOfAllShips() throws PipelineException {
        try {
            HashMap<String, AISSignal> shipsInfo = anomalyDetectionPipeline.getCurrentAISSignals();

            return shipsInfo.values().stream().toList();
        } catch (Exception e) {
            if (e instanceof PipelineException) {
                throw (PipelineException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Computes the current anomaly information of all ships.
     *
     * @return a list of anomaly information objects for all ships
     */
    public List<AnomalyInformation> getCurrentAnomalyInformationOfAllShips() throws PipelineException {
        try {
            HashMap<String, AnomalyInformation> shipsInfo = anomalyDetectionPipeline.getCurrentScores();

            return shipsInfo.values().stream().toList();
        } catch (Exception e) {
            if (e instanceof PipelineException) {
                throw (PipelineException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
