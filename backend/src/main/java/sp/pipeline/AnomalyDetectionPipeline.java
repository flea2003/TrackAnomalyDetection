package sp.pipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import sp.model.AnomalyInformation;
import sp.dtos.ExternalAISSignal;
import sp.exceptions.PipelineException;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.Notification;
import sp.model.ShipInformation;
import sp.pipeline.aggregators.CurrentStateAggregator;
import sp.pipeline.aggregators.NotificationsAggregator;
import sp.pipeline.scorecalculators.ScoreCalculationStrategy;
import sp.services.WebSocketShipsDataService;
import java.util.Objects;

@Service
public class AnomalyDetectionPipeline {
    private static final String KAFKA_STORE_NOTIFICATIONS_NAME = "ships-notification-store";
    private static final String RAW_INCOMING_AIS_TOPIC_NAME_PROPERTY = "incoming.ais-raw.topic.name";
    private static final String INCOMING_AIS_TOPIC_NAME_PROPERTY = "incoming.ais.topic.name";
    private static final String CALCULATED_SCORES_TOPIC_NAME_PROPERTY = "calculated.scores.topic.name";
    private static final String KAFKA_SERVER_ADDRESS_PROPERTY = "kafka.server.address";
    private static final String KAFKA_STORE_NAME_PROPERTY = "kafka.store.name";

    private String rawIncomingAisTopicName;
    private String incomingAisTopicName;
    private String calculatedScoresTopicName;
    private String kafkaServerAddress;
    private String kafkaStoreName;

    private final ScoreCalculationStrategy scoreCalculationStrategy;
    private final StreamUtils streamUtils;
    private final CurrentStateAggregator currentStateAggregator;
    private final NotificationsAggregator notificationsAggregator;
    private final WebSocketShipsDataService webSocketShipDataService;

    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<Long, CurrentShipDetails> state;


    /**
     * Constructor for the AnomalyDetectionPipeline.
     *
     * @param scoreCalculationStrategy Strategy the strategy to use for calculating the anomaly scores
     */
    @Autowired
    public AnomalyDetectionPipeline(@Qualifier("simpleScoreCalculator")ScoreCalculationStrategy scoreCalculationStrategy,
                                    StreamUtils streamUtils, CurrentStateAggregator currentStateAggregator,
                                    NotificationsAggregator notificationsAggregator,
                                    WebSocketShipsDataService webSocketShipDataService)
        throws IOException {

        this.scoreCalculationStrategy = scoreCalculationStrategy;
        this.streamUtils = streamUtils;
        this.currentStateAggregator = currentStateAggregator;
        this.notificationsAggregator = notificationsAggregator;
        this.webSocketShipDataService = webSocketShipDataService;

        loadParametersFromConfigFile();
        buildPipeline();
    }

    /**
     * Closes the pipeline by closing KafkaStreams and Flink environment.
     *
     * @throws Exception when closing Flink environment throws exception
     */
    public void closePipeline() throws Exception {
        kafkaStreams.close();
        flinkEnv.close();
    }

    /**
     * Loads the needed parameters from the configuration file.
     */
    private void loadParametersFromConfigFile() throws IOException {
        Properties config = streamUtils.loadConfig();

        if (config == null) {
            throw new IOException("Properties file not found");
        }

        rawIncomingAisTopicName = config.getProperty(RAW_INCOMING_AIS_TOPIC_NAME_PROPERTY);
        incomingAisTopicName = config.getProperty(INCOMING_AIS_TOPIC_NAME_PROPERTY);
        calculatedScoresTopicName = config.getProperty(CALCULATED_SCORES_TOPIC_NAME_PROPERTY);
        kafkaServerAddress = config.getProperty(KAFKA_SERVER_ADDRESS_PROPERTY);
        kafkaStoreName = config.getProperty(KAFKA_STORE_NAME_PROPERTY);
    }

