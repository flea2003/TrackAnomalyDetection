package sp.pipeline;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StreamUtils {

    private final String configPath;

    public StreamUtils(@Value("${kafka.config.file:kafka-connection.properties}") String configPath) {
        this.configPath = configPath;
    }

    /**
     * Loads the properties from the configuration file.
     *
     * @return the loaded properties
     * @throws IOException if such file does not exist
     */
    public Properties loadConfig() throws IOException {
        if (!Files.exists(Paths.get(configPath))) {
            throw new IOException("Kafka configuration file '" + configPath + "' was not found.");
        }

        Properties config = new Properties();
        try (InputStream inputStream = new FileInputStream(configPath)) {
            config.load(inputStream);
        }

        return config;
    }

    /**
     * Creates a Flink stream that is consuming from a Kafka topic.
     *
     * @param topic the name of the topic from which to consume
     * @return the created Flink stream
     */
    public KafkaSource<String> getFlinkStreamConsumingFromKafka(String topic) throws IOException {
        // Load the properties of Kafka from the configuration file
        Properties props = loadConfig();

        return KafkaSource.<String>builder()
                .setProperties(props)
                .setTopics(topic)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }

    /**
     * Creates a KafkaStreams object, according to the configuration file.
     *
     * @param builder streams builder
     * @return created KafkaStreams object with the specified configuration
     */
    public KafkaStreams getKafkaStreamConsumingFromKafka(StreamsBuilder builder) throws IOException {
        // Load properties from the configuration file
        Properties props = loadConfig();

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaStreams(builder.build(), props);
    }
}

