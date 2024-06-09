package sp.services;

import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.exceptions.DatabaseException;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.exceptions.PipelineStartingException;
import sp.model.CurrentShipDetails;
import sp.pipeline.AnomalyDetectionPipeline;
import java.util.HashMap;
import java.util.List;
import sp.utils.sql.QueryExecutor;

@Service
public class ShipsDataService {
    private final AnomalyDetectionPipeline anomalyDetectionPipeline;
    private final QueryExecutor queryExecutor;
    private final Integer activeTime = 30;


    /**
     * Constructor for service class.
     *
     * @param queryExecutor object which will execute the queries
     * @param anomalyDetectionPipeline object that is responsible for managing and handling the stream of data and
     *     anomaly information computation
     */
    @Autowired
    public ShipsDataService(AnomalyDetectionPipeline anomalyDetectionPipeline, QueryExecutor queryExecutor) {
        this.anomalyDetectionPipeline = anomalyDetectionPipeline;
        this.queryExecutor = queryExecutor;
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
            .getFilteredShipDetails(x -> {
                if (x.getCurrentAISSignal() == null) {
                    return false;
                } else {
                    return Duration.between(x.getCurrentAISSignal().getReceivedTime(), currentTime).toMinutes() <= activeTime;
                }
            });
        return shipsInfo.values().stream().toList();
    }

    /**
     * Queries the Druid database in order to retrieve the list of CurrentShipDetails of the corresponding ship.
     *
     * @param id the id of the ship on which we will query our data
     * @return the list of the retrieved details
     * @throws DatabaseException in case the query doesn't succeed
     */
    public List<CurrentShipDetails> getHistoryOfShip(long id) throws DatabaseException {
        return queryExecutor.executeQueryOneLong(id, "src/main/resources/db/history.sql", CurrentShipDetails.class);
    }
}
