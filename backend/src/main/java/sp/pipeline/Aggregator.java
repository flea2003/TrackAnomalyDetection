package sp.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import sp.dtos.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;

import java.util.ArrayList;

@Service
public class Aggregator {

    /**
     * Aggregates data to a resulting map.
     *
     * @param aggregatedShipDetails object that stores all needed data for a ship
     * @param valueJson json value for a signal
     * @param key hash value of the ship
     * @return updated object that stores all needed data for a ship
     */
    public CurrentShipDetails aggregateSignals(CurrentShipDetails aggregatedShipDetails, String valueJson, String key) throws JsonProcessingException {
        System.out.println("Started aggregating JSON value. JSON: " + valueJson);

        // If this is the first signal received, instantiate the past information as an empty list
        if (aggregatedShipDetails.getPastInformation() == null)
            aggregatedShipDetails.setPastInformation(new ArrayList<>());

        ShipInformation shipInformation = ShipInformation.fromJson(valueJson);
        AnomalyInformation anomalyInformation = shipInformation.getAnomalyInformation();

        // If the signal is AIS signal, add it to past information
        if (shipInformation.getAnomalyInformation() == null) {
            aggregatedShipDetails.getPastInformation().add(shipInformation);
        } else if (shipInformation.getAisSignal() == null) {
            // If the signal is Anomaly Information signal, attach it to a corresponding AIS signal

            // Set the anomaly information to be the most recent one
            // TODO: take care of proper format for the date
            // TODO: CONSIDER ANOMALY INFO ARRIVING EARLIER THAN AIS SIGNAL
            aggregatedShipDetails.setAnomalyInformation(shipInformation.getAnomalyInformation());

            updateCorrespondingShipInformation(aggregatedShipDetails, anomalyInformation);
        } else throw new RuntimeException("Something went wrong");

        System.out.println("Current ship details after aggregation, for " + key + " ship: " + aggregatedShipDetails);
        return aggregatedShipDetails;
    }

    public void updateCorrespondingShipInformation(CurrentShipDetails aggregatedShipDetails, AnomalyInformation anomalyInformation) {
        // Find the corresponding AISSignal for the AnomalyInformation object, and update the ShipInformation object
        ShipInformation information = findCorrespondingAisSignal(aggregatedShipDetails, anomalyInformation);
        if (information == null) {
            System.out.println("Corresponding AISSignal was not found. " +
                    "Probably update reached the pipeline faster than the initial signal.");
            return;
        }

        information.setAnomalyInformation(anomalyInformation);
        aggregatedShipDetails.setAnomalyInformation(anomalyInformation);
    }

    public ShipInformation findCorrespondingAisSignal(CurrentShipDetails aggregatedShipDetails, AnomalyInformation anomalyInformation) {
        for (int i = aggregatedShipDetails.getPastInformation().size() - 1; i >= 0; i--) {
            ShipInformation information = aggregatedShipDetails.getPastInformation().get(i);
            if (information.getAisSignal().getTimestamp().isEqual(anomalyInformation.getCorrespondingTimestamp())) {
                // Check that there are no problems with the data
                assert information.getAisSignal().getShipHash().equals(anomalyInformation.getShipHash());
                assert information.getShipHash().equals(anomalyInformation.getShipHash());

                return information;
            }
        }

        // Corresponding AISSignal not found
        return null;
    }

}
