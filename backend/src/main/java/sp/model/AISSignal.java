package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import lombok.*;
import org.apache.kafka.common.serialization.Serde;
import sp.dtos.AnomalyInformation;
import sp.dtos.StreamRecord;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonSerialize
public class AISSignal implements Serializable, StreamRecord {

    public final String shipHash;
    public final float speed;
    public final float longitude;
    public final float latitude;
    public final float course;
    public final float heading;
    public final String timestamp;
    public final String departurePort;

    /**
     * Returns the object in JSON format
     *
     * @return json representation of the object
     */
    @Override
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {return mapper.writeValueAsString(this);}
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the AIS object from a given string
     *
     * @param val string value (in JSON format) that is being converted to an AIS object
     * @return AIS object from a given string
     */
    public static AISSignal fromJson(String val) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(val, AISSignal.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Serde<AISSignal> getSerde() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        return new Jackson2Serde<>(jsonObjectMapper, AISSignal.class);
    }

}