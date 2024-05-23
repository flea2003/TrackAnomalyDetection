package sp.pipeline;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Component
public class PipelineConfiguration {
    private static final String RAW_INCOMING_AIS_TOPIC_NAME_PROPERTY = "incoming.ais-raw.topic.name";
    private static final String INCOMING_AIS_TOPIC_NAME_PROPERTY = "incoming.ais.topic.name";
    private static final String CALCULATED_SCORES_TOPIC_NAME_PROPERTY = "calculated.scores.topic.name";
    private static final String KAFKA_SERVER_ADDRESS_PROPERTY = "kafka.server.address";
    private static final String KAFKA_STORE_NAME_PROPERTY = "kafka.store.name";

    @Getter
    private final String rawIncomingAisTopicName;

    @Getter
    private final String incomingAisTopicName;

    @Getter
    private final String calculatedScoresTopicName;

    @Getter
    private final String kafkaServerAddress;

    @Getter
    private final String kafkaStoreName;

    @Getter
    private Properties savedConfiguration;

    /**
     * Constructor for the PipelineConfiguration class. Loads the configuration file from the specified path.
     * If the file is not found, an IOException is thrown. The public fields are then loaded from the file.
     *
     * @param configPath path to the configuration file
     * @throws IOException if the configuration file is not found
     */
    public PipelineConfiguration(@Value("${kafka.config.file:kafka-connection.properties}")
                                 String configPath) throws IOException {
        loadConfig(configPath);

        rawIncomingAisTopicName = savedConfiguration.getProperty(RAW_INCOMING_AIS_TOPIC_NAME_PROPERTY);
        incomingAisTopicName = savedConfiguration.getProperty(INCOMING_AIS_TOPIC_NAME_PROPERTY);
        calculatedScoresTopicName = savedConfiguration.getProperty(CALCULATED_SCORES_TOPIC_NAME_PROPERTY);
        kafkaServerAddress = savedConfiguration.getProperty(KAFKA_SERVER_ADDRESS_PROPERTY);
        kafkaStoreName = savedConfiguration.getProperty(KAFKA_STORE_NAME_PROPERTY);

    }

    /**
     * Loads the configuration file from the specified path.
     *
     * @param path path to the configuration file
     * @throws IOException if the configuration file is not found
     */
    private void loadConfig(String path) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            throw new IOException("Kafka configuration file '" + path + "' was not found.");
        }

        Properties config = new Properties();
        try (InputStream inputStream = new FileInputStream(path)) {
            config.load(inputStream);
        }

        this.savedConfiguration = config;
    }

    /**
     * Returns the properties from the configuration file.
     *
     * @return the properties from the configuration file
     */
    public Properties getFullConfiguration() {
        return savedConfiguration;
    }
}
