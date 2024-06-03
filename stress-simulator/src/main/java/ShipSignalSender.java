import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.time.OffsetDateTime;

public class ShipSignalSender {
    private final KafkaProducer<String, String> producer;
    private final String topicName;
    private final int index;
    private final int minIntervalBetweenSignals;
    private final int maxIntervalBetweenSignals;
    private AISSignal currentSignal;
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
        initializeSignal();
    }

    /**
     * Initializes the current ship position/signal with random values.
     */
    void initializeSignal() {
        float longitude = (float) (Math.random() * 360 - 180);
        float latitude = (float) (Math.random() * 180 - 90);
        float speed = (float) (Math.random() * 30);
        float course = (float) (Math.random() * 360);
        float heading = (float) (Math.random() * 360);
        currentSignal = new AISSignal(
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
        timestampForNewSignal = OffsetDateTime.now();
    }


    /**
     * Slightly updates the last signal with random values.
     */
    void randomlyUpdateSignal() {
        float longitude = currentSignal.getLongitude() + (float) (Math.random() * 0.1 - 0.05);
        float latitude = currentSignal.getLatitude() + (float) (Math.random() * 0.1 - 0.05);
        float speed = currentSignal.getSpeed() + (float) (Math.random() * 2 - 1);
        float course = currentSignal.getCourse() + (float) (Math.random() * 10 - 5);
        float heading = currentSignal.getHeading() + (float) (Math.random() * 10 - 5);
        currentSignal = new AISSignal(
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
    }

    /**
     * Method is called every ~500ms. Checks if it is time to send a signal.
     * If it is - sends a signal, updates the current position to something new
     * and updates the timestamp for the next signal to be sent.
     *
     * @throws JsonProcessingException in case converting to JSON fails
     */
    public void step() throws JsonProcessingException {
        if (timestampForNewSignal.isBefore(OffsetDateTime.now())) {

            randomlyUpdateSignal();
            producer.send(new ProducerRecord<>(topicName, currentSignal.toJson()));

            // Update the time for sending the next signal
            timestampForNewSignal = OffsetDateTime.now().plusSeconds(
                    minIntervalBetweenSignals + (int) (Math.random() * (maxIntervalBetweenSignals - minIntervalBetweenSignals))
            );
        }
    }
}