    /**
     * Private helper method for building the sp.pipeline.
     */
    private void buildPipeline() throws IOException {
        this.flinkEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        // Create a keyed Kafka Stream of incoming AnomalyInformation signals
        StreamsBuilder builder = new StreamsBuilder();

        DataStream<AISSignal> streamWithAssignedIds = buildIdAssignmentPart();

        buildScoreCalculationPart(streamWithAssignedIds);
        buildScoreAggregationPart(builder);
        buildNotificationPart();

        this.kafkaStreams = streamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();
    }

    /**
     * Creates a sink from Flink to a Kafka topic.
     *
     * @param kafkaServerAddress Kafka server address
     * @param topicName Kafka topic name to send the data to
     * @return the created KafkaSink object
     */
    private KafkaSink<String> createSinkFlinkToKafka(String kafkaServerAddress, String topicName) {
        return KafkaSink.<String>builder()
                .setBootstrapServers(kafkaServerAddress)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topicName)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .build();
    }

    /**
     * Builds the first part of the pipeline - the part that takes as input the raw AIS signals from Kafka,
     * assigns an internal ID to each signal and sends them to another Kafka topic.
     * The internal ID is calculated as a hash of the producer ID and the ship hash.
     *
     * @return the DataStream with the AISSignal objects that have been assigned an internal ID.
     *         Used in the next step of the pipeline.
     */
    private DataStream<AISSignal> buildIdAssignmentPart() throws IOException {
        // Create a Flink stream that consumes AIS signals from Kafka
        KafkaSource<String> kafkaSource = streamUtils.getFlinkStreamConsumingFromKafka(rawIncomingAisTopicName);
        DataStream<String> rawSourceSerialized = flinkEnv.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "AIS Source");

        // Map stream from JSON strings to ExternalAISSignal objects
        DataStream<ExternalAISSignal> sourceWithNoIDs = rawSourceSerialized.map(ExternalAISSignal::fromJson);

        // Map ExternalAISSignal objects to AISSignal objects by assigning an internal ID
        return sourceWithNoIDs.map(x -> {
            int calculatedID = Objects.hash(x.getProducerID(), x.getShipHash()) & 0x7FFFFFFF; // Ensure positive ID
            return new AISSignal(x, calculatedID);
        });
    }

    /**
     * Builds the first part of the `sp.pipeline` - the score calculation part, done in Flink. This `sp.pipeline`
     * consumes AIS signals from Kafka, calculates the anomaly scores (in Flink) and sends them to back
     * to Kafka into another topic.

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     */
    private void buildScoreCalculationPart(DataStream<AISSignal> source) {

        // Send the id-assigned AISSignal objects to a Kafka topic (to be used later when aggregating the scores)
        KafkaSink<String> signalsSink = createSinkFlinkToKafka(kafkaServerAddress, incomingAisTopicName);
        source.map(AISSignal::toJson).sinkTo(signalsSink);

        // Set up the anomaly detection part of the sp.pipeline (happens in Flink)
        DataStream<AnomalyInformation> updateStream = scoreCalculationStrategy.setupFlinkAnomalyScoreCalculationPart(source);

        // Map the computed AnomalyInformation objects to JSON strings
        DataStream<String> updateStreamSerialized = updateStream.map(AnomalyInformation::toJson);

        // Send the calculated AnomalyInformation objects to Kafka
        KafkaSink<String> scoresSink = createSinkFlinkToKafka(kafkaServerAddress, calculatedScoresTopicName);
        updateStreamSerialized.sinkTo(scoresSink);
    }

    /**
     * Builds the second part of the `sp.pipeline` - the score aggregation part. In particular, this part
     * takes the calculated score updates from Kafka (which were pushed there by the previous part)
     * and aggregates them into a KTable. It also takes care of appending needed AIS signals to the KTable.
     * This KTable is then used as the state of the sp.pipeline.
     *
     * <p>The actual implementation works the following way:
     * 1. Incoming AIS signals are mapped to ShipInformation object with the anomaly score missing.
     * 2. AIS score updates are mapped to ShipInformation objects as well, with AIS signal missing.
     * 3. The AIS-signal type ShipInformation objects get to the KTable first and just get added there.
     * 4. The AIS-score-update type ShipInformation objects  get to the KTable after those and just update the missing
     * anomaly score field in the corresponding places.
     * </p>
     */
    private void buildScoreAggregationPart(StreamsBuilder builder) {
        // Construct and merge two streams and select the ship hash as a key for the new stream.
        KStream<Long, ShipInformation> mergedStream = mergeStreams(builder);

        // Construct the KTable (state that is stored) by aggregating the merged stream
        KTable<Long, CurrentShipDetails> table = mergedStream
                .mapValues(x -> {
                    try {
                        return x.toJson();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .groupByKey()
                .aggregate(
                        CurrentShipDetails::new,
                        (key, valueJson, aggregatedShipDetails) -> {
                            try {
                                CurrentShipDetails newShipState =
                                        currentStateAggregator.aggregateSignals(aggregatedShipDetails, valueJson);
                                // Publish the new CurrentShipDetails state to the clients
                                // Message is forwarded to the client side only if an WebSocket connection is open
                                if (this.webSocketShipDataService.checkForOpenConnections()) {
                                    this.webSocketShipDataService.sendCurrentShipDetails(newShipState);
                                }
                                return newShipState;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Materialized
                                .<Long, CurrentShipDetails, KeyValueStore<Bytes, byte[]>>as(kafkaStoreName)
                                .withValueSerde(CurrentShipDetails.getSerde())
                );

        builder.build();
        this.state = table;
    }

    /**
     * Builds a KStream object that consists of computed AISSignal objects, wrapped around
     * ShipInformation class (i.e. AnomalyInformation is set to null)
     *
     * @param builder streams builder
     * @return a KStream object that consists of computed AISSignal objects, wrapped around
     *     ShipInformation class
     */
    private KStream<Long, ShipInformation> streamAISSignals(StreamsBuilder builder) {

        // Take the initial AISSignal and wrap them into ShipInformation objects, so we could later merge the stream
        // with already wrapped AnomalyInformation objects
        KStream<Long, String> streamAISSignalsJSON = builder.stream(incomingAisTopicName);
        return streamAISSignalsJSON
                .mapValues(x -> {
                    AISSignal aisSignal;
                    try {
                        aisSignal = AISSignal.fromJson(x);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    return new ShipInformation(aisSignal.getId(), null, aisSignal);
                });
    }

    /**
     * Builds a KStream object that consists of computed AnomalyInformation objects, wrapped around
     * ShipInformation class (i.e. AISSignal is set to null)
     *
     * @param builder streams builder
     * @return KStream object that consists of computed AnomalyInformation objects, wrapped around
     *     ShipInformation class
     */
    private KStream<Long, ShipInformation> streamAnomalyInformation(StreamsBuilder builder) {

        // Take computed AnomalyInformation JSON strings, deserialize them and wrap them into ShipInformation objects,
        // so we could later merge the stream with wrapped simple AISSignal objects
        KStream<Long, String> streamAnomalyInformationJSON = builder.stream(calculatedScoresTopicName);

        return streamAnomalyInformationJSON.mapValues(x -> {
            AnomalyInformation anomalyInformation;
            try {
                anomalyInformation = AnomalyInformation.fromJson(x);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return new ShipInformation(anomalyInformation.getId(), anomalyInformation, null);
        });
    }

    /**
     * Starts the stream processing: both Flink anomaly-detection part and Kafka score-aggregation part.
     * Note that this method is not blocking, and it will not be called by the constructor.
     */
    public void runPipeline() {
        try {
            this.flinkEnv.executeAsync();
            this.kafkaStreams.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the current (last updated) anomaly scores of the ships in the system.
     * Additionally return the current max anomaly score information of the ships in the system.
     *
     * @return the current and max scores of the ships in the system.
     */
    public HashMap<Long, CurrentShipDetails> getCurrentShipDetails() throws PipelineException {
        try {
            // Get the current state of the KTable
            final String storeName = this.state.queryableStoreName();
            ReadOnlyKeyValueStore<Long, CurrentShipDetails> view = this.kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            );

            // Create a copy of the state considering only the current AnomalyInformation values for each ship
            HashMap<Long, CurrentShipDetails> stateCopy = new HashMap<>();
            try (KeyValueIterator<Long, CurrentShipDetails> iter = view.all()) {
                iter.forEachRemaining(kv -> stateCopy
                        .put(kv.key, kv.value));
            }
            return stateCopy;

        } catch (Exception e) {
            String err = "Failed to query store: " + e.getMessage() + ", continuing";
            System.out.println(err);
            throw new PipelineException(err);
        }
    }

    /**
     * Notification pipeline building part. The idea is the following: there is a database, where all notifications are
     * stored. When backend restarts, a state (which is actually the notifications Kafka table) queries the
     * notificationsService class, and for each ship, its last notification is retrieved as the initial state
     * Notification object. Then, a stream of updates from the KafkaTable which stores the current state is retrieved:
     * it contains a stream of updates that happen in that table. Then, the stateful mapping part is responsible for the
     * logic of computing when a new notification should be created, and once it has to be created, querying the
     * notificationService class, which handles it. It then also updates the most recent notification that is stored for
     * that particular ship. Note that to decide whether a new notification for a particular ship should be created, it
     * is enough to have the information of the most recent notification for that ship, and a new AnomalyInformation
     * signal (which in our case is wrapped in CurrentShipDetails for optimization purposes for retrieving AIS signal).
     *
     */
    private void buildNotificationPart() throws IOException {
        // Construct a stream for computed AnomalyInformation objects
        KStream<Long, CurrentShipDetails> streamOfUpdates = state.toStream();
        /*
        // Use the following code for easier testing (and also comment out the first line in the method)

        KStream<Long, String> firstStream = builder.stream(calculatedScoresTopicName);
        KStream<Long, AnomalyInformation> second = firstStream.mapValues(x -> {try { return AnomalyInformation
        .fromJson(x); } catch (JsonProcessingException e) {  throw new RuntimeException(e); }  });
        KStream<Long, AnomalyInformation> third = second.selectKey((key, value) -> value.getId());
        KStream<Long, CurrentShipDetails> streamOfUpdates = third.mapValues(x -> new CurrentShipDetails(x, null));
        */
        // Construct the KTable (state that is stored) by aggregating the merged stream
        streamOfUpdates
            .mapValues(x -> {
                try {
                    return x.toJson();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            })
            .groupByKey()
            .aggregate(
                Notification::new,
                (key, valueJson, lastInformation) -> {
                    try {
                        return notificationsAggregator.aggregateSignals(lastInformation, valueJson, key);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                Materialized
                        .<Long, Notification, KeyValueStore<Bytes, byte[]>>as(KAFKA_STORE_NOTIFICATIONS_NAME)
                        .withValueSerde(Notification.getSerde())
            );
    }

    /**
     * Method that constructs a unified stream of AnomalyInformation and AISSignal instances,
     * wrapped inside a ShipInformation class.
     *
     * @param builder StreamsBuilder instance responsible for configuring the KStream instances
     * @return unified stream
     */
    private KStream<Long, ShipInformation> mergeStreams(StreamsBuilder builder) {
        // Construct two separate streams for AISSignals and computed AnomalyScores, and wrap each stream values into
        // ShipInformation object, so that we could later merge these two streams
        KStream<Long, ShipInformation> streamAnomalyInformation  = streamAnomalyInformation(builder);
        KStream<Long, ShipInformation> streamAISSignals = streamAISSignals(builder);

        // Merge two streams and select the ship hash as a key for the new stream.
        return streamAISSignals
                .merge(streamAnomalyInformation)
                .selectKey((key, value) -> value.getShipId());
    }
}
