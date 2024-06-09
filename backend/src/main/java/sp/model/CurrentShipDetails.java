package sp.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.kafka.common.serialization.Serde;
import sp.utils.UtilsObjectMapper;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class CurrentShipDetails implements Serializable {
    private AnomalyInformation currentAnomalyInformation;
    private AISSignal currentAISSignal;
    private MaxAnomalyScoreDetails maxAnomalyScoreInfo;

    /**
     * Get serializer+deserializer for CurrentShipDetails. I am using simple JSON serialization here.
     * With the help of Jackson2Serde, I don't have to write the object.
     *
     * @return Serde object for this class.
     */
    public static Serde<CurrentShipDetails> getSerde() {
        return new Jackson2Serde<>(new UtilsObjectMapper(), CurrentShipDetails.class);
    }
}
