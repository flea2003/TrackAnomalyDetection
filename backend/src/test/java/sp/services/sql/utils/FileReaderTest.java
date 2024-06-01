package sp.services.sql.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class FileReaderTest {

    @Test
    void readQueryFromFileTest() throws SQLException {
        assertThat(FileReader.readQueryFromFile("src/test/java/sp/services/sql/utils/queries/test.sql"))
            .isEqualTo("TEST 123");
    }

}