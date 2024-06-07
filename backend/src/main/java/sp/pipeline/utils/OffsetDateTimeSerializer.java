package sp.pipeline.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeSerializer extends Serializer<OffsetDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public void write(Kryo kryo, Output output, OffsetDateTime offsetDateTime) {
        output.writeString(offsetDateTime.format(FORMATTER));
    }

    /**
     * Reads bytes and returns a new object of the specified concrete type.
     * <p>
     * Before Kryo can be used to read child objects, {@link Kryo#reference(Object)} must be called with the parent object to
     * ensure it can be referenced by the child objects. Any serializer that uses {@link Kryo} to read a child object may need to
     * be reentrant.
     * <p>
     * This method should not be called directly, instead this serializer can be passed to {@link Kryo} read methods that accept a
     * serialier.
     *
     * @param kryo
     * @param input
     * @param type
     * @return May be null if {@link #getAcceptsNull()} is true.
     */
    @Override
    public OffsetDateTime read(Kryo kryo, Input input, Class<OffsetDateTime> type) {
        return OffsetDateTime.parse(input.readString(), FORMATTER);
    }

}