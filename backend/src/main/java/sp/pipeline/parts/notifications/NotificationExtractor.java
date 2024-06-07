package sp.pipeline.parts.notifications;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sp.model.Notification;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.parts.aggregation.extractors.GenericKafkaExtractor;
import sp.pipeline.utils.StreamUtils;
import sp.pipeline.utils.json.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationExtractor extends GenericKafkaExtractor {

    private ConcurrentHashMap<Long, Notification> state = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(NotificationExtractor.class);

    /**
     * A constructor for notifications extractor. Calls the constructor of the parent class, which
     * starts polling the Kafka topic every 1ms.
     *
     * @param streamUtils utility class for setting up streams
     * @param configuration an object that holds configuration properties
     */
    @Autowired
    public NotificationExtractor(StreamUtils streamUtils, PipelineConfiguration configuration) {
        super(streamUtils, configuration, 1000000 - 1, configuration.getNotificationsTopicName()); // poll every ~1ms
    }

    /**
     * Processes an incoming record from the Kafka topic.
     *
     * @param record the record incoming from Kafka topic
     */
    @Override
    protected void processNewRecord(ConsumerRecord<Long, String> record) {
        Notification newNotification;
        try {
            newNotification = JsonMapper.fromJson(record.value(), Notification.class);
        } catch (Exception e) {
            logger.error("JSON error while processing internal record, so skipping it. Error: ", e);
            return;
        }
        state.put(newNotification.getId(), newNotification);
    }

    /**
     * Returns all notifications from the saved state.
     *
     * @return a list of all notifications
     */
    public List<Notification> getAllNotifications() {
        return new ArrayList<>(state.values());
    }

    /**
     * Returns a particular notification from the saved state. If no such notification exists,
     * returns null.
     *
     * @param id id of the notification
     * @return notification object
     */
    public Notification getNotificationById(Long id) {
        return state.get(id);
    }
}
