package sp.services;

import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.TrajectoryObject;
import sp.exceptions.DatabaseException;
import sp.exceptions.NotExistingShipException;
import sp.model.CurrentShipDetails;
import sp.pipeline.AnomalyDetectionPipeline;
import sp.pipeline.parts.aggregation.extractors.ShipInformationExtractor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import sp.utils.sql.QueryExecutor;

@Service
public class ShipsDataService {
    private final AnomalyDetectionPipeline anomalyDetectionPipeline;
    private final QueryExecutor queryExecutor;

    private final ShipInformationExtractor shipInformationExtractor;
    private final Integer activeTime = 30;
    private final Integer subsamplingThreshold = 1000;

    /**
     * Constructor for service class.
     *
     * @param queryExecutor object which will execute the queries
     * @param anomalyDetectionPipeline object that is responsible for managing and handling the stream of data and
     *     anomaly information computation. Injecting it here makes sure that it is started when the service is created.
     * @param shipInformationExtractor object that is responsible for extracting ship information from a Kafka topic
     */
    @Autowired
    public ShipsDataService(AnomalyDetectionPipeline anomalyDetectionPipeline,
                            QueryExecutor queryExecutor,
                            ShipInformationExtractor shipInformationExtractor) {
        this.anomalyDetectionPipeline = anomalyDetectionPipeline;
        this.queryExecutor = queryExecutor;
        this.shipInformationExtractor = shipInformationExtractor;
        anomalyDetectionPipeline.runPipeline();
    }

    /**
     * Retrieves the current extensive information for a specified ship.
     *
     * @param shipId the id of the ship
     * @return CurrentShipDetails instance encapsulating the current extensive information of a ship
     */
    public CurrentShipDetails getIndividualCurrentShipDetails(Long shipId)
            throws NotExistingShipException {
        CurrentShipDetails anomalyInfo = shipInformationExtractor.getCurrentShipDetails().get(shipId);
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
    public List<CurrentShipDetails> getCurrentShipDetailsOfAllShips() {
        OffsetDateTime currentTime = OffsetDateTime.now();
        HashMap<Long, CurrentShipDetails> shipsInfo = shipInformationExtractor.getFilteredShipDetails(
                x -> x.getCurrentAISSignal() != null
                        && Duration.between(x.getCurrentAISSignal().getReceivedTime(), currentTime).toMinutes() <= activeTime
        );
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

    /**
     * Queries the Druid database in order to retrieve the list of CurrentShipDetails of the corresponding ship.
     * Then, subsamples the retrieved list in case it is too long.
     *
     * @param id the id of the ship on which we will query our data
     * @return the list of the retrieved details
     * @throws DatabaseException in case the query doesn't succeed
     */
    public List<TrajectoryObject> getSubsampledHistoryOfShip(long id) throws DatabaseException {
        List<TrajectoryObject> fullHistory = queryExecutor
                .executeQueryOneLong(id, "src/main/resources/db/sampledHistory.sql", TrajectoryObject.class);

        List<TrajectoryObject> result = new ArrayList<>();

        if (fullHistory.size() <= subsamplingThreshold)
            result = fullHistory;
        else {
            // Compute the ratio at which the list elements will be picked
            // (only every subsampleRation-th element will be picked)
            double subsampleRatio = (double) fullHistory.size() / (double) subsamplingThreshold;

            for (double i = 0; i < fullHistory.size() - 1; i += subsampleRatio) {
                result.add(fullHistory.get((int) i));
            }

            // Also, make sure that the very last signal is added
            result.add(fullHistory.get(fullHistory.size() - 1));
        }

        return result
                .stream()
                .sorted((x, y) -> -x.getAisSignal().getTimestamp().compareTo(y.getAisSignal().getTimestamp()))
                .toList();
    }
}
