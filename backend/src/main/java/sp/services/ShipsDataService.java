package sp.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.exceptions.PipelineStartingException;
import sp.model.CurrentShipDetails;
import sp.pipeline.AnomalyDetectionPipeline;
import java.util.HashMap;
import java.util.List;
import sp.services.sql.utils.FileReader;
import sp.services.sql.utils.ResultSetReader;
import sp.utils.DruidConfig;


@Service
public class ShipsDataService {
    private final AnomalyDetectionPipeline anomalyDetectionPipeline;
    private final DruidConfig druidConfig;
    private final Integer activeTime = 30;

    /**
     * Constructor for service class.
     *
     * @param anomalyDetectionPipeline object that is responsible for managing and handling the stream of data and
     *     anomaly information computation
     * @param druidConfig the database object of Apache Druid
     */
    @Autowired
    public ShipsDataService(AnomalyDetectionPipeline anomalyDetectionPipeline, DruidConfig druidConfig) {
        this.anomalyDetectionPipeline = anomalyDetectionPipeline;
        this.druidConfig = druidConfig;
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
        try {

            String str = FileReader.readQueryFromFile("src/main/java/sp/services/sql/queries/history.sql");
            PreparedStatement statement = druidConfig.connection()
                .prepareStatement(str);
            statement.setLong(1, id);
            ResultSetReader<CurrentShipDetails>resultSetReader = new ResultSetReader<>();
            return resultSetReader.extractQueryResults(statement.executeQuery(), CurrentShipDetails.class);

        } catch (SQLException e) {
            throw new PipelineException();
        }
    }
}
