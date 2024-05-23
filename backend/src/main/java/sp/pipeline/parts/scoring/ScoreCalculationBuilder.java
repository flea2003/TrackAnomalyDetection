package sp.pipeline.parts.scoring;

import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.pipeline.utils.json.JsonMapper;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.utils.StreamUtils;
import sp.pipeline.parts.scoring.scorecalculators.ScoreCalculationStrategy;

@Component
public class ScoreCalculationBuilder {
    private final StreamUtils streamUtils;
    private final PipelineConfiguration config;
    private final ScoreCalculationStrategy scoreCalculationStrategy;

    /**
     * Constructor for the ScoreCalculationBuilder class.
     *
     * @param streamUtils utility class for setting up streams
     * @param configuration an object that holds configuration properties
     * @param scoreCalculationStrategy the strategy for calculating the anomaly scores
     */
    @Autowired
    public ScoreCalculationBuilder(StreamUtils streamUtils,
                                   PipelineConfiguration configuration,
                                   @Qualifier("simpleScoreCalculator")
                                       ScoreCalculationStrategy scoreCalculationStrategy) {
        this.streamUtils = streamUtils;
        this.config = configuration;
        this.scoreCalculationStrategy = scoreCalculationStrategy;
    }

    /**
     * Builds the first part of the `sp.pipeline` - the score calculation part, done in Flink. This `sp.pipeline`
     * consumes AIS signals from Kafka, calculates the anomaly scores (in Flink) and sends them to back
     * to Kafka into another topic.

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     *
     * @param source the source stream of incoming AIS signals
     */
    public void buildScoreCalculationPart(DataStream<AISSignal> source) {

        // Set up the anomaly detection part of the sp.pipeline (happens in Flink)
        DataStream<AnomalyInformation> updateStream = scoreCalculationStrategy.setupFlinkAnomalyScoreCalculationPart(source);

        // Map the computed AnomalyInformation objects to JSON strings
        DataStream<String> updateStreamSerialized = updateStream.map(JsonMapper::toJson);

        // Send the calculated AnomalyInformation objects to Kafka
        KafkaSink<String> scoresSink = streamUtils.createSinkFlinkToKafka(config.getKafkaServerAddress(),
                config.getCalculatedScoresTopicName());
        updateStreamSerialized.sinkTo(scoresSink);
    }
}
