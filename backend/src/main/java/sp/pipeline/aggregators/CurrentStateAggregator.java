package sp.pipeline.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.model.MaxAnomalyScoreDetails;
import sp.model.ShipInformation;
import java.time.OffsetDateTime;

@Service
public class CurrentStateAggregator {

    /**
     * Aggregates data to a resulting map.
     *
     * @param aggregatedShipDetails object that stores the latest received data for a ship
     * @param valueJson json value for a signal
     * @return updated object that stores all needed data for a ship
     */
    public CurrentShipDetails aggregateSignals(CurrentShipDetails aggregatedShipDetails, String valueJson)
            throws JsonProcessingException {

        ShipInformation shipInformation = ShipInformation.fromJson(valueJson);
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
     * @return - the updated MaxAnomalyScoreDetails instance
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
}
