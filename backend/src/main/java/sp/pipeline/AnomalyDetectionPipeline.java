package sp.pipeline;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.exceptions.PipelineException;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.pipeline.parts.aggregation.ScoreAggregationBuilder;
import sp.pipeline.parts.identification.IdAssignmentBuilder;
import sp.pipeline.parts.notifications.NotificationsDetectionBuilder;
import sp.pipeline.parts.scoring.ScoreCalculationBuilder;

import java.io.IOException;
import java.util.HashMap;

@Service
public class AnomalyDetectionPipeline {

    private final StreamUtils streamUtils;
    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<Long, CurrentShipDetails> state;
    private final IdAssignmentBuilder idAssignmentBuilder;
    private final ScoreCalculationBuilder scoreCalculationBuilder;
    private final ScoreAggregationBuilder scoreAggregationBuilder;
    private final NotificationsDetectionBuilder notificationsDetectionBuilder;

    /**
     * Constructor for the AnomalyDetectionPipeline.
     *
     */
    @Autowired
    public AnomalyDetectionPipeline(StreamUtils streamUtils,
                                    IdAssignmentBuilder idAssignmentBuilder,
                                    ScoreCalculationBuilder scoreCalculationBuilder,
                                    ScoreAggregationBuilder scoreAggregationBuilder,
                                    NotificationsDetectionBuilder notificationsDetectionBuilder) throws IOException {
        this.streamUtils = streamUtils;
        this.idAssignmentBuilder = idAssignmentBuilder;
        this.scoreCalculationBuilder = scoreCalculationBuilder;
        this.scoreAggregationBuilder = scoreAggregationBuilder;
        this.notificationsDetectionBuilder = notificationsDetectionBuilder;

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
        scoreCalculationBuilder.buildScoreCalculationPart(streamWithAssignedIds);

        this.state = scoreAggregationBuilder.buildScoreAggregationPart(builder);
        notificationsDetectionBuilder.buildNotifications(this.state);

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

}
