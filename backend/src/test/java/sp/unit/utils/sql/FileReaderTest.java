package sp.unit.utils.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import sp.utils.sql.FileReader;

class FileReaderTest {

    @Test
    void readQueryFromFileTest() throws SQLException {
        assertThat(FileReader.readQueryFromFile("src/test/resources/test.sql"))
            .isEqualTo("TEST 123");
    }

    @Test
    void readQueryFromNonExistentFile() throws SQLException {
        assertThatThrownBy(() -> FileReader.readQueryFromFile("src/test/resources/nonexistent.sql"))
            .isInstanceOf(SQLException.class);
    }

}