package sp.pipeline.parts.aggregation.extractors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.utils.StreamUtils;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GenericKafkaExtractor {
    protected final StreamUtils streamUtils;
    protected final PipelineConfiguration configuration;
    private final int pollingFrequency;

    /**
     * Constructor for a class that handles polling (and extracting) from a Kafka topic.
     *
     * @param streamUtils an object that holds utility methods for dealing with streams
     * @param configuration an object that holds configuration properties
     * @param pollingFrequency the frequency at which we poll the Kafka topic (in nanoseconds)
     */
    public GenericKafkaExtractor(StreamUtils streamUtils,
                                 PipelineConfiguration configuration,
                                 int pollingFrequency) {
        this.streamUtils = streamUtils;
        this.configuration = configuration;
        this.pollingFrequency = pollingFrequency;

        // Spawn a thread to constantly consume from the Kafka topic and update the state
        new Thread(this::stateUpdatingThread).start();

    }

    /**
     * This method runs in a thread, separate from the main application. It handles polling the Kafka topic
     * and distributing the processing of the incoming messages to separate threads.
     */
    private void stateUpdatingThread() {
        // Create a consumer for the current ship details topic
        try(KafkaConsumer<Long, String> consumer = streamUtils.getConsumer()) {

            consumer.subscribe(List.of(configuration.getCurrentShipDetailsTopicName()));

            // Create a thread pool to handle the incoming messages
            final ExecutorService executor = Executors.newFixedThreadPool(20);

            // Busy poll the Kafka topic. If new message has arrived, process it in a separate thread
            while (true) {
                try {
                    Thread.sleep(0, pollingFrequency);
                    consumer.poll(Duration.ofNanos(pollingFrequency)).forEach(record -> {
                        executor.submit(() -> processNewRecord(record));
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Processes an incoming record from the Kafka topic.
     *
     * @param record the record incoming from Kafka topic
     */
    protected abstract void processNewRecord(ConsumerRecord<Long, String> record);
}
