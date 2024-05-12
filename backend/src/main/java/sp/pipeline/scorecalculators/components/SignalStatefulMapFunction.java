package sp.pipeline.scoreCalculators.components;

import org.apache.flink.api.common.functions.RichMapFunction;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;

public class SignalStatefulMapFunction extends RichMapFunction<AISSignal, AnomalyInformation> {
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        return null;
    }
}
