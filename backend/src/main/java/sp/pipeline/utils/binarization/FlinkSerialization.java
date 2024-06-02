package sp.pipeline.utils.binarization;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkSerialization {

    private static final Logger logger = LoggerFactory.getLogger(FlinkSerialization.class);

    /**
     * Static method that takes a Flink data stream of objects and serializes them to serialized strings.
     *
     * @param source a Flink data stream of objects
     * @param <T> the type of incoming stream
     * @return a Flink data stream of kry-encoded strings
     */
    public static <T> DataStream<String> serialize(DataStream<T> source) {
        return source.flatMap((T x, Collector<String> collector) -> {
            try {
                collector.collect(SerializationMapper.toSerializedString(x));
            } catch (Exception e) {
                logger.error("Failed to serialize object to binary, outgoing from Flink. Skipping this object. Object: " + x);
            }
        }).returns(String.class);

    }

    /**
     * Static method that takes a Flink data stream of binary-encoded strings and deserializes them to objects.
     *
     * @param source a Flink data stream of binary-encoded strings
     * @param classType the class type of the objects to deserialize to
     * @param <T> the type of resulting stream
     * @return a Flink data stream of objects
     */
    public static <T> DataStream<T> deserialize(DataStream<String> source, Class<T> classType) {
        return source.flatMap((String x, Collector<T> collector) -> {
            try {
                collector.collect(SerializationMapper.fromSerializedString(x, classType));
            } catch (Exception e) {
                logger.error("Failed to deserialize binary-encoded message, incoming to Flink. "
                        + "Skipping this message. Message: " + x);
            }
        }).returns(classType);
    }
}
