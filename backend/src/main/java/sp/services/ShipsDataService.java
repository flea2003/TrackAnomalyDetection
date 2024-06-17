package sp.services;

import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.DatabaseExtractObject;
import sp.dtos.TrajectoryObject;
import sp.exceptions.DatabaseException;
import sp.exceptions.NotExistingShipException;
import sp.model.CurrentShipDetails;
import sp.pipeline.AnomalyDetectionPipeline;
import sp.pipeline.parts.aggregation.extractors.ShipInformationExtractor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sp.utils.sql.QueryExecutor;

@Service
public class ShipsDataService {
    private final AnomalyDetectionPipeline anomalyDetectionPipeline;
    private final QueryExecutor queryExecutor;
    private final NotificationService notificationService;

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
     * @param notificationService notification service class
     */
    @Autowired
    public ShipsDataService(AnomalyDetectionPipeline anomalyDetectionPipeline,
                            QueryExecutor queryExecutor,
                            ShipInformationExtractor shipInformationExtractor,
                            NotificationService notificationService) {
        this.anomalyDetectionPipeline = anomalyDetectionPipeline;
        this.queryExecutor = queryExecutor;
        this.shipInformationExtractor = shipInformationExtractor;
        this.notificationService = notificationService;
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
     * Queries the Druid database in order to retrieve the list of AISSignals and anomaly scores of a wanted ship.
     * Then, subsamples the retrieved list in case it is big, and sends the data to frontend
     *
     * @param id the id of the ship on which we will query our data
     * @return the list of the retrieved details
     * @throws DatabaseException in case the query doesn't succeed
     */
    public List<TrajectoryObject> getSubsampledHistoryOfShip(long id) throws DatabaseException {
        // Initialize the subsampled trajectory list
        List<DatabaseExtractObject> subsampledTrajectory = new ArrayList<>();

        // Extract all AIS signals + anomaly scores for the wanted ship
        List<DatabaseExtractObject> fullHistory = queryExecutor
                .executeQueryOneLong(id, "src/main/resources/db/sampledHistory.sql", DatabaseExtractObject.class);

        // Check if the extracted list is smaller than the threshold. If it is, do not subsample it
        if (fullHistory.size() <= subsamplingThreshold)
            subsampledTrajectory = fullHistory;
        else {
            // Compute the ratio at which the list elements will be picked
            double subsampleRatio = (double) fullHistory.size() / (double) subsamplingThreshold;

            // Add every subsampleRation-th element to the result list
            for (double i = 0; i < fullHistory.size() - 1; i += subsampleRatio) {
                subsampledTrajectory.add(fullHistory.get((int) i));
            }
            // Also, make sure that the very last signal is added
            subsampledTrajectory.add(fullHistory.get(fullHistory.size() - 1));
        }

        // map the extracted trajectory list to a simplified object
        List<TrajectoryObject> finalDisplayedTrajectory = castTrajectoryObjects(subsampledTrajectory);

        // Extract all points that correspond to notifications, so that they would not be subsampled
        List<TrajectoryObject> notificationsData = getNotificationCoordinates(id);

        List<TrajectoryObject> finalResult = new ArrayList<>();

        // Add the trajectory to the final result
        finalResult.addAll(finalDisplayedTrajectory);

        // Add all notification positions to the final result
        finalResult.addAll(notificationsData);

        // Return a final result, sorted by the timestamp
        return finalResult.stream().sorted((x, y) -> -x.getTimeValue().compareTo(y.getTimeValue())).toList();
    }

    /**
     * Helper method to get all coordinates for notifications that the ship produced.
     * This is done so that the notification points are not subsampled out of the trajectory
     *
     * @param id id of the ship whose trajectory points are considered
     * @return list of trajectory points that correspond to notifications
     */
    private List<TrajectoryObject> getNotificationCoordinates(long id) {
        return notificationService
                .getAllNotificationForShip(id)
                .stream()
                .map(x -> new TrajectoryObject(
                        x.getShipID(),
                        x.getCurrentShipDetails().getCurrentAISSignal().getLongitude(),
                        x.getCurrentShipDetails().getCurrentAISSignal().getLatitude(),
                        x.getCurrentShipDetails().getCurrentAISSignal().getTimestamp(),
                        x.getCurrentShipDetails().getCurrentAnomalyInformation().getScore()
                ))
                .toList();
    }


    /**
     * Helper method to map the contents of subsampled trajectory list to a list DTOs.
     *
     * @param subsampledTrajectory  a list of trajectory points that have a non-simplified type
     * @return list of trajectoryObject objects that will be sent to frontend
     */
    private List<TrajectoryObject> castTrajectoryObjects(List<DatabaseExtractObject> subsampledTrajectory) {
        return subsampledTrajectory
                .stream()
                .map(x -> new TrajectoryObject(
                        x.getAisSignal().getId(),
                        x.getAisSignal().getLongitude(),
                        x.getAisSignal().getLatitude(),
                        x.getAisSignal().getTimestamp(),
                        x.getAnomalyScore()
                ))
                .toList();
    }
}
