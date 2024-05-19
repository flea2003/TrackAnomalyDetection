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
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotFoundNotificationException;
import sp.dtos.ExternalAISSignal;
import sp.exceptions.PipelineException;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;
import sp.pipeline.scorecalculators.ScoreCalculationStrategy;
import sp.services.NotificationService;
import java.io.IOException;
import java.util.HashMap;
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
    private final Aggregator aggregator;

    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private NotificationService notificationService;
    private final int notificationThreshold;
    private KTable<Long, CurrentShipDetails> state;


    /**
     * Constructor for the AnomalyDetectionPipeline.
     *
     * @param scoreCalculationStrategy Strategy the strategy to use for calculating the anomaly scores
     */
    @Autowired
    public AnomalyDetectionPipeline(@Qualifier("simpleScoreCalculator")ScoreCalculationStrategy scoreCalculationStrategy,
                                    NotificationService notificationService, StreamUtils streamUtils, Aggregator aggregator)
        throws IOException {

        this.notificationService = notificationService;
        notificationThreshold = 30;
        this.scoreCalculationStrategy = scoreCalculationStrategy;
        this.streamUtils = streamUtils;
        this.aggregator = aggregator;

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
        buildNotifications(builder);
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
                                return aggregator.aggregateSignals(aggregatedShipDetails, valueJson);
                            } catch (JsonProcessingException e) {
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
     *
     * @return the current scores of the ships in the system.
     */
    public HashMap<Long, AnomalyInformation> getCurrentScores() throws PipelineException {
        try {
            // Get the current state of the KTable
            final String storeName = this.state.queryableStoreName();
            ReadOnlyKeyValueStore<Long, CurrentShipDetails> view = this.kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            );

            // Create a copy of the state considering only the current AnomalyInformation values for each ship
            HashMap<Long, AnomalyInformation> stateCopy = new HashMap<>();
            try (KeyValueIterator<Long, CurrentShipDetails> iter = view.all()) {
                iter.forEachRemaining(kv -> stateCopy.put(kv.key, kv.value.getCurrentAnomalyInformation()));
            }
            return stateCopy;

        } catch (Exception e) {
            String err = "Failed to query store: " + e.getMessage() + ", continuing";
            System.out.println(err);
            throw new PipelineException(err);
        }
    }

    /**
     * Returns the current (last updated) AIS signals of the ships in the system.
     *
     * @return the current (last updated) AIS signals of the ships in the system.
     */
    public HashMap<Long, AISSignal> getCurrentAISSignals() throws PipelineException {
        try {
            // Get the current state of the KTable
            final String storeName = this.state.queryableStoreName();
            ReadOnlyKeyValueStore<Long, CurrentShipDetails> view = this.kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            );

            // Create a copy of the state considering only the current AISSignal values for each ship
            HashMap<Long, AISSignal> stateCopy = new HashMap<>();
            try (KeyValueIterator<Long, CurrentShipDetails> iter = view.all()) {
                iter.forEachRemaining(kv -> stateCopy.put(kv.key, kv.value.getCurrentAISSignal()));
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
     * notificationsService class, and for each ship, its last notification is retrieved (this is not exactly what
     * happens, but the idea is the same). Then, from the ships-scores topic, a stream of AnomalyInformation objects is
     * constantly being retrieved. The aggregation part is responsible for the logic of computing when a new
     * notification should be created, and once it has to be created, querying the notificationService class, which
     * handles it. It then also updates the most recent notification that is stored for that particular ship.
     * Note that to decide whether a new notification for a particular ship should be created, it is enough to have the
     * information of the mosrt recent notification for that ship, and a new AnomalyInformation signal.
     *
     * @param builder StreamsBuilder object
     */
    public void buildNotifications(StreamsBuilder builder) throws IOException {
        // Construct a stream for computed AnomalyInformation objects
        KStream<String, String> streamAnomalyInformationJSON = builder.stream(calculatedScoresTopicName);
        KStream<String, AnomalyInformation> streamAnomalyInformation  = streamAnomalyInformationJSON.mapValues(x -> {
            try {
                return AnomalyInformation.fromJson(x);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        // Key the stream
        KStream<Long, AnomalyInformation> streamAnomalyInformationKeyed = streamAnomalyInformation
                .selectKey((key, value) -> value.getId());

        // Construct the KTable (state that is stored) by aggregating the merged stream
        KTable<Long, AnomalyInformation> notificationsState = streamAnomalyInformationKeyed
                .mapValues(x -> {
                    try {
                        return x.toJson();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .groupByKey()
                .aggregate(
                        AnomalyInformation::new,
                        (key, valueJson, lastInformation) -> {
                            try {
                                return aggregateNotificationSignals(lastInformation, valueJson, key);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Materialized
                                .<Long, AnomalyInformation, KeyValueStore<Bytes, byte[]>>as(KAFKA_STORE_NOTIFICATIONS_NAME)
                                .withValueSerde(AnomalyInformation.getSerde())
                );
        this.kafkaStreams = streamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();
    }

    /**
     * Method that is responsible for aggregating the AnomalyInformation signals, and creating and storing new
     * notifications.
     *
     * @param currentAnomaly AnomalyInformation object that corresponds to the most recently created notification
     * @param valueJson JSON value of the AnomalyInformation object that just arrived
     * @param key hash of the ship
     * @return anomaly information that corresponds to the newest notification (so either the new anomaly information,
     *      or the old one)
     * @throws JsonProcessingException in case JSON value does not correspond to an AnomalyInformation object
     */
    public AnomalyInformation aggregateNotificationSignals(AnomalyInformation currentAnomaly, String valueJson, Long key)
            throws JsonProcessingException {
        // Convert the new anomaly information object from JSON TODO: perhaps add the threshold to some conf file!
        AnomalyInformation newAnomaly = AnomalyInformation.fromJson(valueJson);
        // Check if the stored current anomaly object has null fields (meaning that the backend has restarted!)
        if (currentAnomaly.getCorrespondingTimestamp() == null) {
            try {
                // Fetch the anomaly information that corresponds to the most recently saved notification
                currentAnomaly = notificationService.getNewestNotificationForShip(key).getAnomalyInformation();
            } catch (NotFoundNotificationException e) {
                // If there were no notifications saved (meaning that ship never became anomalous), save the current
                // state as the newest anomaly object
                currentAnomaly = newAnomaly;
                if (currentAnomaly.getScore() >= notificationThreshold) {
                    // If that newest anomaly score exceeds the threshold, add a new notification to the database
                    // TODO: here also a query to the AIS signals database will have to take place, to retrieve a
                    //  corresponding AIS signal
                    notificationService.addNotification(newAnomaly);
                }
            }
        }
        // If the current anomaly score exceeds the threshold, then we will for
        // TODO: when Victor merges, also logic for checking if new TYPES of anomalies emerged will need to be added
        if (currentAnomaly.getScore() >= notificationThreshold) {
            // Store the same anomaly object (although it does not matter which is stored currently)
            if (newAnomaly.getScore() >= notificationThreshold) {
                newAnomaly = currentAnomaly;
            }
            // Otherwise, if the new anomaly score is lower, in the state we will store the new one.
        } else {
            if (newAnomaly.getScore() < notificationThreshold) {
                newAnomaly = currentAnomaly;
            } else {
                // Otherwise, if now the anomaly exceeds the threshold, we need to store it in the database
                // TODO: here also a query to the AIS signals database will have to take place, to retrieve
                //  a corresponding AIS signal
                notificationService.addNotification(newAnomaly);
            }
        }
        return newAnomaly;
    }

    /**
     * Aggregates data to a resulting map.
     *
     * @param aggregatedShipDetails object that stores the latest received data for a ship
     * @param valueJson json value for a signal
     * @param key hash value of the ship
     * @return updated object that stores all needed data for a ship
     */
    public CurrentShipDetails aggregateSignals(CurrentShipDetails aggregatedShipDetails, String valueJson, Long key)
            throws JsonProcessingException {

        ShipInformation shipInformation = ShipInformation.fromJson(valueJson);
        AnomalyInformation anomalyInformation = shipInformation.getAnomalyInformation();
        AISSignal aisSignal = shipInformation.getAisSignal();

        // Boolean to check if the current aggregated ship details object has not yet been fully initialized
        // i.e., if either no AISSignal or AnomalyInformation has been set yet
        boolean shipDetailsNotInitialized = aggregatedShipDetails.getCurrentAISSignal() == null
                || aggregatedShipDetails.getCurrentAnomalyInformation() == null;

        // If the processed ShipInformation instance encapsulates a AISSignal instance:
        // update the current value of the AISSignal field
        if (aisSignal != null && (shipDetailsNotInitialized
                || aisSignal.getTimestamp().isAfter(aggregatedShipDetails.getCurrentAISSignal().getTimestamp()))
        ) {
            aggregatedShipDetails.setCurrentAISSignal(aisSignal);
        }

        // If the processed ShipInformation instance encapsulates a AnomalyInformation instance:
        // update the current value of the AnomalyInformation field
        if (anomalyInformation != null
                && (shipDetailsNotInitialized || anomalyInformation.getCorrespondingTimestamp()
                .isAfter(aggregatedShipDetails.getCurrentAnomalyInformation().getCorrespondingTimestamp()))
        ) {
            aggregatedShipDetails.setCurrentAnomalyInformation(anomalyInformation);
        }

        return aggregatedShipDetails;
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
