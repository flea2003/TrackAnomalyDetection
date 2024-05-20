package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import sp.utils.UtilsObjectMapper;
import java.io.Serializable;
import java.time.OffsetDateTime;


@Getter
@Setter
@NoArgsConstructor(force = true)
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class MaxAnomalyScoreDetails implements Serializable {
    private final Float maxAnomalyScore;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime correspondingTimestamp;

    /**
     * Converts a particular MaxAnomalyScoreDetails object to a JSON string.
     *
     * @return the respective JSON string
     */
    public String toJson() throws JsonProcessingException {
        return new UtilsObjectMapper().writeValueAsString(this);
    }

    /**
     * Converts a JSON string to an AnomalyInformation object.
     *
     * @param val the JSON string to convert
     * @return the converted AISUpdate object
     */
    public static MaxAnomalyScoreDetails fromJson(String val) throws JsonProcessingException {
        return new UtilsObjectMapper().readValue(val, MaxAnomalyScoreDetails.class);
    }

}
