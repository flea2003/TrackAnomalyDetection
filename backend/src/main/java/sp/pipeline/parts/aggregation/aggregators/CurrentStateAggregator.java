package sp.pipeline.parts.aggregation.aggregators;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.springframework.stereotype.Component;
import sp.model.*;
import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class CurrentStateAggregator extends RichMapFunction<ShipInformation, CurrentShipDetails> {
    private ValueState<CurrentShipDetails> aggregatedShipDetailsState;

    /**
     * Aggregates data to a resulting map.
     *
     * @param shipInformation object that stores the latest received data for a ship
     * @return an updated current ship details for the ship
     */
    @Override
    public CurrentShipDetails map(ShipInformation shipInformation) throws IOException {

        // Handle initializing the state
        if (aggregatedShipDetailsState.value() == null) {
            aggregatedShipDetailsState.update(new CurrentShipDetails());
        }

        CurrentShipDetails aggregatedShipDetails = aggregatedShipDetailsState.value();

        AnomalyInformation anomalyInformation = shipInformation.getAnomalyInformation();
        AISSignal aisSignal = shipInformation.getAisSignal();

        // If the processed ShipInformation instance encapsulates a AISSignal instance:
        // update the current value of the AISSignal field
        if (encapsulatesAISSignal(aisSignal, aggregatedShipDetails)) {
            aggregatedShipDetails.setCurrentAISSignal(aisSignal);
        }

        // If the processed ShipInformation instance encapsulates a AnomalyInformation instance:
        // update the current value of the AnomalyInformation field, additionally modifying the value
        // of the highest recorder Anomaly Score for the ship
        if (encapsulatesAnomalyInformation(anomalyInformation, aggregatedShipDetails)) {
            aggregatedShipDetails.setCurrentAnomalyInformation(anomalyInformation);

            // Update the value of the maxAnomalyScoreInfo field
            MaxAnomalyScoreDetails updatedMaxAnomalyScoreDetails = updateMaxScoreDetails(aggregatedShipDetails,
                    anomalyInformation);

            aggregatedShipDetails.setMaxAnomalyScoreInfo(updatedMaxAnomalyScoreDetails);
        }

        return aggregatedShipDetails;
    }

    /**
     * Utility method for updating the maxAnomalyScoreInfo filed of the streams aggregating object.
     *
     * @param aggregatedShipDetails an object that stores the current state of the ship
     * @param anomalyInformation the new AnomalyInformation signal for updating the max score
     * @return the updated MaxAnomalyScoreDetails instance
     */
    private MaxAnomalyScoreDetails updateMaxScoreDetails(CurrentShipDetails aggregatedShipDetails,
                                                         AnomalyInformation anomalyInformation) {
        // Given that we received a new AnomalyInformation signal we have to update
        // the MaxAnomalyScoreDetails field
        // If the field maxAnomalyScoreInfo of the aggregating object is not initialized:
        // consider the value of the highest recorded score to be 0
        // consider the value of the corresponding timestamp to be null
        boolean isMaxScoreUninitialized = aggregatedShipDetails.getMaxAnomalyScoreInfo() == null;

        MaxAnomalyScoreDetails currentMaxInfo = isMaxScoreUninitialized
                ? new MaxAnomalyScoreDetails(0F, null)
                : aggregatedShipDetails.getMaxAnomalyScoreInfo();

        float newMaxScore = Math.max(currentMaxInfo.getMaxAnomalyScore(),
                anomalyInformation.getScore());

        OffsetDateTime newTimestamp = newMaxScore == anomalyInformation.getScore()
                ? anomalyInformation.getCorrespondingTimestamp()
                : currentMaxInfo.getCorrespondingTimestamp();

        return new MaxAnomalyScoreDetails(newMaxScore, newTimestamp);
    }

    /**
     * Check if the current aggregated ship details object has not yet been fully initialized,
     * i.e., if either no AISSignal or AnomalyInformation has been set yet.
     *
     * @param aggregatedShipDetails current ship details.
     * @return true if the current details are NOT finalized, and false otherwise.
     */
    public boolean shipDetailsNotInitialized(CurrentShipDetails aggregatedShipDetails) {
        return aggregatedShipDetails.getCurrentAISSignal() == null
                || aggregatedShipDetails.getCurrentAnomalyInformation() == null;
    }

    /**
     * Checks if the processed ShipInformation instance encapsulates a AISSignal instance,
     * in order for aggregator to update aggregated details.
     *
     * @param aisSignal new signal
     * @param aggregatedShipDetails current aggregated ship details
     * @return true if aggregatedShipDetails need to be updated based on aisSignal, and false
     *     otherwise.
     */
    public boolean encapsulatesAISSignal(AISSignal aisSignal, CurrentShipDetails aggregatedShipDetails) {
        if (aisSignal == null) {
            return false;
        }

        if (shipDetailsNotInitialized(aggregatedShipDetails)) {
            return true;
        }

        return aisSignal.getTimestamp()
                .isAfter(aggregatedShipDetails.getCurrentAISSignal().getTimestamp());
    }

    /**
     * Checks if the processed ShipInformation instance encapsulates a AnomalyInformation instance,
     * in order to update the current aggregated details.
     *
     * @param anomalyInformation the new anomaly information
     * @param aggregatedShipDetails the current aggregated ship details
     * @return true if current details need to be updated with anomalyInformation, and false otherwise.
     */
    public boolean encapsulatesAnomalyInformation(
            AnomalyInformation anomalyInformation, CurrentShipDetails aggregatedShipDetails) {
        if (anomalyInformation == null) {
            return false;
        }

        if (shipDetailsNotInitialized(aggregatedShipDetails)) {
            return true;
        }

        return anomalyInformation.getCorrespondingTimestamp()
                .isAfter(aggregatedShipDetails.getCurrentAnomalyInformation().getCorrespondingTimestamp());
    }

    /**
     * Set up the Flink state.
     *
     * @param openContext The context containing information about the context in which the function
     *     is opened.
     * @throws Exception in case setting up the state fails
     */
    @Override
    public void open(OpenContext openContext) throws Exception {
        super.open(openContext);

        // Get the state
        aggregatedShipDetailsState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("aggregatedShipDetailsState", CurrentShipDetails.class));
    }
}
