package sp.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
import sp.model.MaxAnomalyScoreDetails;
import sp.utils.UtilsObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class ExtendedAnomalyInformation implements Serializable {
    private AnomalyInformation anomalyInformation;
    private MaxAnomalyScoreDetails maxAnomalyScoreDetails;

    /**
     * Converts a particular ExtendedAnomalyInformation object to a JSON string.
     *
     * @return the respective JSON string
     */
    public String toJson() throws JsonProcessingException {
        return new UtilsObjectMapper().writeValueAsString(this);
    }

    /**
     * Converts a JSON string to an ExtendedAnomalyInformation object.
     *
     * @param val the JSON string to convert
     * @return the converted ExtendedAnomalyInformation object
     */
    public static ExtendedAnomalyInformation fromJson(String val) throws JsonProcessingException {
        return new UtilsObjectMapper().readValue(val, ExtendedAnomalyInformation.class);
    }


}
