package sp.pipeline.parts.scoring;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.pipeline.parts.scoring.scorecalculators.ScoreCalculationStrategy;

@Component
public class ScoreCalculationBuilder {
    private final ScoreCalculationStrategy scoreCalculationStrategy;

    /**
     * Constructor for the ScoreCalculationBuilder class.
     *
     * @param scoreCalculationStrategy the strategy for calculating the anomaly scores
     */
    @Autowired
    public ScoreCalculationBuilder(@Qualifier("simpleScoreCalculator")
                                       ScoreCalculationStrategy scoreCalculationStrategy) {
        this.scoreCalculationStrategy = scoreCalculationStrategy;
    }

    /**
     * Builds the first part of the pipeline: the score calculation part, done in Flink. This part
     * consumes AIS signals from a Flink source, calculates the anomaly scores (in Flink) and sends
     * feeds the forward to the next Flink component.
     * Note that this part could also include a branching out, such as also producing the objects into
     * a Kafka topic, for instance, for intermediate integration testing purposes.
     * For that, checkout the Git history of this class (as this was done before).

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     *
     * @param source the source stream of incoming AIS signals
     * @return a stream of anomaly information, derived from the incoming AIS signals
     */
    public DataStream<AnomalyInformation> buildScoreCalculationPart(DataStream<AISSignal> source) {
        return scoreCalculationStrategy.setupFlinkAnomalyScoreCalculationPart(source);
    }
}
