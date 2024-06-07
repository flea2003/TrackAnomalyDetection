package sp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sp.pipeline.PipelineConfiguration;

@Configuration
public class DruidConfig {
    private final PipelineConfiguration pipelineConfiguration;

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
            return DriverManager.getConnection(url, connectionProperties);
        } catch (SQLException e) {
            return null;
        }
    }

}
