package sp.services.sql.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sp.pipeline.utils.json.JsonMapper;

public class ResultSetReader<T> {

    /**
     * Methods extracts the rows from a ResultSet after a SQL query is performed.
     *
     * @param resultSet - the resultSet of the query
     * @param classType - the class that we have to extract
     * @return - the retrieved list of the classType
     * @throws SQLException - Exception if something went wrong
     */
    public List<T> extractQueryResults(ResultSet resultSet, Class<T> classType) throws SQLException {
        List<T> extractedResults = new ArrayList<>();
        while (resultSet.next()) {
            extractedResults.add(exctractRow(resultSet, classType));
        }
        return extractedResults;
    }

    /**
     * Extracts a particular row of the ResultSet.
     *
     * @param resultSet - the resultSet of the query
     * @param classType - the class that we have to extract
     * @return - the retrieved list of the classType
     * @throws SQLException - Exception if something went wrong
     */
    private T exctractRow(ResultSet resultSet, Class<T> classType) throws SQLException {
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
            e.printStackTrace();
            throw new SQLException();
        }
    }

}
