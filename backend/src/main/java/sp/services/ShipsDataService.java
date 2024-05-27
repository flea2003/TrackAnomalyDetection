package sp.services;

import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.exceptions.PipelineStartingException;
import sp.model.CurrentShipDetails;
import sp.pipeline.AnomalyDetectionPipeline;
import java.util.HashMap;
import java.util.List;


@Service
public class ShipsDataService {

    private final AnomalyDetectionPipeline anomalyDetectionPipeline;
    private final Integer activeTime = 30;

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
     * Retrieves the current extensive information for a specified ship.
     *
     * @param shipId the id of the ship
     * @return CurrentShipDetails instance encapsulating the current extensive information of a ship
     */
    public CurrentShipDetails getIndividualCurrentShipDetails(Long shipId)
            throws NotExistingShipException, PipelineException, PipelineStartingException {
        CurrentShipDetails anomalyInfo = anomalyDetectionPipeline.getShipInformationExtractor()
            .getCurrentShipDetails().get(shipId);
        if (anomalyInfo == null) {
            throw new NotExistingShipException("Couldn't find such ship.");
        }
        return anomalyInfo;
    }


    /**
     * Retrieves the current extensive information for all ships in the system.
     *
     * @return the CurrentShipDetails instances corresponding to all ships
     */
    public List<CurrentShipDetails> getCurrentShipDetails() throws PipelineException, PipelineStartingException {
        OffsetDateTime currentTime = OffsetDateTime.now();
        HashMap<Long, CurrentShipDetails> shipsInfo = anomalyDetectionPipeline.getShipInformationExtractor()
            .getFilteredShipDetails(x -> Duration.between(x.getCurrentAISSignal().getReceivedTime(),
                currentTime).toMinutes() <= activeTime);
        return shipsInfo.values().stream().toList();
    }

}
