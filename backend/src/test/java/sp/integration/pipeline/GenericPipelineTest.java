package sp.integration.pipeline;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import sp.pipeline.AnomalyDetectionPipeline;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.parts.aggregation.ScoreAggregationBuilder;
import sp.pipeline.parts.aggregation.aggregators.CurrentStateAggregator;
import sp.pipeline.parts.identification.IdAssignmentBuilder;
import sp.pipeline.parts.notifications.NotificationsAggregator;
import sp.pipeline.parts.notifications.NotificationsDetectionBuilder;
import sp.pipeline.parts.scoring.ScoreCalculationBuilder;
import sp.pipeline.parts.scoring.scorecalculators.ScoreCalculationStrategy;
import sp.pipeline.parts.scoring.scorecalculators.SimpleScoreCalculator;
import sp.pipeline.utils.StreamUtils;
import sp.repositories.NotificationRepository;
import sp.services.NotificationService;
import sp.services.ShipsDataService;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * The following class contains methods for running an integration test.
 * It handles starting and destroying the Kafka/Flink cluster in between tests (the
 * class has a setupPipelineComponentsAndRun() method for that and @BeforeEach and
 * an @AfterEach for loading config and destroying the cluster.
 * It also has 2 helper methods for producing to a Kafka topic and consuming from it.
 * Integration test classes should inherit from this class to simplify testing.
 */
class GenericPipelineTest {

    private EmbeddedKafkaZKBroker embeddedKafka;
    private static final int kafkaPort = 50087;
    private static final int zkPort = 50082;
    private PipelineConfiguration config;
    private StreamExecutionEnvironment env;

    // Items to be inherited by children
    protected NotificationRepository notificationRepository;
    protected NotificationService notificationService;
    protected String rawAISTopic;
    protected String identifiedAISTopic;
    protected String scoresTopic;
    protected AnomalyDetectionPipeline anomalyDetectionPipeline;
    protected ShipsDataService shipsDataService;


    /**
     * Loads the configuration file, and also updates the server address
     * according to our hardcoded port.
     *
     * @throws IOException if config cannot be loaded
     */
    private void loadConfigFile() throws IOException {
        String randomUUID = UUID.randomUUID().toString();

        // Load the configuration file and update it were
        config = new PipelineConfiguration("kafka-connection.properties");
        config.updateConfiguration("bootstrap.servers", "localhost:" + kafkaPort);
        config.updateConfiguration("kafka.server.address", "localhost:" + kafkaPort);

        // Setup log directory and application ID to something random so that different tests do not clash
        config.updateConfiguration("application.id", "anomaly-detection-pipeline-test-" + randomUUID);
        config.updateConfiguration("kafka.logs.dir", System.getProperty("java.io.tmpdir") + "/spring.kafka." + randomUUID);

        // Set the topic names to something a bit random as well so that tests do not clash
        config.updateConfiguration("incoming.ais-raw.topic.name", "ships-raw-AIS" + "-" + randomUUID);
        config.updateConfiguration("incoming.ais.topic.name", "ships-AIS" + "-" + randomUUID);
        config.updateConfiguration("calculated.scores.topic.name", "ships-scores" + "-" + randomUUID);

        rawAISTopic = config.getRawIncomingAisTopicName();
        identifiedAISTopic = config.getIncomingAisTopicName();
        scoresTopic = config.getCalculatedScoresTopicName();
    }

    /**
     * Loads the configuration file and starts and Embedded Kafka server. I.e., it creates a Zookeeper
     * instance and a Kafka server instance. It uses EmbeddedKafkaZKBroker, since we are using zookeeper (ZK)
     * for in our application. We could also use EmbeddedKafkaKraftBroker, if we used Kraft instead of zookeeper.
     * @throws IOException if loading the config fails
     */
    @BeforeEach
    void setup() throws IOException {
        loadConfigFile();

        // Setup embedded kafka
        embeddedKafka = new EmbeddedKafkaZKBroker(1, true, 1, rawAISTopic, identifiedAISTopic, scoresTopic);
        embeddedKafka.kafkaPorts(kafkaPort);
        embeddedKafka.zkPort(zkPort);

        embeddedKafka.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() throws Exception {
        embeddedKafka.doWithAdmin(x -> x.deleteTopics(List.of(rawAISTopic, identifiedAISTopic, scoresTopic)));
        embeddedKafka.destroy();
        env.close();
    }

    /**
     * Sets up the pipeline object, injecting all required dependencies
     * and mocking the notificationsService.
     * The pipeline is also started with the creation of shipsDataService (it starts the pipeline
     * in the constructor).
     */
    protected void setupPipelineComponentsAndRun() {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(10);

        IdAssignmentBuilder idAssignmentBuilder;
        ScoreCalculationBuilder scoreCalculationBuilder;
        ScoreAggregationBuilder scoreAggregationBuilder;
        NotificationsDetectionBuilder notificationsDetectionBuilder;
        ScoreCalculationStrategy scoreCalculationStrategy;
        CurrentStateAggregator currentStateAggregator;
        NotificationsAggregator notificationsAggregator;
        StreamUtils streamUtils;

        // Mock the notification service class (to mock the DB)
        notificationRepository = mock(NotificationRepository.class);
        notificationService = spy(new NotificationService(notificationRepository));

        // Create the core objects
        scoreCalculationStrategy = new SimpleScoreCalculator();
        currentStateAggregator = new CurrentStateAggregator();
        notificationsAggregator = new NotificationsAggregator(notificationService);

        // Create the pipeline builders
        streamUtils = new StreamUtils(config);
        idAssignmentBuilder = new IdAssignmentBuilder(streamUtils, config);
        scoreCalculationBuilder = new ScoreCalculationBuilder(streamUtils, config, scoreCalculationStrategy);
        scoreAggregationBuilder = new ScoreAggregationBuilder(config, currentStateAggregator);
        notificationsDetectionBuilder = new NotificationsDetectionBuilder(notificationsAggregator);

        // Create the pipeline itself
        anomalyDetectionPipeline = new AnomalyDetectionPipeline(
                streamUtils, idAssignmentBuilder, scoreCalculationBuilder, scoreAggregationBuilder, notificationsDetectionBuilder,
                env
        );

        // Instantiate Service classes for querying
        shipsDataService = new ShipsDataService(anomalyDetectionPipeline);
    }

    /**
     * Given a topic and a list of strings, produces them to the topic.
     *
     * @param topic the topic name
     * @param items a list of strings to produce to that topic
     * @throws ExecutionException in case producing does not work out
     * @throws InterruptedException in case producing does not work out
     */
    protected void produceToTopic(String topic, List<String> items) throws ExecutionException, InterruptedException {
        // Create a producer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            for (String item : items) {
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, item);
                producer.send(record).get();
            }
        }
    }

    /**
     * Consumes a given number of items from a topic. If the required number of items
     * do not come in time, a RuntimeException is thrown.
     *
     * @param topic the topic from which to consume
     * @param count number of elements to get
     * @param seconds maximum number of seconds to wait for the elements
     * @return a list of strings that were consumed
     */
    protected List<String> getItemsFromTopic(String topic, int count, int seconds) {
        // Create a consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);

        List<String> items = new ArrayList<>();
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.of(seconds, java.time.temporal.ChronoUnit.SECONDS));
        for (ConsumerRecord<String, String> record : records) {
            items.add(record.value());
        }

        if (items.size() != count) {
            throw new RuntimeException("Not enough records were consumed in time. Got " + items.size() + " but expected " + count + " record(s)");
        }

        return items;
    }
}