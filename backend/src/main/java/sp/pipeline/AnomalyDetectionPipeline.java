package sp.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
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
import sp.services.NotificationService;

@Service
public class AnomalyDetectionPipeline {
    private static final String INCOMING_AIS_TOPIC_NAME;
    private static final String CALCULATED_SCORES_TOPIC_NAME;
    private static final String KAFKA_SERVER_ADDRESS;
    private static final String KAFKA_STORE_NAME;
    private static final String KAFKA_STORE_NOTIFICATIONS_NAME;
    private final ScoreCalculationStrategy scoreCalculationStrategy;
    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<String, CurrentShipDetails> state;
    private NotificationService notificationService;
    private final int notificationThreshold;

    // Load the needed parameters from the configurations file
    static {
        try {
            INCOMING_AIS_TOPIC_NAME = StreamUtils.loadConfig().getProperty("incoming.ais.topic.name");
            CALCULATED_SCORES_TOPIC_NAME = StreamUtils.loadConfig().getProperty("calculated.scores.topic.name");
            KAFKA_SERVER_ADDRESS = StreamUtils.loadConfig().getProperty("kafka.server.address");
            KAFKA_STORE_NAME = StreamUtils.loadConfig().getProperty("kafka.store.name");
            KAFKA_STORE_NOTIFICATIONS_NAME = StreamUtils.loadConfig().getProperty("kafka.store.name-notifications");
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
    public AnomalyDetectionPipeline(ScoreCalculationStrategy scoreCalculationStrategy, NotificationService notificationService) throws IOException {
        this.scoreCalculationStrategy = scoreCalculationStrategy;
        this.notificationService = notificationService;
        notificationThreshold = 30;
        buildPipeline();
    }

    /**
     * Private helper method for building the sp.pipeline.
     */
    private void buildPipeline() throws IOException {
        // Create a keyed Kafka Stream of incoming AnomalyInformation signals
        StreamsBuilder builder = new StreamsBuilder();

        buildScoreCalculationPart();
        buildScoreAggregationPart(builder);
        buildNotifications(builder);
    }

    /**
     * Builds the first part of the sp.pipeline - the score calculation part, done in Flink. This sp.pipeline
     * consumes AIS signals from Kafka, calculates the anomaly scores (in Flink) and sends them to back
     * to Kafka into another topic.

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     */
    private void buildScoreCalculationPart() {
        this.flinkEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create a Flink stream that consumes AIS signals from Kafka
        KafkaSource<String> kafkaSource = StreamUtils.getFlinkStreamConsumingFromKafka(INCOMING_AIS_TOPIC_NAME);
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
                .setBootstrapServers(KAFKA_SERVER_ADDRESS)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(CALCULATED_SCORES_TOPIC_NAME)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                // TODO: Maybe we need this, maybe not. Not sure yet.
                //  .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .build();

        updateStreamSerialized.sinkTo(sink);
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
    private void buildScoreAggregationPart(StreamsBuilder builder) {
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
                        return aggregateSignals(aggregatedShipDetails, valueJson, key);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
            Materialized
                    .<String, CurrentShipDetails, KeyValueStore<Bytes, byte[]>>as(KAFKA_STORE_NAME)
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
    private KStream<String, ShipInformation> streamAISSignals(StreamsBuilder builder) {

        // Take the initial AISSignal and wrap them into ShipInformation objects, so we could later merge the stream
        // with already wrapped AnomalyInformation objects
        KStream<String, String> streamAISSignalsJSON = builder.stream(INCOMING_AIS_TOPIC_NAME);
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
        KStream<String, String> streamAnomalyInformationJSON = builder.stream(CALCULATED_SCORES_TOPIC_NAME);
        KStream<String, ShipInformation> streamAnomalyInformation  = streamAnomalyInformationJSON.mapValues(x -> {
            System.out.println("Received AnomalyInformation object as JSON string in ship-scores. JSON: " + x);
            AnomalyInformation anomalyInformation = null;
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
     * Aggregates data to a resulting map.
     *
     * @param aggregatedShipDetails object that stores all needed data for a ship
     * @param valueJson json value for a signal
     * @param key hash value of the ship
     * @return updated object that stores all needed data for a ship
     */
    public CurrentShipDetails aggregateSignals(CurrentShipDetails aggregatedShipDetails, String valueJson, String key)
            throws JsonProcessingException {
        System.out.println("Started aggregating JSON value. JSON: " + valueJson);

        // If this is the first signal received, instantiate the past information as an empty list
        if (aggregatedShipDetails.getPastInformation() == null)
            aggregatedShipDetails.setPastInformation(new ArrayList<>());

        ShipInformation shipInformation = ShipInformation.fromJson(valueJson);
        AnomalyInformation anomalyInformation = shipInformation.getAnomalyInformation();

        // If the signal is AIS signal, add it to past information
        if (shipInformation.getAnomalyInformation() == null) {
            aggregatedShipDetails.getPastInformation().add(shipInformation);
        } else if (shipInformation.getAisSignal() == null) {
            // If the signal is Anomaly Information signal, attach it to a corresponding AIS signal

            // Set the anomaly information to be the most recent one
            // TODO: take care of proper format for the date
            // TODO: CONSIDER ANOMALY INFO ARRIVING EARLIER THAN AIS SIGNAL
            aggregatedShipDetails.setAnomalyInformation(shipInformation.getAnomalyInformation());

            // Find the corresponding AISSignal for the AnomalyInformation object, and update the ShipInformation object
            for (int i = aggregatedShipDetails.getPastInformation().size() - 1; i >= 0; i--) {
                ShipInformation information = aggregatedShipDetails.getPastInformation().get(i);

                if (information.getAisSignal().getTimestamp().isEqual(anomalyInformation.getCorrespondingTimestamp())) {
                    // Check that there are no problems with the data
                    assert information.getAisSignal().getShipHash().equals(anomalyInformation.getShipHash());
                    assert information.getShipHash().equals(anomalyInformation.getShipHash());

                    information.setAnomalyInformation(anomalyInformation);
                    aggregatedShipDetails.setAnomalyInformation(anomalyInformation);
                    break;
                }
            }
        } else throw new RuntimeException("Something went wrong");

        System.out.println("Current ship details after aggregation, for " + key + " ship: " + aggregatedShipDetails);
        return aggregatedShipDetails;
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

    /**
     * Notification pipeline building part. The idea is the following: there is a database, where all notifications are
     * stored. When backend restarts, a state (which is actually the notifications Kafka table) queries the
     * notificationsService class, and for each ship, its last notification is retrieved (this is not exactly what
     * happens, but the idea is the same). Then, from the ships-scores topic, a stream of AnomalyInformation objects is
     * constantly being retrieved. The aggregation part is responsible for the logic of computing when a new
     * notification should be created, and once it has to be created, querying the notificationService class, which
     * handles it. It then also updates the most recent notification that is stored for that particular ship.
     *
     * Note that to decide whether a new notification for a particular ship should be created, it is enough to have the
     * information of the mosrt recent notification for that ship, and a new AnomalyInformation signal.
     *
     * @param builder
     */
    public void buildNotifications(StreamsBuilder builder) {
        // Construct a stream for computed AnomalyInformation objects
        KStream<String, String> streamAnomalyInformationJSON = builder.stream(CALCULATED_SCORES_TOPIC_NAME);
        KStream<String, AnomalyInformation> streamAnomalyInformation  = streamAnomalyInformationJSON.mapValues(x -> {
            try {
                return AnomalyInformation.fromJson(x);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        // Key the stream
        KStream<String, AnomalyInformation> streamAnomalyInformationKeyed = streamAnomalyInformation.selectKey((key, value) -> value.getShipHash());

        // Construct the KTable (state that is stored) by aggregating the merged stream
        KTable<String, AnomalyInformation> notificationsState = streamAnomalyInformationKeyed
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
                                return aggregateSignals(lastInformation, valueJson, key);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Materialized
                                .<String, AnomalyInformation, KeyValueStore<Bytes, byte[]>>as(KAFKA_STORE_NOTIFICATIONS_NAME)
                                .withValueSerde(AnomalyInformation.getSerde())
                );

        this.kafkaStreams = StreamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();
    }

    /**
     * Method that is responsible for aggregating the AnomalyInformation signals, and creating and storing new
     * notifications
     *
     * @param currentAnomaly AnomalyInformation object that corresponds to the most recently created notification
     * @param valueJson JSON value of the AnomalyInformation object that just arrived
     * @param key hash of the ship
     * @return anomaly information that corresponds to the newest notification (so either the new anomaly information,
     * or the old one)
     * @throws JsonProcessingException
     */
    public AnomalyInformation aggregateSignals(AnomalyInformation currentAnomaly, String valueJson, String key) throws JsonProcessingException {
        // TODO: perhaps add the threshold to some configurations file!

        // Convert the new anomaly information object from JSON
        AnomalyInformation newAnomaly = AnomalyInformation.fromJson(valueJson);

        // Check if the stored current anomaly object has null fields (meaning that the backend has restarted!)
        if (currentAnomaly.getCorrespondingTimestamp() == null) {
            try {

                // Fetch the anomaly information that corresponds to the most recently saved notification
                currentAnomaly = notificationService.getNewestNotificationForShip(key).getAnomalyInformation();

            } catch (EntityNotFoundException e ) {

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

            // Store the old anomaly object
            if (newAnomaly.getScore() < notificationThreshold) {
                newAnomaly = currentAnomaly;
            } else {

                // Otherwise, if now the anomaly exceeds the threshold, we need to store it in the database
                // TODO: here also a query to the AIS signals database will have to take place, to retrieve a
                //  corresponding AIS signal
                notificationService.addNotification(newAnomaly);
            }
        }
        return newAnomaly;
    }
}
