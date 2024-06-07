package sp.pipeline.utils.json;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.util.function.SerializableSupplier;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is a very slightly modified version of the JsonSerializationSchema class from the
 * Flink library. It is used to deserialize JSON strings into Java objects. The only modification is
 * that it ignores all exceptions that occur during deserialization and returns null instead of
 * throwing an exception.
 */

@SuppressWarnings("PMD")
public class MyJsonSerializationSchema<T> implements SerializationSchema<T> {

    private static final long serialVersionUID = 1L;

    private final SerializableSupplier<ObjectMapper> mapperFactory;

    protected transient ObjectMapper mapper;

    /**
     * Creates a JSON serialization schema for the given class.
     */
    public MyJsonSerializationSchema() {
        this(() -> new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    /**
     * Creates a JSON serialization schema for the given class.
     *
     * @param mapperFactory A factory for creating {@link ObjectMapper} instances
     */
    public MyJsonSerializationSchema(SerializableSupplier<ObjectMapper> mapperFactory) {
        this.mapperFactory = mapperFactory;
    }

    /**
     * Creates a JSON serialization schema for the given class.
     *
     * @param context The object mapper to use.
     */
    @Override
    public void open(InitializationContext context) {
        mapper = mapperFactory.get();
    }

    /**
     * Serializes the incoming element to a byte array.
     *
     * @param element The element to be serialized.
     * @return The serialized element.
     */
    @Override
    public byte[] serialize(T element) {
        try {
            return mapper.writeValueAsBytes(element);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
