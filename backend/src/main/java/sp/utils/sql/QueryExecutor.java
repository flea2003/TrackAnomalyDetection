package sp.utils.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.utils.DruidConfig;

@Service
public class QueryExecutor {
    private final DruidConfig druidConfig;

    /**
     * Constructor for the QueryExecutor class.
     *
     * @param druidConfig the class containing the druid database
     */
    @Autowired
    public QueryExecutor(DruidConfig druidConfig) {
        this.druidConfig = druidConfig;
    }

    /**
     * Executes a SQL query which takes as input a parameter of type long.
     *
     * @param id the parameter of type long
     * @param path the path of where the sql query is located
     * @param tclass the class of the answer of this query
     * @param <T> the type of class that will be the answer of this query
     * @return a list of tclass objects
     * @throws SQLException throws if the SQL query fails
     */
    public <T> List<T> executeQueryOneLong(long id, String path, Class<T> tclass) throws SQLException {
        String query = FileReader.readQueryFromFile(path);

        try (PreparedStatement statement = druidConfig.connection().prepareStatement(query)) {
            // in the sql query, the parameter 1 is the id that we query on
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return ResultSetReader.extractQueryResults(resultSet, tclass);
            }
        }
    }

}
