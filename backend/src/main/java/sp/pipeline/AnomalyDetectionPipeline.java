package sp.pipeline;

import lombok.Getter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.pipeline.parts.aggregation.ScoreAggregationBuilder;
import sp.pipeline.parts.extractors.ShipInformationExtractor;
import sp.pipeline.parts.identification.IdAssignmentBuilder;
import sp.pipeline.parts.notifications.NotificationsDetectionBuilder;
import sp.pipeline.parts.scoring.ScoreCalculationBuilder;
import sp.pipeline.utils.StreamUtils;

@Service
public class AnomalyDetectionPipeline {
    private final StreamUtils streamUtils;
    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<Long, CurrentShipDetails> state;
    @Getter
    private ShipInformationExtractor shipInformationExtractor;
    private final IdAssignmentBuilder idAssignmentBuilder;
    private final ScoreCalculationBuilder scoreCalculationBuilder;
    private final ScoreAggregationBuilder scoreAggregationBuilder;
    private final NotificationsDetectionBuilder notificationsDetectionBuilder;

    /**
     * Constructor for the AnomalyDetectionPipeline class.
     *
     * @param streamUtils utility class for setting up streams
     * @param idAssignmentBuilder builder for the id assignment part of the pipeline
     * @param scoreCalculationBuilder builder for the score calculation part of the pipeline
     * @param scoreAggregationBuilder builder for the score aggregation part of the pipeline
     * @param notificationsDetectionBuilder builder for the notifications detection part of the pipeline
     */
    @Autowired
    public AnomalyDetectionPipeline(StreamUtils streamUtils,
                                    IdAssignmentBuilder idAssignmentBuilder,
                                    ScoreCalculationBuilder scoreCalculationBuilder,
                                    ScoreAggregationBuilder scoreAggregationBuilder,
                                    NotificationsDetectionBuilder notificationsDetectionBuilder) {
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
     * Private helper method for building the pipeline step by step.
     */
    private void buildPipeline()  {

        this.flinkEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        // Build the pipeline part that assigns IDs to incoming AIS signals (Flink)
        DataStream<AISSignal> streamWithAssignedIds = idAssignmentBuilder.buildIdAssignmentPart(flinkEnv);

        // Build the pipeline part that calculates the anomaly scores (Flink)
        scoreCalculationBuilder.buildScoreCalculationPart(streamWithAssignedIds);

        StreamsBuilder builder = new StreamsBuilder();

        // Build the pipeline part that aggregates the scores (Kafka Streams)
        this.state = scoreAggregationBuilder.buildScoreAggregationPart(builder);

        // Build the pipeline part that produces notifications (Kafka Streams)
        notificationsDetectionBuilder.buildNotifications(this.state);

        // Build the Kafka part of the pipeline
        builder.build();

        this.kafkaStreams = streamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();

        this.shipInformationExtractor = new ShipInformationExtractor(state, kafkaStreams);
    }


    /**
     * Starts the stream processing: both Flink and Kafka parts.
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
}
