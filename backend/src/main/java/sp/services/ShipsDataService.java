package sp.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import sp.utils.DruidConfig;
import sp.utils.sql.FileReader;
import sp.utils.sql.ResultSetReader;


@Service
public class ShipsDataService {
    private final AnomalyDetectionPipeline anomalyDetectionPipeline;
    private final DruidConfig druidConfig;
    private final Integer activeTime = 30;
    private final ResultSetReader<CurrentShipDetails> resultSetReader;


    /**
     * Constructor for service class.
     *
     * @param anomalyDetectionPipeline object that is responsible for managing and handling the stream of data and
     *     anomaly information computation
     * @param resultSetReader - the object which retrieves the ShipDetails from the SQL queries
     * @param druidConfig the database object of Apache Druid
     */
    @Autowired
    public ShipsDataService(AnomalyDetectionPipeline anomalyDetectionPipeline,
                            ResultSetReader<CurrentShipDetails> resultSetReader,
                            DruidConfig druidConfig) {
        this.anomalyDetectionPipeline = anomalyDetectionPipeline;
        this.druidConfig = druidConfig;
        this.resultSetReader = resultSetReader;
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

    /**
     * Queries the Druid database in order to retrieve the list of CurrentShipDetails of the corresponding ship.
     *
     * @param id - the id of the ship on which we will query our data
     * @return - the list of the retrieved details
     * @throws PipelineException - in case the query doesn't succeed
     */
    public List<CurrentShipDetails> getHistoryOfShip(long id) throws PipelineException {
        String query;
        try {
            query = FileReader.readQueryFromFile("src/main/resources/history.sql");
        } catch (SQLException e) {
            throw new PipelineException("Error reading SQL query from file");
        }

        try (Connection connection = druidConfig.connection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            // in the sql query, the parameter 1 is the id that we query on
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSetReader.extractQueryResults(resultSet, CurrentShipDetails.class);
            }

        } catch (SQLException e) {
            throw new PipelineException("Error executing SQL query");
        }
    }
}
