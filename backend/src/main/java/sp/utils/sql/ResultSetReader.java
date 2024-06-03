package sp.utils.sql;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import sp.pipeline.utils.json.JsonMapper;

@Service
public class ResultSetReader {

    /**
     * Methods extracts the rows from a ResultSet after a SQL query is performed.
     *
     * @param resultSet the resultSet of the query
     * @param classType the class that we have to extract
     * @param <T> generics of the object which defines the class tclass
     * @return the retrieved list of the classType
     * @throws SQLException Exception if something went wrong
     */
    public static <T> List<T> extractQueryResults(ResultSet resultSet, Class<T> classType) throws SQLException {
        List<T> extractedResults = new ArrayList<>();
        while (resultSet.next()) {
            extractedResults.add(extractRow(resultSet, classType));
        }
        return extractedResults;
    }

    /**
     * Extracts a particular row of the ResultSet.
     *
     * @param resultSet the resultSet of the query
     * @param classType the class that we have to extract
     * @param <T> generics of the object which defines the class tclass
     * @return the retrieved list of the classType
     * @throws SQLException Exception if something went wrong
     */
    private static <T> T extractRow(ResultSet resultSet, Class<T> classType) throws SQLException {
        JsonObject jsonObject = new JsonObject();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            String columnValue = resultSet.getString(i);
            JsonElement jsonElement = JsonParser.parseString(columnValue);
            jsonObject.add(columnName, jsonElement);
        }
        try {
            Gson gson = new Gson();
            String json = gson.toJson(jsonObject);
            return JsonMapper.fromJson(json, classType);
        } catch (Exception e) {
            throw new SQLException();
        }
    }

}
