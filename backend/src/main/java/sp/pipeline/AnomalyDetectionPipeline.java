package sp.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.Notification;
import sp.pipeline.parts.aggregation.ScoreAggregationBuilder;
import sp.pipeline.parts.aggregation.aggregators.NotificationsAggregator;
import sp.pipeline.parts.identification.IdAssignmentBuilder;
import sp.pipeline.parts.scoring.ScoreCalculationBuilder;
import sp.pipeline.parts.scoring.scorecalculators.ScoreCalculationStrategy;

import java.io.IOException;
import java.util.HashMap;

@Service
public class AnomalyDetectionPipeline {

    private final PipelineConfiguration configuration;
    private final ScoreCalculationStrategy scoreCalculationStrategy;
    private final StreamUtils streamUtils;
    private final NotificationsAggregator notificationsAggregator;
    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<Long, CurrentShipDetails> state;
    private final IdAssignmentBuilder idAssignmentBuilder;
    private final ScoreCalculationBuilder scoreCalculationBuilder;
    ScoreAggregationBuilder scoreAggregationBuilder;


    /**
     * Constructor for the AnomalyDetectionPipeline.
     *
     * @param scoreCalculationStrategy Strategy the strategy to use for calculating the anomaly scores
     */
    @Autowired
    public AnomalyDetectionPipeline(@Qualifier("simpleScoreCalculator") ScoreCalculationStrategy scoreCalculationStrategy,
                                    StreamUtils streamUtils,
                                    NotificationsAggregator notificationsAggregator,
                                    PipelineConfiguration configuration,
                                    IdAssignmentBuilder idAssignmentBuilder,
                                    ScoreCalculationBuilder scoreCalculationBuilder,
                                    ScoreAggregationBuilder scoreAggregationBuilder) throws IOException {
        this.scoreCalculationStrategy = scoreCalculationStrategy;
        this.streamUtils = streamUtils;
        this.notificationsAggregator = notificationsAggregator;
        this.configuration = configuration;
        this.idAssignmentBuilder = idAssignmentBuilder;
        this.scoreCalculationBuilder = scoreCalculationBuilder;
        this.scoreAggregationBuilder = scoreAggregationBuilder;

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
        scoreCalculationBuilder.buildScoreCalculationPart(streamWithAssignedIds, scoreCalculationStrategy);

        this.state = scoreAggregationBuilder.buildScoreAggregationPart(builder);
        buildNotifications(builder);

        this.kafkaStreams = streamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();
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
    }
}
