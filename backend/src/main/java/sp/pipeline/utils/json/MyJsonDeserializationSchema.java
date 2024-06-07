package sp.pipeline.utils.json;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.function.SerializableSupplier;
import org.apache.flink.util.jackson.JacksonMapperFactory;
import java.io.IOException;

/**
 * This class is a slightly modified version of the JsonDeserializationSchema class from the
 * Flink library. It is used to deserialize JSON strings into Java objects. The only modification is
 * that it ignores all exceptions that occur during deserialization and returns null instead of
 * throwing an exception.
 */
public class MyJsonDeserializationSchema<T> extends JsonDeserializationSchema<T> {

    private static final long serialVersionUID = 1L;

    private final Class<T> clazz;
    private final SerializableSupplier<ObjectMapper> mapperFactory;
    protected transient ObjectMapper mapper;

    /**
     * Creates a JSON deserialization schema for the given class.
     *
     * @param clazz The class to which the JSON string is deserialized.
     */
    public MyJsonDeserializationSchema(Class<T> clazz) {
        this(clazz, JacksonMapperFactory::createObjectMapper);
    }

    /**
     * Creates a JSON deserialization schema for the given class.
     *
     * @param clazz The class to which the JSON string is deserialized.
     * @param mapperFactory A factory for creating ObjectMapper instances
     */
    public MyJsonDeserializationSchema(
            Class<T> clazz, SerializableSupplier<ObjectMapper> mapperFactory) {
        super(clazz);
        this.clazz = clazz;
        this.mapperFactory = mapperFactory;
    }

    /**
     * Creates a JSON deserialization schema for the given class.
     *
     * @param context the initialization context
     */
    @Override
    public void open(InitializationContext context) {
        mapper = mapperFactory.get();
    }

    /**
     * Deserializes the JSON message.
     *
     * @param message The message, as a byte array.
     * @return The deserialized message as an object.
     */
    @Override
    public T deserialize(byte[] message) {
        try {
            return mapper.readValue(message, clazz);
        } catch (IOException e) {
            return null;
        }
    }
}
