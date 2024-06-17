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

@Getter
@Component
public class PipelineConfiguration {
    private static final String RAW_INCOMING_AIS_TOPIC_NAME_PROPERTY = "incoming.ais-raw.topic.name";
    private static final String NOTIFICATIONS_TOPIC_NAME_PROPERTY = "notifications.topic.name";
    private static final String KAFKA_SERVER_ADDRESS_PROPERTY = "kafka.server.address";
    private static final String KAFKA_SHIPS_HISTORY_STORE_NAME_PROPERTY = "kafka.ships-history.name";
    private static final String KAFKA_STORE_NAME_PROPERTY = "kafka.store.name";
    private static final String DRUID_URL_PROPERTY = "druid.connection.url";
    private static final String EXTRACTOR_THREAD_POOL_SIZE_PROPERTY = "extractor.thread.pool.size";
    private static final String POLLING_FREQUENCY_FOR_SOCKETS_PROPERTY = "polling.frequency.for.sockets";
    private static final String POLLING_FREQUENCY_FOR_CURRENT_DETAILS_PROPERTY = "polling.frequency.for.current.details";
    private static final String POLLING_FREQUENCY_FOR_NOTIFICATIONS_PROPERTY = "polling.frequency.for.notifications";
    private static final String FLINK_DEPENDENCY_JAR_PATH_PROPERTY = "flink.shadow.jar.name";
    private static final String FLINK_JOB_MANAGER_IP_PROPERTY = "flink.job.manager.ip";
    private static final String FLINK_JOB_MANAGER_PORT_PROPERTY = "flink.job.manager.port";
    private static final String FLINK_PARALLELISM_PROPERTY = "flink.parallelism";

    private String rawIncomingAisTopicName;
    private String shipsHistoryTopicName;
    private String notificationsTopicName;
    private String kafkaServerAddress;
    private String kafkaStoreName;
    private String flinkDependencyJarPath;
    private String flinkJobManagerIp;
    private int extractorThreadPoolSize;
    private int pollingFrequencyForSockets;
    private int pollingFrequencyForCurrentDetails;
    private int pollingFrequencyForNotifications;
    private int flinkJobManagerPort;
    private int flinkParallelism;

    private String druidUrl;

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

        updateLocalFields();
    }

    /**
     * Based in savedConfiguration, updates local fields.
     */
    private void updateLocalFields() {
        rawIncomingAisTopicName = savedConfiguration.getProperty(RAW_INCOMING_AIS_TOPIC_NAME_PROPERTY);
        kafkaServerAddress = savedConfiguration.getProperty(KAFKA_SERVER_ADDRESS_PROPERTY);
        shipsHistoryTopicName = savedConfiguration.getProperty(KAFKA_SHIPS_HISTORY_STORE_NAME_PROPERTY);
        kafkaStoreName = savedConfiguration.getProperty(KAFKA_STORE_NAME_PROPERTY);
        druidUrl = savedConfiguration.getProperty(DRUID_URL_PROPERTY);
        notificationsTopicName = savedConfiguration.getProperty(NOTIFICATIONS_TOPIC_NAME_PROPERTY);
        extractorThreadPoolSize = Integer.parseInt(savedConfiguration.getProperty(EXTRACTOR_THREAD_POOL_SIZE_PROPERTY));
        pollingFrequencyForSockets = Integer.parseInt(savedConfiguration.getProperty(POLLING_FREQUENCY_FOR_SOCKETS_PROPERTY));
        pollingFrequencyForCurrentDetails = Integer.parseInt(
                savedConfiguration.getProperty(POLLING_FREQUENCY_FOR_CURRENT_DETAILS_PROPERTY)
        );
        pollingFrequencyForNotifications = Integer.parseInt(
                savedConfiguration.getProperty(POLLING_FREQUENCY_FOR_NOTIFICATIONS_PROPERTY)
        );
        flinkDependencyJarPath = savedConfiguration.getProperty(FLINK_DEPENDENCY_JAR_PATH_PROPERTY);
        flinkJobManagerIp = savedConfiguration.getProperty(FLINK_JOB_MANAGER_IP_PROPERTY);
        flinkJobManagerPort = Integer.parseInt(savedConfiguration.getProperty(FLINK_JOB_MANAGER_PORT_PROPERTY));
        flinkParallelism = Integer.parseInt(savedConfiguration.getProperty(FLINK_PARALLELISM_PROPERTY));
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
     * Edits the configuration file with the specified key and value.
     * Used for testing purposes
     *
     * @param key the key to edit
     * @param value the value to set
     */
    public void updateConfiguration(String key, String value) {
        savedConfiguration.setProperty(key, value);
        updateLocalFields();
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
