import helperObjects.Timestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import parsers.DEBSParser;
import parsers.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Main {
    /**
     * Main method
     *
     * @param args arguments
     * @throws InterruptedException exception
     * @throws IOException exception
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        // Example of creating a simulator

        String topicName = "example-topic";
        String serverName  = "localhost:9092";
        String dataSet = "/Users/justinas/Desktop/codebase/simulator/streaming_data/DEBS_DATASET_PUBLIC_second.csv";
        Timestamp startTime = new Timestamp(2015, 04, 1, 20, 25);
        Timestamp endTimestamp = new Timestamp(2015, 04, 1, 20, 25);
        Parser parser = new DEBSParser(new BufferedReader(new FileReader(dataSet)));
        KafkaProducer<String, String> producer = createProducer(serverName);
        Simulator simulator = new Simulator(parser, startTime, endTimestamp, topicName, producer);
        simulator.setSpeed(60);

        simulator.startStream();

    }

    /**
     * Returns a Kafka producer
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