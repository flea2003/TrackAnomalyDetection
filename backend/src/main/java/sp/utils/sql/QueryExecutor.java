package sp.utils.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import sp.utils.DruidConfig;

public class QueryExecutor {
    private static DruidConfig druidConfig;

    /**
     * Executes a SQL query which takes as input a parameter of type long.
     *
     * @param id the parameter of type long
     * @param path the path of where the sql query is located
     * @param tclass the class of the answer of this query
     * @param <T> generics of the object which defines the class tclass
     * @return a list of tclass objects
     * @throws SQLException throws if the SQL query fails
     */
    public static <T> List<T> executeQueryOneLong(long id, String path, Class<T> tclass) throws SQLException {
        String query;

        query = FileReader.readQueryFromFile(path);

        try (Connection connection = druidConfig.connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            // in the sql query, the parameter 1 is the id that we query on
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return ResultSetReader.extractQueryResults(resultSet, tclass);
            }

        }
    }

}
