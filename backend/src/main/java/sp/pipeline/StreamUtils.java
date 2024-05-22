package sp.pipeline;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

@Service
public class StreamUtils {

    private final PipelineConfiguration configuration;

    /**
     * Sets up the StreamUtils object
     *
     * @param configuration object that hold configuration properties
     */
    @Autowired
    public StreamUtils(PipelineConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates a Flink stream that is consuming from a Kafka topic.
     *
     * @param topic the name of the topic from which to consume
     * @return the created Flink stream
     */
    public KafkaSource<String> getFlinkStreamConsumingFromKafka(String topic) throws IOException {
        // Load the properties of Kafka from the configuration file
        Properties props = configuration.loadConfig();

        return KafkaSource.<String>builder()
                .setProperties(props)
                .setTopics(topic)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }

    /**
     * Creates a KafkaStreams object, according to the configuration file.
     *
     * @param builder streams builder
     * @return created KafkaStreams object with the specified configuration
     */
    public KafkaStreams getKafkaStreamConsumingFromKafka(StreamsBuilder builder) throws IOException {
        // Load properties from the configuration file
        Properties props = configuration.loadConfig();

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaStreams(builder.build(), props);
    }
}

