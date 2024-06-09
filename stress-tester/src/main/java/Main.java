import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Main class that creates a specified number of ships and sends signals from them to the Kafka topic.
 */
public class Main {

    private static final String topicName = "ships-raw-AIS";
    private static final String serverName = "localhost:9092";


    // Bounds for the interval between sent signals for ships
    private static final int minIntervalBetweenMessages = 3;
    private static final int maxIntervalBetweenMessages = 7;


    // Main value to customize - defines how many signals are sent per second
    private static final int signalsPerSecond = 100;


    // Number of threads that will perform the sending of signals
    private static final int threadCount = 5;


    /**
     * Main method that creates a specified number of ships and sends signals from them to the Kafka topic.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Calculate the number of ships needed to achieve the required signals-per-second
        int shipCount = signalsPerSecond * (maxIntervalBetweenMessages + minIntervalBetweenMessages) / 2;

        // Create a sender object for each ship. In order to send signals, the step() methods for these
        // sender objects should be called as often as possible. I.e., each call of step() checks if it is
        // time to send a signal for that particular ship and sends it if it is time to do so.
        ShipSignalSender[] senders = createSenderForEveryShip(shipCount);

        // Call step() method for all senders as often as possible
        pollSenders(senders);
    }

    /**
     * Creates an object for each ship which handles sending signals for that particular ship.
     * In order to later use these objects for sending signals, one simply should call the step() method
     * for each object as often as possible. (e.g. in a loop in a separate thread)
     *
     * @param shipCount the number of ships for which to create senders
     * @return an array of ShipSignalSender objects
     */
    private static ShipSignalSender[] createSenderForEveryShip(int shipCount) {
        // Create a producer for each thread
        KafkaProducer<String, String>[] producers = new KafkaProducer[threadCount];
        for (int i = 0; i < threadCount; i++) {
            producers[i] = createProducer();
        }

        // Create a sender for each ships
        ShipSignalSender[] senders = new ShipSignalSender[shipCount];
        for (int i = 0; i < shipCount; i++) {
            senders[i] = new ShipSignalSender(topicName, producers[i % threadCount],
                    i, minIntervalBetweenMessages, maxIntervalBetweenMessages);
        }

        return senders;
    }

    /**
     * Polls the senders often. Creates a thread pool to make sure it is nicely parallelized.
     *
     * @param senders the senders to poll
     */
    private static void pollSenders(ShipSignalSender[] senders) {
        // Create a thread pool that will execute the senders
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            // Each thread is responsible for a subset of ships with indices from lowerIndex to upperIndex
            final int lowerIndex = senders.length / threadCount * i;
            final int upperIndex = Math.min(lowerIndex + senders.length / threadCount, senders.length);

            // Start the thread that will send signals for the subset of ships
            executor.submit(() -> {
                while (true) {
                    Thread.sleep(500);
                    for (int j = lowerIndex; j < upperIndex; j++)
                        senders[j].step();
                }
            });
        }
    }

    /**
     * Returns a Kafka producer to the server defined in the fields of this class.
     *
     * @return Kafka producer with specified configurations
     */
    private static KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverName);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(properties);
    }

}