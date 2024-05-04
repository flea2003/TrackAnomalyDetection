package helperObjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class AIS implements Serializable {
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
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString( this );
        } catch (JsonProcessingException e) {
            throw e;
        }
        return json;
    }

    /**
     * Creates the AIS object from a given string
     *
     * @param val string value (in JSON format) that is being converted to an AIS object
     * @return AIS object from a given string
     */
    public static AIS fromJson(String val) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(val, AIS.class);
        } catch (JsonProcessingException e) {
            throw e;
        }
    }
}