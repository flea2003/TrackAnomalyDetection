package sp.pipeline.parts.notifications;

import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.springframework.stereotype.Component;
import sp.model.CurrentShipDetails;
import sp.model.Notification;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.utils.StreamUtils;

@Component
public class NotificationsDetectionBuilder {
    private final NotificationsAggregator notificationsAggregator;
    private final StreamUtils streamUtils;
    private final PipelineConfiguration configuration;

    /**
     * Constructor for the NotificationsDetectionBuilder class.
     *
     * @param notificationsAggregator an injected object that handles the logic of creating and storing notifications
     * @param streamUtils an injected object that helps with the stream operations
     * @param configuration an object that holds configuration properties
     */
    public NotificationsDetectionBuilder(NotificationsAggregator notificationsAggregator,
                                         StreamUtils streamUtils,
                                         PipelineConfiguration configuration) {
        this.notificationsAggregator = notificationsAggregator;
        this.streamUtils = streamUtils;
        this.configuration = configuration;
    }

    /**
     * Notification pipeline building part. The idea is the following: All processing starts from the stream
     * of the aggregated CurrentShipDetails. Then, the stateful mapping part is responsible for the
     * logic of computing when a new notification should be created, and once it has to be created adding it to the stream.
     * It then also (internally) updates the most recent notification that is stored for that particular ship.
     * Note that to decide whether a new notification for a particular ship should be created, it
     * is enough to have the information of the most recent notification for that ship, and a new AnomalyInformation
     * signal (which in our case is wrapped in CurrentShipDetails for optimization purposes for retrieving AIS signal).
     * All notifications are forwarded to a Kafka topic for storage and also querying by the web part of the backend.
     *
     * @param streamOfUpdates a stream that contains the state updates, i.e., the aggregated CurrentShipDetails
     */
    public void buildNotifications(DataStream<CurrentShipDetails> streamOfUpdates) {
        // Construct the KTable (state that is stored) by aggregating the merged stream
        DataStream<Notification> notificationStream = streamOfUpdates
                .keyBy(CurrentShipDetails::extractId)
                .flatMap(notificationsAggregator);

        // Sink the notifications to a Kafka topic
        KafkaSink<Notification> kafkaSink = streamUtils.createSinkFlinkToKafka(
                configuration.getNotificationsTopicName()
        );
        notificationStream.sinkTo(kafkaSink);
    }
}
