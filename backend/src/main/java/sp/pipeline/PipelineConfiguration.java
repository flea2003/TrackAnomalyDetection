package sp.pipeline;

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
    private static final String KAFKA_STORE_NOTIFICATIONS_NAME = "ships-notification-store";
    private static final String RAW_INCOMING_AIS_TOPIC_NAME_PROPERTY = "incoming.ais-raw.topic.name";
    private static final String INCOMING_AIS_TOPIC_NAME_PROPERTY = "incoming.ais.topic.name";
    private static final String CALCULATED_SCORES_TOPIC_NAME_PROPERTY = "calculated.scores.topic.name";
    private static final String KAFKA_SERVER_ADDRESS_PROPERTY = "kafka.server.address";
    private static final String KAFKA_STORE_NAME_PROPERTY = "kafka.store.name";
    private final String configPath;
    public final String rawIncomingAisTopicName;
    public final String incomingAisTopicName;
    public final String calculatedScoresTopicName;
    public final String kafkaServerAddress;
    public final String kafkaStoreName;
    private Properties savedConfiguration;


    public PipelineConfiguration(@Value("${kafka.config.file:kafka-connection.properties}") String configPath) throws IOException {
        this.configPath = configPath;
        loadConfig();

        if (savedConfiguration == null) {
            throw new IOException("Properties file not found");
        }

        rawIncomingAisTopicName = savedConfiguration.getProperty(RAW_INCOMING_AIS_TOPIC_NAME_PROPERTY);
        incomingAisTopicName = savedConfiguration.getProperty(INCOMING_AIS_TOPIC_NAME_PROPERTY);
        calculatedScoresTopicName = savedConfiguration.getProperty(CALCULATED_SCORES_TOPIC_NAME_PROPERTY);
        kafkaServerAddress = savedConfiguration.getProperty(KAFKA_SERVER_ADDRESS_PROPERTY);
        kafkaStoreName = savedConfiguration.getProperty(KAFKA_STORE_NAME_PROPERTY);

    }

    private void loadConfig() throws IOException {
        if (!Files.exists(Paths.get(configPath))) {
            throw new IOException("Kafka configuration file '" + configPath + "' was not found.");
        }

        Properties config = new Properties();
        try (InputStream inputStream = new FileInputStream(configPath)) {
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
