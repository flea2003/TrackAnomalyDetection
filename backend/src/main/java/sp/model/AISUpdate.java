package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import lombok.*;
import org.apache.kafka.common.serialization.Serde;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString
public class AISUpdate {
    private String shipHash;
    private float newScore;
    private AISSignal correspondingSignal;

    /**
     * Converts a particular AISUpdate object to a JSON string.
     *
     * @return the respective JSON string
     */
    public String toJson(){
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString( this );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    /**
     * Converts a JSON string to an AISUpdate object.
     *
     * @param val the JSON string to convert
     * @return the converted AISUpdate object
     */
    public static AISUpdate fromJson(String val) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(val, AISUpdate.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get serializer+deserializer for AISUpdate. Simple JSON serialization is used here.
     *
     * @return Serde object for this class.
     */
    public static Serde<AISUpdate> getSerde() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        return new Jackson2Serde<>(jsonObjectMapper, AISUpdate.class);
    }
}
