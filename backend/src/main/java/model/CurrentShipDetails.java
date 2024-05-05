package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serde;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonSerialize
public class CurrentShipDetails implements Serializable {
    private Float score;
    private List<AISSignal> pastSignals;


    /**
     * Get serializer+deserializer for CurrentShipDetails. I am using simple JSON serialization here.
     * With the help of Jackson2Serde, I don't have to write the object.
     *
     * @return Serde object for this class.
     */
    public static Serde<CurrentShipDetails> getSerde() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        return new Jackson2Serde<>(jsonObjectMapper, CurrentShipDetails.class);
    }
}
