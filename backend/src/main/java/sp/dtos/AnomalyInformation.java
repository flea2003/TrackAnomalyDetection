package sp.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
public class AnomalyInformation implements Serializable {
    private final Float score;
    private final String explanation;
    private final String correspondingTimestamp;
    private final String shipHash;

    /**
     * Converts a particular AISUpdate object to a JSON string.
     *
     * @return the respective JSON string
     */
    public String toJson(){
        ObjectMapper mapper = new ObjectMapper();
        String json;
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
    public static AnomalyInformation fromJson(String val) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(val, AnomalyInformation.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
