import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import helperobjects.AISSignal;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.OffsetDateTime;
import java.util.Properties;

public class ShipSignalSender {
    private final KafkaProducer<String, String> producer;
    private final String topicName;
    private final int index;
    private final int minIntervalBetweenSignals;
    private final int maxIntervalBetweenSignals;

    public ShipSignalSender(
            String serverName,
            String topicName,
            int index,
            int minIntervalBetweenSignals,
            int maxIntervalBetweenSignals
    ) {
        this.producer = createProducer(serverName);
        this.topicName = topicName;
        this.index = index;
        this.minIntervalBetweenSignals = minIntervalBetweenSignals;
        this.maxIntervalBetweenSignals = maxIntervalBetweenSignals;
    }

    public void start() throws JsonProcessingException {

        float longitude = (float) (Math.random() * 360 - 180);
        float latitude = (float) (Math.random() * 180 - 90);
        float speed = (float) (Math.random() * 30);
        float course = (float) (Math.random() * 360);
        float heading = (float) (Math.random() * 360);

        // Start sending signals
        while (true) {
            // Wait a random amount of time
            try {
                Thread.sleep((long) (Math.random() * (maxIntervalBetweenSignals - minIntervalBetweenSignals) + minIntervalBetweenSignals));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Slightly randomly update the values
            longitude += (float) ((Math.random() - 0.5) * 0.1);
            latitude += (float) ((Math.random() - 0.5) * 0.1);
            speed += (float) ((Math.random() - 0.5) * 1);
            course += (float) ((Math.random() - 0.5) * 0.1);
            heading += (float) ((Math.random() - 0.5) * 0.1);

            // Send a signal
            AISSignal signal = new AISSignal(
                    "stress-simulator",
                    "ship" + index,
                    speed,
                    longitude,
                    latitude,
                    course,
                    heading,
                    OffsetDateTime.now(),
                    "departurePort"
            );
            producer.send(new ProducerRecord<>(topicName, signal.toJson()));
        }
    }

    /**
     * Returns a Kafka producer.
     *
     * @param server name of the server
     * @return Kafka producer with specified configurations
     */
    private static KafkaProducer<String, String> createProducer(String server) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(properties);
    }
}
