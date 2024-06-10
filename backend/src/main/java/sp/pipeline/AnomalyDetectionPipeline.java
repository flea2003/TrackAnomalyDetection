package sp.pipeline;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.pipeline.parts.aggregation.ScoreAggregationBuilder;
import sp.pipeline.parts.identification.IdAssignmentBuilder;
import sp.pipeline.parts.notifications.NotificationsDetectionBuilder;
import sp.pipeline.parts.scoring.ScoreCalculationBuilder;
import sp.pipeline.utils.OffsetDateTimeSerializer;

@Service
public class AnomalyDetectionPipeline {
    private final StreamExecutionEnvironment flinkEnv;
    private final IdAssignmentBuilder idAssignmentBuilder;
    private final ScoreCalculationBuilder scoreCalculationBuilder;
    private final ScoreAggregationBuilder scoreAggregationBuilder;
    private final NotificationsDetectionBuilder notificationsDetectionBuilder;

    /**
     * Constructor for the AnomalyDetectionPipeline class.
     *
     * @param idAssignmentBuilder builder for the id assignment part of the pipeline
     * @param scoreCalculationBuilder builder for the score calculation part of the pipeline
     * @param scoreAggregationBuilder builder for the score aggregation part of the pipeline
     * @param notificationsDetectionBuilder builder for the notifications detection part of the pipeline
     */
    @Autowired
    public AnomalyDetectionPipeline(IdAssignmentBuilder idAssignmentBuilder,
                                    ScoreCalculationBuilder scoreCalculationBuilder,
                                    ScoreAggregationBuilder scoreAggregationBuilder,
                                    NotificationsDetectionBuilder notificationsDetectionBuilder) {
        this.idAssignmentBuilder = idAssignmentBuilder;
        this.scoreCalculationBuilder = scoreCalculationBuilder;
        this.scoreAggregationBuilder = scoreAggregationBuilder;
        this.notificationsDetectionBuilder = notificationsDetectionBuilder;

        // For now, hardcode some flink properties. This will be completely redone in the next MR
        // (we will be connecting to a remote cluster)
        Configuration config = new Configuration();
        config.setString("taskmanager.memory.fraction", "0.3");
        config.setString("taskmanager.memory.network.max", "300 mb");
        config.setString("pipeline.default-kryo-serializers",
                "class:java.time.OffsetDateTime,serializer:" + OffsetDateTimeSerializer.class.getName());

        this.flinkEnv = StreamExecutionEnvironment.createLocalEnvironment(config);

        buildPipeline();
    }

    /**
     * An overloaded constructor (same as above) that allows to inject a custom
     * FlinkEnv. Used for testing purposes
     *
     * @param idAssignmentBuilder builder for the id assignment part of the pipeline
     * @param scoreCalculationBuilder builder for the score calculation part of the pipeline
     * @param scoreAggregationBuilder builder for the score aggregation part of the pipeline
     * @param notificationsDetectionBuilder builder for the notifications detection part of the pipeline
     * @param flinkEnv injected Flink environment
     */
    public AnomalyDetectionPipeline(
            IdAssignmentBuilder idAssignmentBuilder,
            ScoreCalculationBuilder scoreCalculationBuilder,
            ScoreAggregationBuilder scoreAggregationBuilder,
            NotificationsDetectionBuilder notificationsDetectionBuilder,
            StreamExecutionEnvironment flinkEnv
    ) {
        this.idAssignmentBuilder = idAssignmentBuilder;
        this.scoreCalculationBuilder = scoreCalculationBuilder;
        this.scoreAggregationBuilder = scoreAggregationBuilder;
        this.notificationsDetectionBuilder = notificationsDetectionBuilder;
        this.flinkEnv = flinkEnv;
        buildPipeline();
    }

    /**
     * Closes the pipeline by closing KafkaStreams and Flink environment.
     *
     * @throws Exception when closing Flink environment throws exception
     */
    public void closePipeline() throws Exception {
        flinkEnv.close();
    }

    /**
     * Private helper method for building the pipeline step by step.
     */
    private void buildPipeline()  {

        // Build the pipeline part that assigns IDs to incoming AIS signals (Flink)
        DataStream<AISSignal> streamWithAssignedIds = idAssignmentBuilder.buildIdAssignmentPart(flinkEnv);

        // Build the pipeline part that calculates the anomaly scores (Flink)
        DataStream<AnomalyInformation> streamOfAnomalyInfo =
                scoreCalculationBuilder.buildScoreCalculationPart(streamWithAssignedIds);

        // Build the pipeline part that creates a stream of CurrentShipDetails. This stream is consumed
        // by the next part of the pipeline - ship information extractor
        DataStream<CurrentShipDetails> aggregatedStream =
                scoreAggregationBuilder.buildScoreAggregationPart(streamWithAssignedIds, streamOfAnomalyInfo);

        // Build the pipeline part that produces notifications (Kafka Streams)
        notificationsDetectionBuilder.buildNotifications(aggregatedStream);
    }


    /**
     * Starts the stream processing: both Flink and Kafka parts.
     * Note that this method is not blocking, and it will not be called by the constructor.
     */
    public void runPipeline() {
        try {
            this.flinkEnv.executeAsync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
