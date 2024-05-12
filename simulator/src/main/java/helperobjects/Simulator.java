package helperobjects;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import parsers.Parser;

public class Simulator {

    @Getter
    private final Stream stream;
    private final String topicName;
    private final KafkaProducer<String, String> producer;

    @Setter
    private int speed;

    /**
     * Constructor for the helperObjects.Simulator object. It instantiates the Stream object, which calls a needed
     * parser that parses the data file, then sorts the signals by their timestamp, and specifies the rest of the
     * data needed for streaming the data.
     *
     * @param parser        parser for the needed data file
     * @param streamStart   start timestamp of the stream
     * @param streamEnd     end timestamp of the stream
     * @param topicName     name of the topic where the stream is directed to
     * @param producer      Kafka producer
     */
    public Simulator(Parser parser, Timestamp streamStart, Timestamp streamEnd, String topicName,
                     KafkaProducer<String, String> producer) throws IOException {
        this.stream = new Stream(streamStart, streamEnd);
        this.stream.parseData(parser);
        this.stream.sortStream();
        this.topicName = topicName;
        this.producer = producer;
        this.speed = 1;
    }

    /**
     * Constructor for setting the wanted stream manually.
     *
     * @param stream actual stream object containing the data that will be streamed
     * @param topicName the topic name
     * @param producer the Kafka producer
     */
    public Simulator(Stream stream, String topicName, KafkaProducer<String, String> producer) {
        this.stream = stream;
        this.topicName = topicName;
        this.producer = producer;
        this.speed = 1;
    }

    /**
     * Method responsible to start the actual streaming of the data. Once called, it starts streaming AIS signals to
     * the specified topic.
     *
     * @throws InterruptedException when interrupted
     */
    public void startStream() throws InterruptedException {
        // Get the stream data
        List<SimpleEntry<Timestamp, String>> data = stream.getData();

        // If the data is empty, return
        if (data.isEmpty()) return;

        // Set the previous signal as the first one
        SimpleEntry<Timestamp, String> previous = data.get(0);

        // Iterate through all signals that need to be streamed
        for (SimpleEntry<Timestamp, String> entry : data) {

            // Check if the current signal should be streamed at the same time as the previous one. If yes, stream it.
            if (previous.getKey().compareTo(entry.getKey()) == 0)
                producer.send(new ProducerRecord<>(this.topicName, entry.getValue()), (metadata, exception) -> {});
            else {

                // Otherwise, stream it after a needed amount of time
                long difference = entry.getKey().difference(previous.getKey());

                // Note that in our case we assume that the signals are retrieved in minutes, not seconds.
                Thread.sleep(difference * 1000 * 60 / this.speed);
                producer.send(new ProducerRecord<>(this.topicName, entry.getValue()), (metadata, exception) -> {});
            }

            // Flush the data and update the previous signal value
            producer.flush();
            previous = entry;
        }
    }

}
