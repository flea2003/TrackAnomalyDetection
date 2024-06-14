package sp.pipeline;

import jakarta.annotation.PreDestroy;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.pipeline.parts.aggregation.ScoreAggregationBuilder;
import sp.pipeline.parts.identification.IdAssignmentBuilder;
import sp.pipeline.parts.notifications.NotificationsDetectionBuilder;
import sp.pipeline.parts.scoring.ScoreCalculationBuilder;

@Service
public class AnomalyDetectionPipeline {
    private final StreamExecutionEnvironment flinkEnv;
    private final IdAssignmentBuilder idAssignmentBuilder;
    private final ScoreCalculationBuilder scoreCalculationBuilder;
    private final ScoreAggregationBuilder scoreAggregationBuilder;
    private final NotificationsDetectionBuilder notificationsDetectionBuilder;
    private JobClient flinkJob;

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
    @Autowired
    public AnomalyDetectionPipeline(
            IdAssignmentBuilder idAssignmentBuilder,
            ScoreCalculationBuilder scoreCalculationBuilder,
            ScoreAggregationBuilder scoreAggregationBuilder,
            NotificationsDetectionBuilder notificationsDetectionBuilder,
            @Qualifier("localFlinkEnv")
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
    @PreDestroy
    public void closePipeline() throws Exception {
        flinkJob.cancel();
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
            flinkJob = this.flinkEnv.executeAsync("Anomaly Detection Pipeline");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
