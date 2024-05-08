package sp.pipeline.scoreCalculators.components;

import sp.dtos.AnomalyInformation;
import sp.model.AISSignal;
import sp.model.ShipInformation;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;

import java.time.Duration;

public class SampleStatefulMapFunction extends RichMapFunction<AISSignal, ShipInformation> {

    private transient ValueState<Float> score;
    private transient ListState<Float> latitudes;

    /**
     * The method initializes the state.
     * @param config The configuration containing the parameters attached to the contract.
     */
    @Override
    public void open(Configuration config) {

        // Setup the time-to-live for the state (30 minutes)
        StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.fromDuration(Duration.ofMinutes(30)))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();

        // Setup the state descriptors
        ValueStateDescriptor<Float> scoreDescriptor =
                new ValueStateDescriptor<>(
                        "score",
                        TypeInformation.of(new TypeHint<Float>() {})
                );
        ListStateDescriptor<Float> latitudesDescriptor =
                new ListStateDescriptor<>(
                        "latitudes",
                        TypeInformation.of(new TypeHint<Float>() {})
                );

        // Set time to live for both states
        scoreDescriptor.enableTimeToLive(ttlConfig);
        latitudesDescriptor.enableTimeToLive(ttlConfig);

        // Initialize the states and set them to be accessible in the map function
        score = getRuntimeContext().getState(scoreDescriptor);
        latitudes = getRuntimeContext().getListState(latitudesDescriptor);
    }

    /**
     * Performs a stateful map. In this case - maps an incoming AIS signal into an AISUpdate.
     * This map is essentially a counter, which counts to score.
     *
     * @param value The input value.
     * @return an AIS update
     * @throws Exception in case the state cannot be accessed for some reason
     */
    @Override
    public ShipInformation map(AISSignal value) throws Exception {

        // Access the current score for the ship. If it is empty, initialize it to 0
        Float currentScore = score.value();
        if (currentScore == null) {
            currentScore = 0F;
        }

        // Increment the score
        currentScore += 1;
        score.update(currentScore);

        // Add the position information to remember
        latitudes.add(value.getLatitude());

        // Return the calculated score update
        return new ShipInformation(value.getShipHash(), new AnomalyInformation(currentScore, "", "", ""), value);
    }
}
