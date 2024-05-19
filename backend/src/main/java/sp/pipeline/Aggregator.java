package sp.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import sp.dtos.AnomalyInformation;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;

@Service
public class Aggregator {

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

        // Boolean to check if the current aggregated ship details object has not yet been fully initialized
        // i.e., if either no AISSignal or AnomalyInformation has been set yet
        boolean shipDetailsNotInitialized = aggregatedShipDetails.getCurrentAISSignal() == null
                || aggregatedShipDetails.getCurrentAnomalyInformation() == null;

        // If the processed ShipInformation instance encapsulates a AISSignal instance:
        // update the current value of the AISSignal field
        if (aisSignal != null && (shipDetailsNotInitialized
                || aisSignal.getTimestamp().isAfter(aggregatedShipDetails.getCurrentAISSignal().getTimestamp()))
        ) {
            aggregatedShipDetails.setCurrentAISSignal(aisSignal);
        }

        // If the processed ShipInformation instance encapsulates a AnomalyInformation instance:
        // update the current value of the AnomalyInformation field
        if (anomalyInformation != null
                && (shipDetailsNotInitialized || anomalyInformation.getCorrespondingTimestamp()
                .isAfter(aggregatedShipDetails.getCurrentAnomalyInformation().getCorrespondingTimestamp()))
        ) {
            aggregatedShipDetails.setCurrentAnomalyInformation(anomalyInformation);
        }

        return aggregatedShipDetails;
    }

}
