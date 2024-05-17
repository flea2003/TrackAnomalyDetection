package sp.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.Serializable;
import java.time.OffsetDateTime;

import com.giladam.kafka.jacksonserde.Jackson2Serde;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.common.serialization.Serde;
import sp.model.CurrentShipDetails;
import sp.utils.UtilsObjectMapper;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class AnomalyInformation implements Serializable {
    private final Float score;
    private final String explanation;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime correspondingTimestamp;
    private final String shipHash;

    /**
     * Converts a particular AISUpdate object to a JSON string.
     *
     * @return the respective JSON string
     */
    public String toJson() throws JsonProcessingException {
        return new UtilsObjectMapper().writeValueAsString(this);
    }

    /**
     * Converts a JSON string to an AISUpdate object.
     *
     * @param val the JSON string to convert
     * @return the converted AISUpdate object
     */
    public static AnomalyInformation fromJson(String val) throws JsonProcessingException {
        return new UtilsObjectMapper().readValue(val, AnomalyInformation.class);
    }

    public static Serde<AnomalyInformation> getSerde() {
        return new Jackson2Serde<>(new UtilsObjectMapper(), AnomalyInformation.class);
    }
}
