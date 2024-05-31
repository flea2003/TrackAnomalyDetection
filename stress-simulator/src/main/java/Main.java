import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Main {

    /**
     * Main method.
     *
     * @param args arguments
     * @throws InterruptedException exception
     * @throws IOException exception
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        // Example of creating a simulator

        String topicName = "ships-raw-AIS";
        String serverName = "localhost:9092";

        // On average, this is 5k signals per second!
//        int shipCount = 30_000;
        int shipCount = 300;
        int minIntervalBetweenMessages = 3;
        int maxIntervalBetweenMessages = 9;

        // Spawn shipCount tasks
        ExecutorService executor = Executors.newFixedThreadPool(30);

        for (int i = 0; i < shipCount; i++) {
            final int savedI = i;
            executor.submit(() -> {
                ShipSignalSender sender = new ShipSignalSender(
                        serverName,
                        topicName,
                        savedI,
                        minIntervalBetweenMessages,
                        maxIntervalBetweenMessages
                );

                try {
                    sender.start();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}