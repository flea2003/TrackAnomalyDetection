import helperobjects.Simulator;
import helperobjects.Timestamp;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import parsers.DEBSParser;
import parsers.Parser;

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
        String serverName  = "localhost:9092";
        String dataSetName = "DEBS_DATASET_PUBLIC_second.csv";
        Timestamp startTime = new Timestamp(2015, 4, 1, 20, 25);
        Timestamp endTimestamp = new Timestamp(2015, 4, 2, 20, 30);

        Parser parser = new DEBSParser(getReader(dataSetName));
        try (KafkaProducer<String, String> producer = createProducer(serverName)) {
            Simulator simulator = new Simulator(parser, startTime, endTimestamp, topicName, producer);
            simulator.setSpeed(1);

            simulator.startStream();
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

    /**
     * Creates a buffered reader for a given dataset. It assumes that the dataset is located in the
     * streaming_data folder
     *
     * @param fileName the name of the dataset
     * @return reader for the dataset
     * @throws FileNotFoundException throw in case the file is not found
     */
    private static BufferedReader getReader(String fileName) throws FileNotFoundException {
        return new BufferedReader(new FileReader("simulator/streaming_data/" + fileName));
    }
}