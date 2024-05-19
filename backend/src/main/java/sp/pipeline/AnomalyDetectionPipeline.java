package sp.pipeline;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
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
import sp.dtos.ExtendedAnomalyInformation;
import sp.dtos.ExternalAISSignal;
import sp.exceptions.PipelineException;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.MaxAnomalyScoreDetails;
import sp.model.ShipInformation;
import sp.pipeline.scorecalculators.ScoreCalculationStrategy;
import java.util.Objects;

@Service
public class AnomalyDetectionPipeline {
    private static final String RAW_INCOMING_AIS_TOPIC_NAME;
    private static final String INCOMING_AIS_TOPIC_NAME;
    private static final String CALCULATED_SCORES_TOPIC_NAME;
    private static final String KAFKA_SERVER_ADDRESS;
    private static final String KAFKA_STORE_NAME;
    private final ScoreCalculationStrategy scoreCalculationStrategy;
    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<Long, CurrentShipDetails> state;

    // Load the needed parameters from the configurations file
    static {
        try {
            RAW_INCOMING_AIS_TOPIC_NAME = StreamUtils.loadConfig().getProperty("incoming.ais-raw.topic.name");
            INCOMING_AIS_TOPIC_NAME = StreamUtils.loadConfig().getProperty("incoming.ais.topic.name");
            CALCULATED_SCORES_TOPIC_NAME = StreamUtils.loadConfig().getProperty("calculated.scores.topic.name");
            KAFKA_SERVER_ADDRESS = StreamUtils.loadConfig().getProperty("kafka.server.address");
            KAFKA_STORE_NAME = StreamUtils.loadConfig().getProperty("kafka.store.name");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor for the AnomalyDetectionPipeline.
     *
     * @param scoreCalculationStrategy Strategy the strategy to use for calculating the anomaly scores
     */
    @Autowired
    public AnomalyDetectionPipeline(@Qualifier("simpleScoreCalculator")ScoreCalculationStrategy scoreCalculationStrategy)
        throws IOException {
        this.scoreCalculationStrategy = scoreCalculationStrategy;
        buildPipeline();
    }

    /**
     * Private helper method for building the sp.pipeline.
     */
    private void buildPipeline() throws IOException {
        this.flinkEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        // Build the pipeline
        DataStream<AISSignal> streamWithAssignedIds = buildIdAssignmentPart();
        buildScoreCalculationPart(streamWithAssignedIds);
        buildScoreAggregationPart();
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
    private DataStream<AISSignal> buildIdAssignmentPart() {
        // Create a Flink stream that consumes AIS signals from Kafka
        KafkaSource<String> kafkaSource = StreamUtils.getFlinkStreamConsumingFromKafka(RAW_INCOMING_AIS_TOPIC_NAME);
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
     * Builds the first part of the sp.pipeline - the score calculation part, done in Flink. This sp.pipeline
     * consumes AIS signals from Kafka, calculates the anomaly scores (in Flink) and sends them to back
     * to Kafka into another topic.

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     */
    private void buildScoreCalculationPart(DataStream<AISSignal> source) {

        // Send the id-assigned AISSignal objects to a Kafka topic (to be used later when aggregating the scores)
        KafkaSink<String> signalsSink = createSinkFlinkToKafka(KAFKA_SERVER_ADDRESS, INCOMING_AIS_TOPIC_NAME);
        source.map(AISSignal::toJson).sinkTo(signalsSink);

        // Set up the anomaly detection part of the sp.pipeline (happens in Flink)
        DataStream<AnomalyInformation> updateStream = scoreCalculationStrategy.setupFlinkAnomalyScoreCalculationPart(source);

        // Map the computed AnomalyInformation objects to JSON strings
        DataStream<String> updateStreamSerialized = updateStream.map(AnomalyInformation::toJson);

        // Send the calculated AnomalyInformation objects to Kafka
        KafkaSink<String> scoresSink = createSinkFlinkToKafka(KAFKA_SERVER_ADDRESS, CALCULATED_SCORES_TOPIC_NAME);
        updateStreamSerialized.sinkTo(scoresSink);
    }

    /**
     * Builds the second part of the sp.pipeline - the score aggregation part. In particular, this part
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
    private void buildScoreAggregationPart() {
        // Create a keyed Kafka Stream of incoming AnomalyInformation signals
        StreamsBuilder builder = new StreamsBuilder();

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
                                return aggregateSignals(aggregatedShipDetails, valueJson, key);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Materialized
                                .<Long, CurrentShipDetails, KeyValueStore<Bytes, byte[]>>as(KAFKA_STORE_NAME)
                                .withValueSerde(CurrentShipDetails.getSerde())
                );

        builder.build();
        this.state = table;
        this.kafkaStreams = StreamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();
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
        KStream<Long, String> streamAISSignalsJSON = builder.stream(INCOMING_AIS_TOPIC_NAME);
        KStream<Long, ShipInformation> streamAISSignals = streamAISSignalsJSON
                .mapValues(x -> {
                    AISSignal aisSignal;
                    aisSignal = AISSignal.fromJson(x);
                    return new ShipInformation(aisSignal.getId(), null, aisSignal);
                });
        return streamAISSignals;
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
        KStream<Long, String> streamAnomalyInformationJSON = builder.stream(CALCULATED_SCORES_TOPIC_NAME);
        KStream<Long, ShipInformation> streamAnomalyInformation  = streamAnomalyInformationJSON.mapValues(x -> {
            AnomalyInformation anomalyInformation = null;
            try {
                anomalyInformation = AnomalyInformation.fromJson(x);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return new ShipInformation(anomalyInformation.getId(), anomalyInformation, null);
        });

        return streamAnomalyInformation;
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
    public HashMap<Long, ExtendedAnomalyInformation> getCurrentScores() throws PipelineException {
        try {
            // Get the current state of the KTable
            final String storeName = this.state.queryableStoreName();
            ReadOnlyKeyValueStore<Long, CurrentShipDetails> view = this.kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            );

            // Create a copy of the state considering only the current AnomalyInformation values for each ship
            HashMap<Long, ExtendedAnomalyInformation> stateCopy = new HashMap<>();
            try (KeyValueIterator<Long, CurrentShipDetails> iter = view.all()) {
                iter.forEachRemaining(kv -> stateCopy
                        .put(kv.key, new ExtendedAnomalyInformation(kv.value.getCurrentAnomalyInformation(),
                                kv.value.getMaxAnomalyScoreInfo())));
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

            // Create a copy of the state considering only the current AISSingnal values for each ship
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

        // If the processed ShipInformation instance encapsulates a AISSignal instance:
        // update the current value of the AISSignal field
        if (aisSignal != null
                && (aggregatedShipDetails.getCurrentAISSignal() == null
                || aisSignal.getTimestamp().isAfter(aggregatedShipDetails.getCurrentAISSignal().getTimestamp()))) {

            aggregatedShipDetails.setCurrentAISSignal(aisSignal);
        }

        // If the processed ShipInformation instance encapsulates a AnomalyInformation instance:
        // update the current value of the AnomalyInformation field, additionally modifying the value
        // of the highest recorder Anomaly Score for the ship
        if (anomalyInformation != null
                && (aggregatedShipDetails.getCurrentAnomalyInformation() == null
                || anomalyInformation.getCorrespondingTimestamp()
                .isAfter(aggregatedShipDetails.getCurrentAnomalyInformation().getCorrespondingTimestamp()))) {

            aggregatedShipDetails.setCurrentAnomalyInformation(anomalyInformation);

            // Update the value of the maxAnomalyScoreInfo field
            MaxAnomalyScoreDetails updatedMaxAnomalyScoreDetails = updateMaxScoreDetails(aggregatedShipDetails,
                    anomalyInformation);

            aggregatedShipDetails.setMaxAnomalyScoreInfo(updatedMaxAnomalyScoreDetails);

        }

        return aggregatedShipDetails;
    }

    /**
     * Utility method for updating the maxAnomalyScoreInfo filed of the streams aggregating object.
     *
     * @return - the updated MaxAnomalyScoreDetails instance
     */
    private MaxAnomalyScoreDetails updateMaxScoreDetails(CurrentShipDetails aggregatedShipDetails,
                                                         AnomalyInformation anomalyInformation) {
        // Given that we received a new AnomalyInformation signal we have to update
        // the MaxAnomalyScoreDetails field
        // If the field maxAnomalyScoreInfo of the aggregating object is not initialized:
        // consider the value of the highest recorded score to be 0
        // consider the value of the corresponding timestamp to be null
        boolean isMaxScoreInitialized = aggregatedShipDetails.getMaxAnomalyScoreInfo() == null;

        float currentMaxScore = isMaxScoreInitialized
                ? 0 : aggregatedShipDetails.getMaxAnomalyScoreInfo().getMaxAnomalyScore();

        float newMaxScore = currentMaxScore < anomalyInformation.getScore()
                ? anomalyInformation.getScore() : currentMaxScore;

        OffsetDateTime currentTimestamp = isMaxScoreInitialized
                ? null : aggregatedShipDetails.getMaxAnomalyScoreInfo().getCorrespondingTimestamp();

        OffsetDateTime newTimestamp = newMaxScore == anomalyInformation.getScore()
                ? anomalyInformation.getCorrespondingTimestamp() : currentTimestamp;

        return new MaxAnomalyScoreDetails(newMaxScore, newTimestamp);
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
                .selectKey((key, value) -> value.getId());
    }
}
