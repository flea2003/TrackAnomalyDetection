package sp.pipeline.parts.aggregation.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.exceptions.NotificationNotFoundException;
import sp.model.CurrentShipDetails;
import sp.model.Notification;
import sp.services.NotificationService;

@Service
@Getter
public class NotificationsAggregator {

    private final NotificationService notificationService;
    private final int notificationThreshold;

    /**
     * Constructor for aggregator class.
     *
     * @param notificationService notifications service
     */
    @Autowired
    public NotificationsAggregator(NotificationService notificationService) {
        this.notificationService = notificationService;

        // Could later be added to configurations file
        this.notificationThreshold = 30;
    }

    /**
     * Method that is responsible for aggregating the CurrentShipDetails object signals, and creating and storing new
     * notifications, in case they need to be sent. The logic of when a notification should be sent is the following:
     * 1. If the anomaly score was below a threshold, and now got above threshold, a notification should be added
     * 2. In all other scenarios, notification should not be sent.
     *
     * However, we want to keep track of the state of the current anomaly score (whether it is below or above threshold)
     * as it is the only way to decide whenther a notification should be sent.
     *
     * Also, note that the logic when a notification should be sent will be improved in the future to account for TYPE
     * of the anomalies.
     *
     * @param previousNotification Notification object that depicts the state
     * @param newValueJson JSON value of the CurrentShipDetails object that just arrived
     * @param shipID internal ID of the ship
     * @return Notification object that corresponds to the newest state (in case the anomaly score gone above the
     * threshold or below)
     * @throws JsonProcessingException in case JSON value does not correspond to an AnomalyInformation object
     */
    public Notification aggregateSignals(Notification previousNotification, String newValueJson, Long shipID)
            throws JsonProcessingException {

        // Retrieve current ship details from the previous notification
        CurrentShipDetails previousShipDetails = previousNotification.getCurrentShipDetails();

        // Convert the newly arrived JSON string to the new CurrentShipDetails object
        CurrentShipDetails newShipDetails = CurrentShipDetails.fromJson(newValueJson);

        // Check if the stored previous anomaly object has null fields, which would mean that the backend has just
        // started, and so the most recent notification information should be retrieved
        if (previousShipDetails == null) previousShipDetails = extractFromJPA(newShipDetails, shipID);

        // TODO: in the future, also logic for checking if new TYPES of anomalies emerged will need to be added
        if (previousShipDetails.getCurrentAnomalyInformation().getScore() >= notificationThreshold) {
            // Store the same anomaly object (although it does not matter which is stored currently)
            if (newShipDetails.getCurrentAnomalyInformation().getScore() >= notificationThreshold) {
                newShipDetails = previousShipDetails;
            }
            // Otherwise, if the new anomaly score is lower, in the state we will store the new one.
        } else {
            if (newShipDetails.getCurrentAnomalyInformation().getScore() < notificationThreshold) {
                newShipDetails = previousShipDetails;
            } else {
                // Otherwise, if now the anomaly exceeds the threshold, we need to store it in the database
                // TODO: here also a query to the AIS signals database will have to take place, to retrieve
                //  a corresponding AIS signal
                notificationService.addNotification(new Notification(newShipDetails));
            }
        }
        return new Notification(newShipDetails);
    }

    /**
     * Method that takes care of logic if the server has just restarted.
     *
     * @param newShipDetails CurrentShipDetails object that corresponds to the new update
     * @param shipID ship ID
     * @return updated current ship details for the current notification
     */
    public CurrentShipDetails extractFromJPA(CurrentShipDetails newShipDetails, Long shipID) {
        CurrentShipDetails currentShipDetails;
        try {
            // Fetch the anomaly information that corresponds to the most recently saved notification
            currentShipDetails = notificationService.getNewestNotificationForShip(shipID).getCurrentShipDetails();

        } catch (NotificationNotFoundException e) {
            // If there were no notifications saved (meaning that ship has not yet ever became anomalous), save the current
            // state as the newest anomaly object
            currentShipDetails = newShipDetails;

            if (currentShipDetails.getCurrentAnomalyInformation().getScore() >= notificationThreshold) {
                // If that newest anomaly score exceeds the threshold, add a new notification to the database
                // TODO: here also a query to the AIS signals database will have to take place, to retrieve a
                //  corresponding AIS signal
                notificationService.addNotification(new Notification(newShipDetails));
            }
        }

        return currentShipDetails;
    }
}

