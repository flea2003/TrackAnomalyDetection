package sp.pipeline;

import org.apache.kafka.streams.StreamsBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class StreamUtilsTest {

    @Test
    void loadConfigFromTestConfigFile() throws IOException {
        Path path = Paths.get("src", "test", "resources", "test.dummy.kafka.config.file");
        StreamUtils streamUtils = new StreamUtils(path.toString());

        Properties expected = new Properties();
        expected.setProperty("a", "b");
        expected.setProperty("b", "c");
        expected.setProperty("c", "d");

        Properties result = streamUtils.loadConfig();

        assertEquals(expected, result);
    }

    @Test
    void loadConfigExceptionFileNotExists() {
        Path path = Paths.get("does", "not", "exist");
        StreamUtils streamUtils = new StreamUtils(path.toString());

        // Check that exceptions are thrown in all three methods due to FileNotFoundException
        assertThrows(IOException.class, streamUtils::loadConfig);
        assertThrows(RuntimeException.class, () -> streamUtils.getFlinkStreamConsumingFromKafka("topic"));
        assertThrows(RuntimeException.class, () ->
                streamUtils.getKafkaStreamConsumingFromKafka(new StreamsBuilder())
        );
    }

}