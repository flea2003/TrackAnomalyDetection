package sp.pipeline.utils;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.pipeline.PipelineConfiguration;
import java.util.Properties;

@Service
public class StreamUtils {

    private final PipelineConfiguration configuration;

    /**
     * Sets up the StreamUtils object.
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
    public KafkaSource<String> getFlinkStreamConsumingFromKafka(String topic) {
        // Load the properties of Kafka from the configuration file
        Properties props = configuration.getFullConfiguration();

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
    public KafkaStreams getKafkaStreamConsumingFromKafka(StreamsBuilder builder) {
        // Load properties from the configuration file
        Properties props = configuration.getFullConfiguration();

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaStreams(builder.build(), props);
    }

    /**
     * Creates a sink from Flink to a Kafka topic.
     *
     * @param kafkaServerAddress Kafka server address
     * @param topicName Kafka topic name to send the data to
     * @return the created KafkaSink object
     */
    public KafkaSink<String> createSinkFlinkToKafka(String kafkaServerAddress, String topicName) {
        return KafkaSink.<String>builder()
                .setBootstrapServers(kafkaServerAddress)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topicName)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .build();
    }
}

