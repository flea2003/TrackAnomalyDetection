package unit.sp.pipeline;

import org.junit.jupiter.api.Test;
import sp.pipeline.PipelineConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PipelineConfigurationTest {

    @Test
    void loadConfigFromTestConfigFile() throws IOException {
        Path path = Paths.get("src", "test", "resources", "test.dummy.kafka.config.file");
        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration(path.toString());

        Properties expected = new Properties();
        expected.setProperty("a", "b");
        expected.setProperty("b", "c");
        expected.setProperty("c", "d");

        Properties result = pipelineConfiguration.getFullConfiguration();

        assertEquals(expected, result);
    }

    @Test
    void loadConfigExceptionFileNotExists() {
        Path path = Paths.get("does", "not", "exist");

        // Check that exceptions are thrown in all three methods due to FileNotFoundException
        assertThrows(IOException.class, () -> {
            new PipelineConfiguration(path.toString());
        });
    }
}
