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
import org.springframework.stereotype.Service;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.exceptions.PipelineException;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;
import sp.pipeline.scorecalculators.ScoreCalculationStrategy;

@Service
public class AnomalyDetectionPipeline {

    private static final String INCOMING_AIS_TOPIC_NAME_PROPERTY = "incoming.ais.topic.name";
    private static final String CALCULATED_SCORES_TOPIC_NAME_PROPERTY = "calculated.scores.topic.name";
    private static final String KAFKA_SERVER_ADDRESS_PROPERTY = "kafka.server.address";
    private static final String KAFKA_STORE_NAME_PROPERTY = "kafka.store.name";

    private String incomingAisTopicName;
    private String calculatedScoresTopicName;
    private String kafkaServerAddress;
    private String kafkaStoreName;

    private final ScoreCalculationStrategy scoreCalculationStrategy;
    private final StreamUtils streamUtils;
    private final Aggregator aggregator;

    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<String, CurrentShipDetails> state;

    /**
     * Constructor for the AnomalyDetectionPipeline.
     *
     * @param scoreCalculationStrategy Strategy the strategy to use for calculating the anomaly scores
     */
    @Autowired
    public AnomalyDetectionPipeline(ScoreCalculationStrategy scoreCalculationStrategy,
                                    StreamUtils streamUtils, Aggregator aggregator) {
        this.scoreCalculationStrategy = scoreCalculationStrategy;
        this.streamUtils = streamUtils;
        this.aggregator = aggregator;

        loadParametersFromConfigFile();
        buildPipeline();
    }

    public void closePipeline() throws Exception {
        kafkaStreams.close();
        kafkaStreams.cleanUp();
        flinkEnv.close();
    }

