package sp.unit.utils.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import sp.exceptions.DatabaseException;
import sp.utils.sql.FileReader;

class FileReaderTest {

    @Test
    void testConstructor(){
        FileReader fileReader = new FileReader();
        assertThat(fileReader).isNotNull();
    }

    @Test
    void readQueryFromFileTest() throws IOException {
        assertThat(FileReader.readQueryFromFile("src/test/resources/test.sql"))
            .isEqualTo("TEST 123");
    }

    @Test
    void readQueryFromNonExistentFile() throws DatabaseException {
        assertThatThrownBy(() -> FileReader.readQueryFromFile("src/test/resources/nonexistent.sql"))
            .isInstanceOf(IOException.class);
    }

}