package sp.services.sql.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonObject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.h2.util.json.JSONObject;
import sp.model.CurrentShipDetails;
import sp.pipeline.utils.json.JsonMapper;

public class ResultSetReader<T> {
    public List<T> extractQueryResults(ResultSet resultSet, Class<T>classType) throws SQLException {
        List<T>extractedResults = new ArrayList<>();
        while(resultSet.next()){
            extractedResults.add(exctractRow(resultSet, classType));
        }
        return extractedResults;
    }

    private T exctractRow(ResultSet resultSet, Class<T>classType) throws SQLException{
        JsonObject jsonObject = new JsonObject();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            String columnValue = resultSet.getString(i);
            jsonObject.addProperty(columnName, columnValue);

        }
        try {
            return JsonMapper.fromJson(jsonObject.toString(), classType);
        } catch (Exception e){
            e.printStackTrace();
            throw new SQLException();
        }
    }

}
