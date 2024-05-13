package sp.pipeline.scorecalculators;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.springframework.stereotype.Component;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.pipeline.scorecalculators.components.SampleStatefulMapFunction;

@Component
public class DefaultScoreCalculator implements ScoreCalculationStrategy {

    /**
     * Simple example of Flink score calculation. Takes as input the AIS signals, keys by ship ID and for each
     * incoming AIS signal per ship, increases the current score (done inside the SampleStatefulMapFunction).
     *
     * @param source the source stream of incoming AIS signals
     * @return a stream of score updates
     */
    @Override
    public DataStream<AnomalyInformation> setupFlinkAnomalyScoreCalculationPart(DataStream<AISSignal> source) {
        return source
                .keyBy(AISSignal::getShipHash)          // Group AIS signals by their ship hash
                .map(new SampleStatefulMapFunction());  // Map each item to an AISUpdate object
    }
}
