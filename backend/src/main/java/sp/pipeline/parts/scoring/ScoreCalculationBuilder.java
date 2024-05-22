package sp.pipeline.parts.scoring;

import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.StreamUtils;
import sp.pipeline.parts.scoring.scorecalculators.ScoreCalculationStrategy;

@Component
public class ScoreCalculationBuilder {
    private StreamUtils streamUtils;
    private PipelineConfiguration configuration;

    @Autowired
    public ScoreCalculationBuilder(StreamUtils streamUtils, PipelineConfiguration configuration) {
        this.streamUtils = streamUtils;
        this.configuration = configuration;
    }


    /**
     * Builds the first part of the `sp.pipeline` - the score calculation part, done in Flink. This `sp.pipeline`
     * consumes AIS signals from Kafka, calculates the anomaly scores (in Flink) and sends them to back
     * to Kafka into another topic.

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     */
    public void buildScoreCalculationPart(DataStream<AISSignal> source, ScoreCalculationStrategy scoreCalculationStrategy) {

        // Send the id-assigned AISSignal objects to a Kafka topic (to be used later when aggregating the scores)
        KafkaSink<String> signalsSink = streamUtils.createSinkFlinkToKafka(configuration.kafkaServerAddress, configuration.incomingAisTopicName);
        source.map(AISSignal::toJson).sinkTo(signalsSink);

        // Set up the anomaly detection part of the sp.pipeline (happens in Flink)
        DataStream<AnomalyInformation> updateStream = scoreCalculationStrategy.setupFlinkAnomalyScoreCalculationPart(source);

        // Map the computed AnomalyInformation objects to JSON strings
        DataStream<String> updateStreamSerialized = updateStream.map(AnomalyInformation::toJson);

        // Send the calculated AnomalyInformation objects to Kafka
        KafkaSink<String> scoresSink = streamUtils.createSinkFlinkToKafka(configuration.kafkaServerAddress, configuration.calculatedScoresTopicName);
        updateStreamSerialized.sinkTo(scoresSink);
    }
}
