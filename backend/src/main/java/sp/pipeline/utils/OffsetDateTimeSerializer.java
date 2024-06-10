package sp.pipeline.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeSerializer extends Serializer<OffsetDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Writes the bytes for the object to the output.
     *
     * @param kryo the Kryo serializer object
     * @param output the output object
     * @param offsetDateTime the object to serialize
     */
    @Override
    public void write(Kryo kryo, Output output, OffsetDateTime offsetDateTime) {
        output.writeString(offsetDateTime.format(FORMATTER));
    }

    /**
     * Reads bytes and returns a new object of the specified concrete type.
     *
     * @param kryo the Kryo serializer object
     * @param input the input object
     * @param type the class of the object to create
     * @return the deserialized object
     */
    @Override
    public OffsetDateTime read(Kryo kryo, Input input, Class<OffsetDateTime> type) {
        return OffsetDateTime.parse(input.readString(), FORMATTER);
    }

}