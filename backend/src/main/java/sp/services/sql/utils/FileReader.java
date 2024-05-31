package sp.services.sql.utils;

import java.io.BufferedReader;
import java.sql.SQLException;

public class FileReader {

    /**
     * Helper method to parse developer defined SQL queries
     *
     * @param filePath - the path where the corresponding query is located
     * @return - the SQL processed query
     */
    public static String readQueryFromFile(String filePath) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        try{
            BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                queryBuilder.append(line).append("\n");
            }
            return queryBuilder.toString().trim();
        } catch (Exception e) {
            throw new SQLException();
        }
    }

}
