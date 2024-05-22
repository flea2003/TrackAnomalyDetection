package sp.pipeline.parts.aggregation.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.exceptions.NotFoundNotificationException;
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
     * Method that is responsible for aggregating the AnomalyInformation signals, and creating and storing new
     * notifications.
     *
     * @param currentNotification most recently created notification
     * @param newValueJson JSON value of the AnomalyInformation object that just arrived
     * @param key hash of the ship
     * @return anomaly information that corresponds to the newest notification (so either the new anomaly information,
     *      or the old one)
     * @throws JsonProcessingException in case JSON value does not correspond to an AnomalyInformation object
     */
    public Notification aggregateSignals(Notification currentNotification, String newValueJson, Long key)
            throws JsonProcessingException {

        // Retrieve current ship details from the current notification
        CurrentShipDetails currentShipDetails = currentNotification.getCurrentShipDetails();

        // Convert the JSON string to the new CurrentShipDetails object
        CurrentShipDetails newShipDetails = CurrentShipDetails.fromJson(newValueJson);

        // Check if the stored current anomaly object has null fields (meaning that the backend has restarted!)
        if (currentShipDetails == null) currentShipDetails = extractFromJPA(newShipDetails, key);

        // TODO: in the future, also logic for checking if new TYPES of anomalies emerged will need to be added
        if (currentShipDetails.getCurrentAnomalyInformation().getScore() >= notificationThreshold) {
            // Store the same anomaly object (although it does not matter which is stored currently)
            if (newShipDetails.getCurrentAnomalyInformation().getScore() >= notificationThreshold) {
                newShipDetails = currentShipDetails;
            }
            // Otherwise, if the new anomaly score is lower, in the state we will store the new one.
        } else {
            if (newShipDetails.getCurrentAnomalyInformation().getScore() < notificationThreshold) {
                newShipDetails = currentShipDetails;
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
     * @param key ship ID
     * @return updated current ship details for the current notification
     */
    public CurrentShipDetails extractFromJPA(CurrentShipDetails newShipDetails, Long key) {
        CurrentShipDetails currentShipDetails;
        try {
            // Fetch the anomaly information that corresponds to the most recently saved notification
            currentShipDetails = notificationService.getNewestNotificationForShip(key).getCurrentShipDetails();

        } catch (NotFoundNotificationException e) {
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
