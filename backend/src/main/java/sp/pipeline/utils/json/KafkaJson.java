package sp.pipeline.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class KafkaJson {
    private static final Logger logger = LoggerFactory.getLogger(KafkaJson.class);

    /**
     * Deserialize a Kafka Stream of JSON strings to a KStream of objects of a given class type.
     * In case of an exception during deserialization, we ignore the message and return an empty list.
     *
     * @param stream KStream of JSON strings
     * @param classType class type of the objects to deserialize to
     * @return a KStream of objects of the given class type
     */
    public static <T> KStream<Long, T> deserialize(KStream<Long, String> stream,
                                                   Class<T> classType) {
        return stream.flatMapValues(x -> {
            try {
                return List.of(JsonMapper.fromJson(x, classType));
            } catch (JsonProcessingException e) {
                logger.warn("Failed to deserialize JSON message, incoming to Kafka. Skipping this message. Message: " + x);
                return new ArrayList<>();
            }
        });
    }

    /**
     * Serialize a Kafka Stream of objects to a KStream of JSON strings.
     *
     * @param stream KStream of objects
     * @return a KStream of JSON strings
     */
    public static <T> KStream<Long, String> serialize(KStream<Long, T> stream) {
        return stream.flatMapValues(x -> {
            try {
                return List.of(JsonMapper.toJson(x));
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize object to JSON, outgoing from Kafka. Skipping this object. Object: " + x);
                return new ArrayList<>();
            }
        });
    }
}
