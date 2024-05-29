package integration.sp.pipeline;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import sp.dtos.ExternalAISSignal;
import sp.model.AISSignal;
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
import sp.pipeline.utils.json.JsonMapper;
import sp.services.NotificationService;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class AnomalyDetectionPipelineTest {
    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(2)
                            .setNumberTaskManagers(1)
                            .build());

    private EmbeddedKafkaZKBroker embeddedKafka;
    private static final int kafkaPort = 50087;
    private static final int zkPort = 50082;
    private PipelineConfiguration config;
    private StreamExecutionEnvironment env;

    // Items to be inherited by children
    protected NotificationService notificationService;
    protected String rawAISTopic;
    protected String identifiedAISTopic;
    protected String scoresTopic;
    protected AnomalyDetectionPipeline anomalyDetectionPipeline;


    /**
     * Loads the configuration file, and also updates the server address
     * according to our hardcoded port.
     *
     * @throws IOException if config cannot be loaded
     */
    private void loadConfigFile() throws IOException {
        // Load the configuration file and update it were
        config = new PipelineConfiguration("kafka-connection.properties");
        config.updateConfiguration("bootstrap.servers", "localhost:" + kafkaPort);
        config.updateConfiguration("kafka.server.address", "localhost:" + kafkaPort);

        rawAISTopic = config.getRawIncomingAisTopicName();
        identifiedAISTopic = config.getIncomingAisTopicName();
        scoresTopic = config.getCalculatedScoresTopicName();
    }

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
    void tearDown() {
        embeddedKafka.destroy();
    }

    /**
     * Sets up the pipeline object, injecting all required dependencies
     * and mocking the notificationsService.
     */
    protected void setupPipelineComponents() {
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
        notificationService = mock(NotificationService.class);

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
    }

    /**
     * Given a topic and a list of strings, produces them to the topic.
     *
     * @param topic the topic name
     * @param items a list of strings to produce to that topic
     * @throws ExecutionException in case producing does not work out
     * @throws InterruptedException in case producing does not work out
     */
    public void produceToTopic(String topic, List<String> items) throws ExecutionException, InterruptedException {
        // Create a producer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        for (String item : items) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, item);
            producer.send(record).get();
        }
    }

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

    @Test
    void testKafkaProducerAndConsumer() throws ExecutionException, InterruptedException, IOException {
        setupPipelineComponents();
        anomalyDetectionPipeline.runPipeline();
        Thread.sleep(5000);

        // Send a message to the raw ships topic
        ExternalAISSignal fakeSignal = new ExternalAISSignal("producer", "hash", 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")), "port");
        produceToTopic(rawAISTopic, List.of(JsonMapper.toJson(fakeSignal)));

        // Get the strings produced to the identified ships topic
        List<String> list = getItemsFromTopic(identifiedAISTopic, 1, 5);

        // Calculate the expected AIS signal
        long expectedID = 1098835837;
        AISSignal expectedAISSignal = new AISSignal(fakeSignal, expectedID);

        // Deserialize the received AIS signal
        AISSignal aisSignal = JsonMapper.fromJson(list.get(0), AISSignal.class);

        // Make sure they are equal (ignoring received time)
        assertEquals(expectedAISSignal, aisSignal);
    }
}