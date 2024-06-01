import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        // Example of creating a simulator

        String topicName = "ships-raw-AIS";
        String serverName = "localhost:9092";

        int minIntervalBetweenMessages = 3;
        int maxIntervalBetweenMessages = 7;

        // Main value to customize
        int signalsPerSecond = 2000;

        // Calculated based on average interval between signals and signals/second
        int shipCount = signalsPerSecond * (maxIntervalBetweenMessages + minIntervalBetweenMessages) / 2;

        System.out.print("Ship count is " + shipCount);

        // Thread count
        int threadCount = 5;

        // Create threadCount producers
        KafkaProducer<String, String>[] producers = new KafkaProducer[threadCount];
        for (int i = 0; i < threadCount; i++) {
            producers[i] = createProducer(serverName);
        }

        // Create shipCount senders
        ShipSignalSender[] senders = new ShipSignalSender[shipCount];
        for (int i = 0; i < shipCount; i++) {
            senders[i] = new ShipSignalSender(topicName, producers[i % threadCount],
                    i, minIntervalBetweenMessages, maxIntervalBetweenMessages);
        }

        // Spawn shipCount tasks
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int l = shipCount / threadCount * i;
            final int r = Math.min(l + shipCount / threadCount, shipCount);
            final int savedI = i;
            executor.submit(()  -> {
                int h = 0;
                while (true) {
                    h++;
                    try {
                        Thread.sleep(500);
                        for (int j = l; j < r; j++) {
                            senders[j].step();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
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