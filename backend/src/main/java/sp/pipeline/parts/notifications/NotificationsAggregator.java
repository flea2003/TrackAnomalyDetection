package sp.pipeline.parts.notifications;

import lombok.Getter;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.util.Collector;
import org.springframework.stereotype.Service;
import sp.model.CurrentShipDetails;
import sp.model.Notification;


@Service
@Getter
public class NotificationsAggregator extends RichFlatMapFunction<CurrentShipDetails, Notification> {

    private final int notificationThreshold = 30;
    private ValueState<Notification> previousNotificationState;

    /**
     * Method that is responsible for aggregating the CurrentShipDetails object signals, and creating notifications,
     * out of them, in case they need to be sent. The logic of when a notification should be sent is the following:
     * 1. If the anomaly score was below a threshold, and now got above threshold, a notification should be added
     * 2. In all other scenarios, notification should not be sent.
     * However, we want to keep track of the state of the current anomaly score (whether it is below or above threshold)
     * as it is the only way to decide whether a notification should be sent.
     * Also, note that the logic when a notification should be sent will be improved in the future to account for TYPE
     * of the anomalies.
     *
     * @param newShipDetails value of the CurrentShipDetails object that just arrived
     * @param out a collector for collecting the results of the function. It is used to emit the notifications
     */
    @Override
    public void flatMap(CurrentShipDetails newShipDetails, Collector<Notification> out) throws Exception {
        // If the current ship details does not have any anomaly information, it is useless for notification calculation,
        // so skip it and wait until at some point, anomaly information comes
        if (newShipDetails.getCurrentAnomalyInformation() == null)
            return;

        if (previousNotificationState.value() == null) {
            previousNotificationState.update(new Notification(newShipDetails));
            return;
        }

        // Retrieve current ship details from the previous notification
        CurrentShipDetails previousShipDetails = previousNotificationState.value().getCurrentShipDetails();

        // Extract previous and new anomaly scores
        float previousScore = previousShipDetails.getCurrentAnomalyInformation().getScore();
        float newScore = newShipDetails.getCurrentAnomalyInformation().getScore();
        boolean previousScoreWasHigh = previousScore >= notificationThreshold;
        boolean newScoreIsHigh = newScore >= notificationThreshold;

        // Information that will be returned as the updated result for the state
        CurrentShipDetails resultingInformation;

        // In case the score has changed to be below the threshold, we should update the state
        if (previousScoreWasHigh && !newScoreIsHigh) {
            previousNotificationState.update(new Notification(newShipDetails));
        }

        // In case the score has changed to now be above the threshold, we should update the state and send a notification
        if (!previousScoreWasHigh && newScoreIsHigh) {
            Notification newNotification = new Notification(newShipDetails);
            previousNotificationState.update(newNotification);
            out.collect(newNotification);
        }
        // Else, if the score did not change with respect to the threshold, we should just do nothing
    }

    /**
     * Set up the Flink state of this map function.
     *
     * @param openContext The context containing information about the context in which the function
     *     is opened.
     * @throws Exception in case setting up the state fails
     */
    @Override
    public void open(OpenContext openContext) throws Exception {
        previousNotificationState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("previousNotification", Notification.class)
        );
    }

}

