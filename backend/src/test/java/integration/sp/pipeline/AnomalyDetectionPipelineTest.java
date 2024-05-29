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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import sp.dtos.ExternalAISSignal;
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
import java.util.HashMap;
import java.util.Map;
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

    private static EmbeddedKafkaZKBroker embeddedKafka;
    private static String rawAISTopic;
    private static String identifiedAISTopic;
    private static String scoresTopic;
    private static final int kafkaPort = 50087;
    private static final int zkPort = 50082;

    private static PipelineConfiguration config;

    private static void loadConfigFile() throws IOException {
        // Load the configuration file and update it were
        config = new PipelineConfiguration("kafka-connection.properties");
        config.updateConfiguration("bootstrap.servers", "localhost:" + kafkaPort);
        config.updateConfiguration("kafka.server.address", "localhost:" + kafkaPort);

        rawAISTopic = config.getRawIncomingAisTopicName();
        identifiedAISTopic = config.getIncomingAisTopicName();
        scoresTopic = config.getCalculatedScoresTopicName();
    }

    @BeforeAll
    static void setup() throws IOException {
        loadConfigFile();

        // Setup embedded kafka
        embeddedKafka = new EmbeddedKafkaZKBroker(1, true, 1, rawAISTopic, identifiedAISTopic, scoresTopic);
        embeddedKafka.kafkaPorts(kafkaPort);
        embeddedKafka.zkPort(zkPort);

        embeddedKafka.afterPropertiesSet();
    }

    @AfterAll
    static void tearDown() {
        embeddedKafka.destroy();
    }


    private AnomalyDetectionPipeline anomalyDetectionPipeline;
    private NotificationService notificationService;
    private StreamUtils streamUtils;
    private StreamExecutionEnvironment env;

    private void setupPipelineComponents() throws IOException {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(10);

        IdAssignmentBuilder idAssignmentBuilder;
        ScoreCalculationBuilder scoreCalculationBuilder;
        ScoreAggregationBuilder scoreAggregationBuilder;
        NotificationsDetectionBuilder notificationsDetectionBuilder;
        ScoreCalculationStrategy scoreCalculationStrategy;
        CurrentStateAggregator currentStateAggregator;
        NotificationsAggregator notificationsAggregator;

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


    @Test
    void testKafkaProducerAndConsumer() throws ExecutionException, InterruptedException, IOException {
        System.out.println("Setting up pipeline components...");
        setupPipelineComponents();

        System.out.println("Running pipeline...");
        anomalyDetectionPipeline.runPipeline();
        Thread.sleep(5000);

        // Producer properties
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        // Consumer properties
        Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("testGroup" , "true", embeddedKafka));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, identifiedAISTopic);

        // Send a message to the raw ships topic
        ExternalAISSignal fakeSignal = new ExternalAISSignal("producer", "hash", 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(), "port");
        ProducerRecord<String, String> record = new ProducerRecord<>(rawAISTopic, JsonMapper.toJson(fakeSignal));
        RecordMetadata metadata = producer.send(record).get();

        // Consume the message from the topic
        System.out.println("Now sleeping for 5s...");
        Thread.sleep(5000);

        DataStream<String> str = env.fromSource(streamUtils.getFlinkStreamConsumingFromKafka(identifiedAISTopic), WatermarkStrategy.noWatermarks(), "AIS Source");
        str.map(x -> {
            System.out.println("Received (IN TEST): " + x);
            return x;
        }).print();

        ConsumerRecord<String, String> singleRecord = null;


        for(int i = 0; i < 500; i++) {
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.of(2, java.time.temporal.ChronoUnit.SECONDS));
            for (ConsumerRecord<String, String> r : records) {
                System.out.println("record = " + r);
            }
            try {
              singleRecord = records.iterator().next();
              System.out.println("[ WE WON ] record = " + singleRecord);
              break;
            } catch (Exception e) {
                System.out.println("No records found, yet... retrying in 1s...");
            }
            Thread.sleep(1000);
        }

        // Assert the message content
        assertNotNull(singleRecord);
        assertEquals("value", singleRecord.value());
    }
}