package sp.utils;

import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sp.exceptions.DatabaseException;
import sp.pipeline.PipelineConfiguration;

@Configuration
public class DruidConfig {

    private final PipelineConfiguration pipelineConfiguration;
    private Connection connection;

    /**
     * Constructor for the DruidConfig class.
     *
     * @param pipelineConfiguration the object containing the hardcoded configurations
     */
    @Autowired
    public DruidConfig(PipelineConfiguration pipelineConfiguration) {
        this.pipelineConfiguration = pipelineConfiguration;
    }

    /**
     * Spring bean which initialized a database connection.
     *
     * @return the established connection
     */
    @Bean
    public Connection connection() {
        Properties connectionProperties = new Properties();
        try {
            String url = pipelineConfiguration.getDruidUrl();
            this.connection = DriverManager.getConnection(url, connectionProperties);
            return this.connection;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Method to close the database connection when the application stops.
     */
    @PreDestroy
    public void closeConnection() throws DatabaseException{
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                throw new DatabaseException("Error closing the Druid database");
            }
        }
    }

}
