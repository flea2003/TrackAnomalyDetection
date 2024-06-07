package sp.pipeline.utils;

import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.utils.json.MyJsonDeserializationSchema;
import sp.pipeline.utils.json.MyJsonSerializationSchema;
import java.util.Properties;
import java.util.UUID;

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
     * Creates a Flink source that is consuming from a Kafka topic. Assumes that incoming data is in JSON format.
     * The type of the data is specified by the classType parameter.
     *
     * @param topic the name of the topic from which to consume
     * @param classType the type of the incoming data
     * @param <T> the type of data that is expected to be in the topic
     * @return the created Flink stream
     */
    public <T> KafkaSource<T> getFlinkStreamConsumingFromKafka(String topic, Class<T> classType) {
        // Load the properties of Kafka from the configuration file
        Properties props = configuration.getFullConfiguration();
        JsonDeserializationSchema<T> jsonFormat = new MyJsonDeserializationSchema<>(classType);

        return KafkaSource.<T>builder()
                .setProperties(props)
                .setTopics(topic)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(jsonFormat)
                .build();
    }

    /**
     * Creates a sink from Flink to a Kafka topic. Also deals with serializing to JSON format.
     *
     * @param topicName Kafka topic name to send the data to
     * @param <T> the type of data that will be sent to the topic
     * @return the created KafkaSink object
     */
    public <T> KafkaSink<T> createSinkFlinkToKafka(String topicName) {
        MyJsonSerializationSchema<T> jsonFormat = new MyJsonSerializationSchema<>();
        return KafkaSink.<T>builder()
                .setBootstrapServers(configuration.getKafkaServerAddress())
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topicName)
                        .setValueSerializationSchema(jsonFormat)
                        .build()
                )
                .build();
    }

    /**
     * Creates a Kafka consumer object, consuming from the Kafka server specified in the configuration file.
     *
     * @return the created KafkaConsumer object
     */
    public KafkaConsumer<Long, String> getConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.getKafkaServerAddress());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        return new KafkaConsumer<>(props);
    }
}

