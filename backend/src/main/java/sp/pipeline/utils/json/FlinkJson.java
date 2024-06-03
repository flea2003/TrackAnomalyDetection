package sp.pipeline.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkJson {
    private static final Logger logger = LoggerFactory.getLogger(FlinkJson.class);

    /**
     * Static method that takes a Flink data stream of objects and serializes them to JSON strings.
     *
     * @param source a Flink data stream of objects
     * @param <T> the type of incoming stream
     * @return a Flink data stream of JSON strings
     */
    public static <T> DataStream<String> serialize(DataStream<T> source) {
        return source.flatMap((T x, Collector<String> collector) -> {
            try {
                collector.collect(JsonMapper.toJson(x));
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize object to JSON, outgoing from Flink. Skipping this object. Object: {}", x);
            }
        }).returns(String.class);

    }

    /**
     * Static method that takes a Flink data stream of JSON strings and deserializes them to objects.
     *
     * @param source a Flink data stream of JSON strings
     * @param classType the class type of the objects to deserialize to
     * @param <T> the type of resulting stream
     * @return a Flink data stream of objects
     */
    public static <T> DataStream<T> deserialize(DataStream<String> source, Class<T> classType) {
        return source.flatMap((String x, Collector<T> collector) -> {
            try {
                collector.collect(JsonMapper.fromJson(x, classType));
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize JSON message, incoming to Flink. Skipping this message. Message: {}", x);
            }
        }).returns(classType);
    }
}