    /**
     * Loads the needed parameters from the configuration file.
     */
    private void loadParametersFromConfigFile() {
        Properties config;
        try {
            config = streamUtils.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (config == null) {
            throw new RuntimeException("Properties file not found");
        }

        incomingAisTopicName = config.getProperty(INCOMING_AIS_TOPIC_NAME_PROPERTY);
        calculatedScoresTopicName = config.getProperty(CALCULATED_SCORES_TOPIC_NAME_PROPERTY);
        kafkaServerAddress = config.getProperty(KAFKA_SERVER_ADDRESS_PROPERTY);
        kafkaStoreName = config.getProperty(KAFKA_STORE_NAME_PROPERTY);
    }

    /**
     * Private helper method for building the sp.pipeline.
     */
    private void buildPipeline() {
        buildScoreCalculationPart();
        buildScoreAggregationPart();
    }

    /**
     * Builds the first part of the `sp.pipeline` - the score calculation part, done in Flink. This `sp.pipeline`
     * consumes AIS signals from Kafka, calculates the anomaly scores (in Flink) and sends them to back
     * to Kafka into another topic.

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     */
    private void buildScoreCalculationPart() {
        this.flinkEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create a Flink stream that consumes AIS signals from Kafka
        KafkaSource<String> kafkaSource = streamUtils.getFlinkStreamConsumingFromKafka(incomingAisTopicName);
        DataStream<String> sourceSerialized = flinkEnv.fromSource(kafkaSource,
                WatermarkStrategy.noWatermarks(), "AIS Source");

        // Map stream from JSON strings to AISSignal objects
        DataStream<AISSignal> source = sourceSerialized.map((x) -> {
            System.out.println("Received AIS signal as JSON to topic ships-AIS. JSON: " + x);
            return AISSignal.fromJson(x);
        });

        // Set up the anomaly detection part of the sp.pipeline (happens in Flink)
        DataStream<AnomalyInformation> updateStream =
                scoreCalculationStrategy.setupFlinkAnomalyScoreCalculationPart(source);

        // Map the computed AnomalyInformation objects to JSON strings
        DataStream<String> updateStreamSerialized = updateStream.map(x -> {
            System.out.println("Mapping the AnomalyInformation object to JSON (from the ships-AIS topic). Object: " + x);
            return x.toJson();
        });

        // Send the calculated AnomalyInformation objects to Kafka
        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaServerAddress)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(calculatedScoresTopicName)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                // TODO: Maybe we need this, maybe not. Not sure yet.
                //  .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .build();

        updateStreamSerialized.sinkTo(sink);
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
    private void buildScoreAggregationPart() {
        StreamsBuilder builder = new StreamsBuilder();

        // Construct and merge two streams and select the ship hash as a key for the new stream.
        KStream<String, ShipInformation> mergedStream = mergeStreams(builder);

        // Construct the KTable (state that is stored) by aggregating the merged stream
        KTable<String, CurrentShipDetails> table = mergedStream
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
                        return aggregator.aggregateSignals(aggregatedShipDetails, valueJson, key);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
            Materialized
                    .<String, CurrentShipDetails, KeyValueStore<Bytes, byte[]>>as(kafkaStoreName)
                    .withValueSerde(CurrentShipDetails.getSerde())
            );

        builder.build();
        this.state = table;
        this.kafkaStreams = streamUtils.getKafkaStreamConsumingFromKafka(builder);
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
    private KStream<String, ShipInformation> streamAISSignals(StreamsBuilder builder) {

        // Take the initial AISSignal and wrap them into ShipInformation objects, so we could later merge the stream
        // with already wrapped AnomalyInformation objects
        KStream<String, String> streamAISSignalsJSON = builder.stream(incomingAisTopicName);
        KStream<String, ShipInformation> streamAISSignals = streamAISSignalsJSON
                .mapValues(x -> {
                    System.out.println("Received AIS signal as JSON to topic ship-AIS for the building part. JSON: " + x);
                    AISSignal aisSignal;
                    try {
                        aisSignal = AISSignal.fromJson(x);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    return new ShipInformation(aisSignal.getShipHash(), null, aisSignal);
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
    private KStream<String, ShipInformation> streamAnomalyInformation(StreamsBuilder builder) {

        // Take computed AnomalyInformation JSON strings, deserialize them and wrap them into ShipInformation objects,
        // so we could later merge the stream with wrapped simple AISSignal objects
        KStream<String, String> streamAnomalyInformationJSON = builder.stream(calculatedScoresTopicName);
        KStream<String, ShipInformation> streamAnomalyInformation  = streamAnomalyInformationJSON.mapValues(x -> {
            System.out.println("Received AnomalyInformation object as JSON string in ship-scores. JSON: " + x);
            AnomalyInformation anomalyInformation;
            try {
                anomalyInformation = AnomalyInformation.fromJson(x);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return new ShipInformation(anomalyInformation.getShipHash(), anomalyInformation, null);
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
     * Returns the current scores of the ships in the system.
     *
     * @return the current scores of the ships in the system.
     */
    public HashMap<String, CurrentShipDetails> getCurrentScores() throws PipelineException {
        try {
            // Get the current state of the KTable
            final String storeName = this.state.queryableStoreName();
            ReadOnlyKeyValueStore<String, CurrentShipDetails> view = this.kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            );

            // Create a copy of the state
            HashMap<String, CurrentShipDetails> stateCopy = new HashMap<>();
            try (KeyValueIterator<String, CurrentShipDetails> iter = view.all()) {
                iter.forEachRemaining(kv -> stateCopy.put(kv.key, kv.value));
            }
            return stateCopy;

        } catch (Exception e) {
            String err = "Failed to query store: " + e.getMessage() + ", continuing";
            System.out.println(err);
            throw new PipelineException(err);
        }
    }

    /**
     * Method that constructs a unified stream of anomaly information and AIS signals, wrapped in ShipInformation class.
     *
     * @param builder Streams builder
     * @return unified stream
     */
    private KStream<String, ShipInformation> mergeStreams(StreamsBuilder builder) {
        // Construct two separate streams for AISSignals and computed AnomalyScores, and wrap each stream values into
        // ShipInformation object, so that we could later merge these two streams
        KStream<String, ShipInformation> streamAnomalyInformation  = streamAnomalyInformation(builder);
        KStream<String, ShipInformation> streamAISSignals = streamAISSignals(builder);

        // Merge two streams and select the ship hash as a key for the new stream.
        return streamAISSignals
                .merge(streamAnomalyInformation)
                .selectKey((key, value) -> value.getShipHash());
    }
}
