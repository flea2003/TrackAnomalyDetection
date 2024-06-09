package sp.pipeline.parts.notifications;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.stereotype.Component;
import sp.model.CurrentShipDetails;
import sp.model.Notification;
import sp.pipeline.utils.binarization.KafkaSerialization;

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
     * @param state the Kafka table representing the state
     */
    public void buildNotifications(KTable<Long, CurrentShipDetails> state) {
        // Construct a stream for computed AnomalyInformation objects
        KStream<Long, CurrentShipDetails> streamOfUpdates = state.toStream();

        // Construct the KTable (state that is stored) by aggregating the merged stream
        KafkaSerialization.serialize(streamOfUpdates)
                .groupByKey()
                .aggregate(Notification::new,
                        KafkaSerialization.aggregator(notificationsAggregator::aggregateSignals, CurrentShipDetails.class),
                        Materialized
                                .<Long, Notification, KeyValueStore<Bytes, byte[]>>as("dummy")
                                .withValueSerde(Notification.getSerde())
                                .withCachingDisabled()
            );
    }
}
