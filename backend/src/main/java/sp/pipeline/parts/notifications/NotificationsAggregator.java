package sp.pipeline.parts.notifications;

import lombok.Getter;
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
     * However, we want to keep track of the state of the current anomaly score (whether it is below or above threshold)
     * as it is the only way to decide whether a notification should be sent.
     * Also, note that the logic when a notification should be sent will be improved in the future to account for TYPE
     * of the anomalies.
     *
     * @param previousNotification Notification object that depicts the state
     * @param newShipDetails value of the CurrentShipDetails object that just arrived
     * @param shipID ship ID
     * @return Notification object that corresponds to the newest state (in case the anomaly score gone above the
     *      threshold or below)
     */
    public Notification aggregateSignals(Notification previousNotification, CurrentShipDetails newShipDetails, Long shipID) {
        // Information that will be returned as the updated result for the state
        CurrentShipDetails resultingInformation;

        // Retrieve current ship details from the previous notification
        CurrentShipDetails previousShipDetails = previousNotification.getCurrentShipDetails();

        // Convert the newly arrived JSON string to the new CurrentShipDetails object
        // Check if the stored previous anomaly object has null fields, which would mean that the backend has just
        // started, and so the most recent notification information should be retrieved
        if (previousShipDetails == null) previousShipDetails = extractFromJPA(newShipDetails, shipID);

        // Extract previous and new anomaly scores to ease up the readability
        float previousScore = previousShipDetails.getCurrentAnomalyInformation().getScore();
        float newScore = newShipDetails.getCurrentAnomalyInformation().getScore();

        // Check if the score of the previously stored notification is above the threshold.
        if (previousScore >= notificationThreshold) {

            // If the previously stored score is above the threshold, and also the newly arrived information object
            // score is above the threshold, then return the old information as the updated state
            if (newScore >= notificationThreshold) resultingInformation = previousShipDetails;

            // If the previously stored score is above the threshold, and the newly arrived information score is below
            // the threshold, return that new information as the new state
            else  resultingInformation = newShipDetails;
        } else {
            // If the previously stored score is below the threshold, and also the newly arrived information object
            // score is below the threshold, then return the old information as the updated state
            if (newScore < notificationThreshold) resultingInformation = previousShipDetails;
            else {
                // If the previously stored score is below the threshold, and the newly arrived information object score is
                // above the threshold, then return the new information as the updated state, and save the new notification in DB
                resultingInformation = newShipDetails;
                notificationService.addNotification(new Notification(newShipDetails));
            }
        }
        // TODO: in the future, also logic for checking if new TYPES of anomalies emerged will need to be added
        return new Notification(resultingInformation);
    }

    /**
     * Method that takes care of dealing with the newly arrived notification object, in case the server has just started
     * since then the current notification object has all attributes as null.
     * In that case, the newest Notification stored in the database is retrieved. If it does not exist, meaning that
     * ship did not have any notifications, then we check if the newly arrived anomaly information score exceeds the
     * threshold: if it does, then we create a new notification and save it in the database. If it does not, we do
     * nothing.
     * Also, if the previous notification existed in the database, we use it to decide whether to create a new
     * notification. This however does not take into consideration that while the server was down, the anomaly score
     * may have gotten below the threshold. This is the only thing that is lacking, however, impossible to solve.
     *
     * @param newShipDetails CurrentShipDetails object that corresponds to the new update
     * @param shipID ship ID
     * @return updated current ship details for the current notification
     */
    public CurrentShipDetails extractFromJPA(CurrentShipDetails newShipDetails, Long shipID) {
        CurrentShipDetails currentShipDetails;
        try {
            // Fetch the current ship details that correspond to the most recently saved notification, and use it for
            // computing whether new notification should be added.
            currentShipDetails = notificationService.getNewestNotificationForShip(shipID).getCurrentShipDetails();

        } catch (NotificationNotFoundException e) {

            // If there were no notifications saved (meaning that ship has not yet ever became anomalous), set the
            // previous state as the newly arrived one
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

