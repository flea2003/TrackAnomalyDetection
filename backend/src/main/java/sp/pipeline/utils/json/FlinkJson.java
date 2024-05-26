package sp.pipeline.utils.json;

import org.apache.flink.streaming.api.datastream.DataStream;

public class FlinkJson {

    /**
     * Static method that takes a Flink data stream of objects and serializes them to JSON strings.
     *
     * @param source a Flink data stream of objects
     * @return a Flink data stream of JSON strings
     */
    public static <T> DataStream<String> serialize(DataStream<T> source) {
        return source.map(JsonMapper::toJson);  // TODO: handle exceptions here
    }

    /**
     * Static method that takes a Flink data stream of JSON strings and deserializes them to objects.
     *
     * @param source a Flink data stream of JSON strings
     * @param classType the class type of the objects to deserialize to
     * @return a Flink data stream of objects
     */
    public static <T> DataStream<T> deserialize(DataStream<String> source, Class<T> classType) {
        return source.map(x -> JsonMapper.fromJson(x, classType)).returns(classType);   // TODO: handle exceptions here
    }
}
