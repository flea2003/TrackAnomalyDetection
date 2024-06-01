import com.fasterxml.jackson.core.JsonProcessingException;
import helperobjects.AISSignal;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.time.OffsetDateTime;

public class ShipSignalSender {
    private final KafkaProducer<String, String> producer;
    private final String topicName;
    private final int index;
    private final int minIntervalBetweenSignals;
    private final int maxIntervalBetweenSignals;


    private float longitude = (float) (Math.random() * 360 - 180);
    private float latitude = (float) (Math.random() * 180 - 90);
    private float speed = (float) (Math.random() * 30);
    private float course = (float) (Math.random() * 360);
    private float heading = (float) (Math.random() * 360);

    // Create a field for the last timestamp
    private OffsetDateTime timestampForNewSignal;

    /**
     * Default constructor for signal sender.
     *
     * @param topicName the topic to produce to
     * @param producer the producer (thread safe)
     * @param index index of the ship
     * @param minIntervalBetweenSignals minimal interval between consecutive signals (in seconds)
     * @param maxIntervalBetweenSignals maximal interval between consecutive signals (in seconds)
     */
    public ShipSignalSender(
            String topicName,
            KafkaProducer<String, String> producer,
            int index,
            int minIntervalBetweenSignals,
            int maxIntervalBetweenSignals
    ) {
        this.topicName = topicName;
        this.producer = producer;
        this.index = index;
        this.minIntervalBetweenSignals = minIntervalBetweenSignals;
        this.maxIntervalBetweenSignals = maxIntervalBetweenSignals;
        timestampForNewSignal = OffsetDateTime.now();
    }

    /**
     * Method is called every ~500ms. Check if it is time to send a signal.
     * If it is sends and sets the time for the next signal.
     *
     * @throws JsonProcessingException in case converting to JSON fails
     */
    public void step() throws JsonProcessingException {
        if (timestampForNewSignal.isBefore(OffsetDateTime.now())) {

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

            // Update the timestamp
            timestampForNewSignal = OffsetDateTime.now().plusSeconds(
                    minIntervalBetweenSignals + (int) (Math.random() * (maxIntervalBetweenSignals - minIntervalBetweenSignals))
            );
        }
    }
}
