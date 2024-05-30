package sp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DruidConfig {

    /**
     * Spring bean which initialized a database connection.
     *
     * @return the established connection
     */
    @Bean
    public Connection connection() {
        Properties connectionProperties = new Properties();
        try {
            String url = "jdbc:avatica:remote:url=http://localhost:8888/druid/v2/sql/avatica/;transparent_reconnection=true";
            return DriverManager.getConnection(url, connectionProperties);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
