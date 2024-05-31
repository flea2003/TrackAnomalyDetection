package helperobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonSerialize
public class AISSignal implements Serializable {
    private final String producerID;
    private final String shipHash;
    private final float speed;
    private final float longitude;
    private final float latitude;
    private final float course;
    private final float heading;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime timestamp;
    private final String departurePort;

    /**
     * Returns the object in JSON format.
     *
     * @return json representation of the object
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.writeValueAsString(this);
    }

    /**
     * Creates the AIS object from a given string.
     *
     * @param val string value (in JSON format) that is being converted to an AIS object
     * @return AIS object from a given string
     */
    public static AISSignal fromJson(String val) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(val, AISSignal.class);
    }
}