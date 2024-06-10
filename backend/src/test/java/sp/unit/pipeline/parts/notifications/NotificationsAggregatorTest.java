package sp.unit.pipeline.parts.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.exceptions.NotificationNotFoundException;
import sp.model.*;
import sp.pipeline.parts.aggregation.aggregators.CurrentStateAggregator;
import sp.pipeline.parts.notifications.NotificationsAggregator;
import sp.pipeline.utils.OffsetDateTimeSerializer;
import sp.services.NotificationService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class NotificationsAggregatorTest {

    private NotificationService notificationService;

    private NotificationsAggregator notificationsAggregator;
    private CurrentShipDetails valueLow = new CurrentShipDetails(new AnomalyInformation(10, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails());
    private CurrentShipDetails valueHigh = new CurrentShipDetails(new AnomalyInformation(50, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails());
    private CurrentShipDetails valueHighest = new CurrentShipDetails(new AnomalyInformation(51, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails());

    private String key = "1";

    private Notification initialValue = new Notification(null, null, false, null);
    private Notification oldValueLow = new Notification(new CurrentShipDetails(new AnomalyInformation(10, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails()));
    private Notification oldValueHigh = new Notification(new CurrentShipDetails(new AnomalyInformation(50, "explanation", OffsetDateTime.of(2004, 01, 27, 1, 1, 0, 0, ZoneOffset.ofHours(0)), 1L), new AISSignal(), new MaxAnomalyScoreDetails()));

    private static class CollectSink implements SinkFunction<Notification> {
        public static final List<Notification> notificationsList = new ArrayList<>();
        @Override
        public synchronized void invoke(Notification details, Context context) {
            notificationsList.add(details);
        }
    }

    /**
     * Sends the provided messages to the mapping function and returns the last element of the returned stream.
     *
     * @param detailsToSend the messages to send to the mapping function
     * @return the last element of the returned stream
     * @throws Exception if the mapping function fails
     */
    List<Notification> runTheMapping(List<CurrentShipDetails> detailsToSend) throws Exception {
        // Set up a simple flink environment
        Configuration config = new Configuration();
        config.setString("pipeline.default-kryo-serializers",
                "class:java.time.OffsetDateTime,serializer:" + OffsetDateTimeSerializer.class.getName());
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);

        DataStream<CurrentShipDetails> detailsStream = env.fromData(detailsToSend);

        DataStream<Notification> resultStream = detailsStream.keyBy(CurrentShipDetails::extractId).flatMap(
                new NotificationsAggregator()
        );
        CollectSink.notificationsList.clear();
        resultStream.addSink(new CollectSink());
        env.execute();
        env.close();

        return CollectSink.notificationsList;
    }

    /**
     * All low values, no notifications should be sent.
     */
    @Test
    void testLowLowLow() throws Exception {
        List<Notification> result = runTheMapping(List.of(valueLow, valueLow, valueLow));
        assertThat(result.size()).isEqualTo(0);
    }

    /**
     * Anomaly scores: 10, 50, 10
     * If above threshold and then below threshold, a notification for the high one should be sent.
     */
    @Test
    void testLowHighLow() throws Exception {
        List<Notification> result = runTheMapping(List.of(valueLow, valueHigh, valueLow));
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCurrentShipDetails()).isEqualTo(valueHigh);
    }

    /**
     * Anomaly scores: 10, 50, 51. Notification should be sent for the 50 only
     */
    @Test
    void testLowHighestHigh() throws Exception {
        List<Notification> result = runTheMapping(List.of(valueLow, valueHighest, valueHigh));
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCurrentShipDetails()).isEqualTo(valueHighest);
    }

    /**
     * Anomaly scores: 10, 50, 10, 50. Notification should be sent for the two 50s
     */
    @Test
    void testLowHighLowHigh() throws Exception {
        List<Notification> result = runTheMapping(List.of(valueLow, valueHigh, valueLow, valueHigh));
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getCurrentShipDetails()).isEqualTo(valueHigh);
        assertThat(result.get(1).getCurrentShipDetails()).isEqualTo(valueHigh);
    }
}

