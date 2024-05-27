package sp.pipeline.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.kafka.streams.kstream.Aggregator;
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
     * @param <T> the type of resulting stream
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
     * @param <T> the type of the stream to serialize
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

    /**
     * A helper function for aggregating values in a Kafka Stream. It defines a Kafka Aggregator
     * that takes as input a key, a JSON string value, and an aggregate value, and then deserializes
     * that JSON into an object of a given class type. It then applies the given aggregator function
     * to the deserialized value and the aggregate, and returns the new aggregate.
     * Essentially it makes sure that the logic of aggregation does not have to deal with JSON deserialization.
     *
     * @param aggregatorFunction the aggregation function to which to pass the aggregate and the deserialized value
     * @param valueType the types of JSON strings to deserialize to
     * @param <A> the type of the aggregate
     * @param <V> the type of the value to deserialize JSONs to
     * @return an Aggregator that can be used in Kafka Streams
     */
    public static <A, V> Aggregator<Long, String, A> aggregator(
            TriFunction<A, V, Long, A> aggregatorFunction,
            Class<V> valueType
    ) {
        return (key, valueJson, aggregate) -> {
            try {
                V value = JsonMapper.fromJson(valueJson, valueType);
                return aggregatorFunction.apply(aggregate, value, key);
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize JSON value inside of an aggregator. Skipping it. JSON: " + valueJson);
                return aggregate;
            }
        };
    }
}
