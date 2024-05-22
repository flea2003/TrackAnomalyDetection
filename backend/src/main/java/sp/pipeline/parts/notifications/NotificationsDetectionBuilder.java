package sp.pipeline.parts.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.stereotype.Component;
import sp.model.CurrentShipDetails;
import sp.model.Notification;

@Component
public class NotificationsDetectionBuilder {
    private final NotificationsAggregator notificationsAggregator;

    /**
     * Constructor for the NotificationsDetectionBuilder class.
     *
     * @param notificationsAggregator an injected object that handles the logic of creating and storing notifications
     */
    public NotificationsDetectionBuilder(NotificationsAggregator notificationsAggregator) {
        this.notificationsAggregator = notificationsAggregator;
    }

    /**
     * Notification pipeline building part. The idea is the following: there is a database, where all notifications are
     * stored. When backend restarts, a state (which is actually the notifications Kafka table) queries the
     * notificationsService class, and for each ship, its last notification is retrieved as the initial state
     * Notification object. Then, a stream of updates from the KafkaTable which stores the current state is retrieved:
     * it contains a stream of updates that happen in that table. Then, the stateful mapping part is responsible for the
     * logic of computing when a new notification should be created, and once it has to be created, querying the
     * notificationService class, which handles it. It then also updates the most recent notification that is stored for
     * that particular ship. Note that to decide whether a new notification for a particular ship should be created, it
     * is enough to have the information of the most recent notification for that ship, and a new AnomalyInformation
     * signal (which in our case is wrapped in CurrentShipDetails for optimization purposes for retrieving AIS signal).
     *
     */
    public void buildNotifications(KTable<Long, CurrentShipDetails> state) {
        // Construct a stream for computed AnomalyInformation objects
        KStream<Long, CurrentShipDetails> streamOfUpdates = state.toStream();
        /*
        // Use the following code for easier testing (and also comment out the first line in the method)

        KStream<Long, String> firstStream = builder.stream(calculatedScoresTopicName);
        KStream<Long, AnomalyInformation> second = firstStream.mapValues(x -> {try { return AnomalyInformation
        .fromJson(x); } catch (JsonProcessingException e) {  throw new RuntimeException(e); }  });
        KStream<Long, AnomalyInformation> third = second.selectKey((key, value) -> value.getId());
        KStream<Long, CurrentShipDetails> streamOfUpdates = third.mapValues(x -> new CurrentShipDetails(x, null));
        */
        // Construct the KTable (state that is stored) by aggregating the merged stream
        streamOfUpdates
                .mapValues(x -> {
                    try {
                        return x.toJson();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .groupByKey()
                .aggregate(
                        Notification::new,
                        (key, valueJson, lastInformation) -> {
                            try {
                                return notificationsAggregator.aggregateSignals(lastInformation, valueJson, key);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        });
    }
}
