package sp.pipeline.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotFoundNotificationException;
import sp.services.NotificationService;

@Service
@Getter
public class NotificationsAggregator {

    private final NotificationService notificationService;
    private final int notificationThreshold;

    /**
     * Constructor for aggregator class
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
     * @param currentAnomaly AnomalyInformation object that corresponds to the most recently created notification
     * @param valueJson JSON value of the AnomalyInformation object that just arrived
     * @param key hash of the ship
     * @return anomaly information that corresponds to the newest notification (so either the new anomaly information,
     *      or the old one)
     * @throws JsonProcessingException in case JSON value does not correspond to an AnomalyInformation object
     */
    public AnomalyInformation aggregateSignals(AnomalyInformation currentAnomaly, String valueJson, Long key)
            throws JsonProcessingException {
        // Convert the new anomaly information object from JSON TODO: perhaps add the threshold to some conf file!
        AnomalyInformation newAnomaly = AnomalyInformation.fromJson(valueJson);
        // Check if the stored current anomaly object has null fields (meaning that the backend has restarted!)
        if (currentAnomaly.getCorrespondingTimestamp() == null) {
            try {
                // Fetch the anomaly information that corresponds to the most recently saved notification
                currentAnomaly = notificationService.getNewestNotificationForShip(key).getAnomalyInformation();
            } catch (NotFoundNotificationException e) {
                // If there were no notifications saved (meaning that ship never became anomalous), save the current
                // state as the newest anomaly object
                currentAnomaly = newAnomaly;
                if (currentAnomaly.getScore() >= notificationThreshold) {
                    // If that newest anomaly score exceeds the threshold, add a new notification to the database
                    // TODO: here also a query to the AIS signals database will have to take place, to retrieve a
                    //  corresponding AIS signal
                    notificationService.addNotification(newAnomaly);
                }
            }
        }
        // If the current anomaly score exceeds the threshold, then we will for
        // TODO: in the future, also logic for checking if new TYPES of anomalies emerged will need to be added
        if (currentAnomaly.getScore() >= notificationThreshold) {
            // Store the same anomaly object (although it does not matter which is stored currently)
            if (newAnomaly.getScore() >= notificationThreshold) {
                newAnomaly = currentAnomaly;
            }
            // Otherwise, if the new anomaly score is lower, in the state we will store the new one.
        } else {
            if (newAnomaly.getScore() < notificationThreshold) {
                newAnomaly = currentAnomaly;
            } else {
                // Otherwise, if now the anomaly exceeds the threshold, we need to store it in the database
                // TODO: here also a query to the AIS signals database will have to take place, to retrieve
                //  a corresponding AIS signal
                notificationService.addNotification(newAnomaly);
            }
        }
        return newAnomaly;
    }
}
