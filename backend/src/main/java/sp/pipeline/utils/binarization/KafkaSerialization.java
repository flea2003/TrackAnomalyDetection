package sp.pipeline.utils.binarization;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class KafkaSerialization {
    private static final Logger logger = LoggerFactory.getLogger(KafkaSerialization.class);

    /**
     * Deserialize a Kafka Stream of binary-encoded strings to a KStream of objects of a given class type.
     * In case of an exception during deserialization, we ignore the message and return an empty list.
     *
     * @param stream KStream of binary-encoded strings
     * @param classType class type of the objects to deserialize to
     * @param <T> the type of resulting stream
     * @return a KStream of objects of the given class type
     */
    public static <T> KStream<Long, T> deserialize(KStream<Long, String> stream,
                                                   Class<T> classType) {
        return stream.flatMapValues(x -> {
            try {
                return List.of(SerializationMapper.fromSerializedString(x, classType));
            } catch (Exception e) {
                logger.warn("Failed to deserialize binary-encoded message, incoming to Kafka. "
                        + "Skipping this message. Message: " + x);
                return new ArrayList<>();
            }
        });
    }

    /**
     * Serialize a Kafka Stream of objects to a KStream of binary-encoded strings.
     *
     * @param stream KStream of objects
     * @param <T> the type of the stream to serialize
     * @return a KStream of binary-encoded strings
     */
    public static <T> KStream<Long, String> serialize(KStream<Long, T> stream) {
        return stream.flatMapValues(x -> {
            try {
                return List.of(SerializationMapper.toSerializedString(x));
            } catch (Exception e) {
                logger.error("Failed to serialize object to binary-encoded string, outgoing from Kafka. "
                        + "Skipping this object. Object: " + x);
                return new ArrayList<>();
            }
        });
    }

    /**
     * A helper function for aggregating values in a Kafka Stream. It defines a Kafka Aggregator
     * that takes as input a key, a binary-encoded string value, and an aggregate value, and then deserializes
     * that binary-encoded string into an object of a given class type. It then applies the given aggregator function
     * to the deserialized value and the aggregate, and returns the new aggregate.
     * Essentially it makes sure that the logic of aggregation does not have to deal with binary-encoded string deserialization.
     *
     * @param aggregatorFunction the aggregation function to which to pass the aggregate and the deserialized value
     * @param valueType the types of binary-encoded strings to deserialize to
     * @param <A> the type of the aggregate
     * @param <V> the type of the value to deserialize the binary-encoded string to
     * @return an Aggregator that can be used in Kafka Streams
     */
    public static <A, V> Aggregator<Long, String, A> aggregator(
            TriFunction<A, V, Long, A> aggregatorFunction,
            Class<V> valueType
    ) {
        return (key, valueSerialized, aggregate) -> {
            try {
                V value = SerializationMapper.fromSerializedString(valueSerialized, valueType);
                return aggregatorFunction.apply(aggregate, value, key);
            } catch (Exception e) {
                logger.error("Failed to deserialize binary-encoded value inside of an aggregator. "
                        + "Skipping this message. Message: " + valueSerialized);
                return aggregate;
            }
        };
    }
}
