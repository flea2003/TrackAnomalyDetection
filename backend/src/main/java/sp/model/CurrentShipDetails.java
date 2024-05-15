package sp.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.kafka.common.serialization.Serde;
import sp.dtos.AnomalyInformation;


@Data
@NoArgsConstructor
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class CurrentShipDetails implements Serializable {
    private AnomalyInformation anomalyInformation;
    private List<ShipInformation> pastInformation;

    /**
     * Get serializer+deserializer for CurrentShipDetails. I am using simple JSON serialization here.
     * With the help of Jackson2Serde, I don't have to write the object.
     *
     * @return Serde object for this class.
     */
    public static Serde<CurrentShipDetails> getSerde() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.registerModule(new JavaTimeModule());
        jsonObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2Serde<>(jsonObjectMapper, CurrentShipDetails.class);
    }
}
