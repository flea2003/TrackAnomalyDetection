package pipeline.scoreCalculators;

import model.AISSignal;
import model.AISUpdate;
import org.apache.flink.streaming.api.datastream.DataStream;

public interface ScoreCalculationStategy {
    /**
     * The key method to implement when setting up the pipeline. This method takes as argument a Flink
     * DataStream of AIS signals and is supposed to return a FlinkStream of AISUpdate objects. Everything
     * inside of this method should deal with Flink.
     *
     * @param source the source stream of incoming AIS signals
     * @return a stream of AISUpdate objects
     */
    DataStream<AISUpdate> setupFlinkAnomalyScoreCalculationPart(DataStream<AISSignal> source);
}
