package sp.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import lombok.*;
import org.apache.kafka.common.serialization.Serde;
import sp.model.ShipInformation;

import java.io.Serializable;
import java.util.stream.Stream;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
public class AnomalyInformation implements Serializable, StreamRecord {
    private final Float score;
    private final String explanation;
    private final String correspondingTimestamp;
    private final String shipHash;


    /**
     * Converts a particular AISUpdate object to a JSON string.
     *
     * @return the respective JSON string
     */
    @Override
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
    public static AnomalyInformation fromJson(String val) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(val, AnomalyInformation.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public static Serde<AnomalyInformation> getSerde() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        return new Jackson2Serde<>(jsonObjectMapper, AnomalyInformation.class);
    }
}
