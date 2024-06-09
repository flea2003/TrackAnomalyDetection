package sp.utils.sql;

import java.io.BufferedReader;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class FileReader {
    /**
     * Helper method to parse developer defined SQL queries.
     *
     * @param filePath the path where the corresponding query is located
     * @return the SQL processed query
     */
    public static String readQueryFromFile(String filePath) throws IOException {
        StringBuilder queryBuilder = new StringBuilder();
        try (java.io.FileReader fileReader = new java.io.FileReader(filePath);
            BufferedReader reader = new BufferedReader(fileReader)) {
            String line = reader.readLine();
            while (line != null) {
                queryBuilder.append(line).append("\n");
                line = reader.readLine();
            }
            return queryBuilder.toString().trim();
        }
    }

}
