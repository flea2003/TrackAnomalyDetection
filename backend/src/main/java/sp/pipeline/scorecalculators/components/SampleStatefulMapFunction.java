package sp.pipeline.scorecalculators.components;

import java.time.Duration;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import sp.model.AISSignal;
import sp.dtos.AnomalyInformation;


public class SampleStatefulMapFunction extends RichMapFunction<AISSignal, AnomalyInformation> {

    private transient ValueState<Float> score;
    private transient ListState<Float> latitudes;

    /**
     * The method initializes the state.
     *
     * @param config The configuration containing the parameters attached to the contract.
     */
    @Override
    public void open(Configuration config) {

        // Set up the time-to-live for the state (30 minutes)
        StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.fromDuration(Duration.ofMinutes(30)))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();

        // Set up the state descriptors
        ValueStateDescriptor<Float> scoreDescriptor =
                new ValueStateDescriptor<>(
                        "score",
                        TypeInformation.of(new TypeHint<>() {})
                );
        ListStateDescriptor<Float> latitudesDescriptor =
                new ListStateDescriptor<>(
                        "latitudes",
                        TypeInformation.of(new TypeHint<>() {})
                );

        // Set time to live for both states
        scoreDescriptor.enableTimeToLive(ttlConfig);
        latitudesDescriptor.enableTimeToLive(ttlConfig);

        // Initialize the states and set them to be accessible in the map function
        score = getRuntimeContext().getState(scoreDescriptor);
        latitudes = getRuntimeContext().getListState(latitudesDescriptor);
    }

    /**
     * Performs a stateful map. In this case - maps an incoming AIS signal into an Anomaly Information object.
     * This map is essentially a counter, which counts to score.
     *
     * @param value The input value.
     * @return an AIS update
     * @throws Exception in case the state cannot be accessed for some reason
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        // Thread.sleep(4000);
        // Access the current score for the ship. If it is empty, initialize it to 0

        System.out.println("Received the AISSignal in the anomaly computation method. Object: " + value);

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
        return new AnomalyInformation(currentScore, "", -1F, value.getTimestamp(), value.getId());
    }
}