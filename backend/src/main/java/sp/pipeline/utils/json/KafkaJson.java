package sp.pipeline.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.streams.kstream.KStream;

public class KafkaJson {

    /**
     * Deserialize a Kafka Stream of JSON strings to a KStream of objects of a given class type.
     *
     * @param stream KStream of JSON strings
     * @param classType class type of the objects to deserialize to
     * @return a KStream of objects of the given class type
     */
    public static <T> KStream<Long, T> deserialize(KStream<Long, String> stream,
                                                   Class<T> classType) {
        return stream.mapValues(x -> {
            try {
                return JsonMapper.fromJson(x, classType);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
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
        return stream.mapValues(x -> {
            try {
                return JsonMapper.toJson(x);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
