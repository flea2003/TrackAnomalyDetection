package sp.unit.pipeline;

import org.junit.jupiter.api.Test;
import sp.pipeline.PipelineConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineConfigurationTest {

    @Test
    void loadConfigFromConfigFile() throws IOException {
        Path path = Paths.get("kafka-connection.properties");
        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration(path.toString());
        assertDoesNotThrow(() -> {
            pipelineConfiguration.getFullConfiguration();
        });
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
