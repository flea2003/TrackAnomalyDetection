package sp.pipeline;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class StreamUtils {
    public static final String CONFIG_PATH = "kafka-connection.properties";
    /**
     * Loads a configuration file.
     *
     * @param path the path to the configuration file
     * @return the loaded properties
     * @throws IOException if such file does not exist
     */
    public static Properties loadConfig(String path) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            throw new IOException("Kafka configuration file '" + path + "' was not found.");
        }
        Properties config = new Properties();
        try (InputStream inputStream = new FileInputStream(path)) {
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
    public static KafkaSource<String> getFlinkStreamConsumingFromKafka(String topic) {
        // Load the properties of Kafka from the configuration file
        Properties props;
        try {
            props = loadConfig(CONFIG_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
    public static KafkaStreams getKafkaStreamConsumingFromKafka(StreamsBuilder builder) {
        // Load properties from the configuration file
        Properties props;
        try {
            props = loadConfig(CONFIG_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaStreams(builder.build(), props);
    }
}

