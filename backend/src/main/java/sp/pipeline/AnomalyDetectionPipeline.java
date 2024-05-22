package sp.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
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
import sp.exceptions.PipelineException;
import sp.model.*;
import sp.pipeline.parts.aggregation.aggregators.CurrentStateAggregator;
import sp.pipeline.parts.aggregation.aggregators.NotificationsAggregator;
import sp.pipeline.parts.identification.IdAssignmentBuilder;
import sp.pipeline.parts.scoring.scorecalculators.ScoreCalculationStrategy;

import java.io.IOException;
import java.util.HashMap;

@Service
public class AnomalyDetectionPipeline {

    private final PipelineConfiguration configuration;
    private final ScoreCalculationStrategy scoreCalculationStrategy;
    private final StreamUtils streamUtils;
    private final CurrentStateAggregator currentStateAggregator;
    private final NotificationsAggregator notificationsAggregator;
    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<Long, CurrentShipDetails> state;
    private final IdAssignmentBuilder idAssignmentBuilder;


    /**
     * Constructor for the AnomalyDetectionPipeline.
     *
     * @param scoreCalculationStrategy Strategy the strategy to use for calculating the anomaly scores
     */
    @Autowired
    public AnomalyDetectionPipeline(@Qualifier("simpleScoreCalculator") ScoreCalculationStrategy scoreCalculationStrategy,
                                    StreamUtils streamUtils,
                                    CurrentStateAggregator currentStateAggregator,
                                    NotificationsAggregator notificationsAggregator,
                                    PipelineConfiguration configuration,
                                    IdAssignmentBuilder idAssignmentBuilder) throws IOException {
        this.scoreCalculationStrategy = scoreCalculationStrategy;
        this.streamUtils = streamUtils;
        this.currentStateAggregator = currentStateAggregator;
        this.notificationsAggregator = notificationsAggregator;
        this.configuration = configuration;
        this.idAssignmentBuilder = idAssignmentBuilder;

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
     * Private helper method for building the sp.pipeline.
     */
    private void buildPipeline() throws IOException {
        this.flinkEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        // Create a keyed Kafka Stream of incoming AnomalyInformation signals
        StreamsBuilder builder = new StreamsBuilder();

        DataStream<AISSignal> streamWithAssignedIds = idAssignmentBuilder.buildIdAssignmentPart(flinkEnv);
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
     * Builds the first part of the `sp.pipeline` - the score calculation part, done in Flink. This `sp.pipeline`
     * consumes AIS signals from Kafka, calculates the anomaly scores (in Flink) and sends them to back
     * to Kafka into another topic.

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     */
    private void buildScoreCalculationPart(DataStream<AISSignal> source) {

        // Send the id-assigned AISSignal objects to a Kafka topic (to be used later when aggregating the scores)
        KafkaSink<String> signalsSink = createSinkFlinkToKafka(configuration.kafkaServerAddress, configuration.incomingAisTopicName);
        source.map(AISSignal::toJson).sinkTo(signalsSink);

        // Set up the anomaly detection part of the sp.pipeline (happens in Flink)
        DataStream<AnomalyInformation> updateStream = scoreCalculationStrategy.setupFlinkAnomalyScoreCalculationPart(source);

        // Map the computed AnomalyInformation objects to JSON strings
        DataStream<String> updateStreamSerialized = updateStream.map(AnomalyInformation::toJson);

        // Send the calculated AnomalyInformation objects to Kafka
        KafkaSink<String> scoresSink = createSinkFlinkToKafka(configuration.kafkaServerAddress, configuration.calculatedScoresTopicName);
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
                                return currentStateAggregator.aggregateSignals(aggregatedShipDetails, valueJson);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Materialized
                                .<Long, CurrentShipDetails, KeyValueStore<Bytes, byte[]>>as(configuration.kafkaStoreName)
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
        KStream<Long, String> streamAISSignalsJSON = builder.stream(configuration.incomingAisTopicName);
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
        KStream<Long, String> streamAnomalyInformationJSON = builder.stream(configuration.calculatedScoresTopicName);

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
        KTable<Long, Notification> notificationsState = streamOfUpdates
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
                                .<Long, Notification, KeyValueStore<Bytes, byte[]>>as("temp-notifications-store")
                                .withValueSerde(Notification.getSerde())
                );
        this.kafkaStreams = streamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();
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
