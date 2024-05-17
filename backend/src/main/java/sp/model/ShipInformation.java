package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.utils.UtilsObjectMapper;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class ShipInformation {
    private String shipHash;

    private AnomalyInformation anomalyInformation;
    private AISSignal aisSignal;

    /**
     * Converts a particular AISUpdate object to a JSON string.
     *
     * @return the respective JSON string
     */
    public String toJson() throws JsonProcessingException {
        // Assert that there are no flaws in the data
        if (!shipHash.isEmpty() && !shipHash.isBlank()) {
            assert anomalyInformation == null || anomalyInformation.getShipHash().equals(shipHash);
            assert aisSignal == null || aisSignal.getShipHash().equals(shipHash);
        }

        return new UtilsObjectMapper().writeValueAsString(this);
    }

    /**
     * Converts a JSON string to an AISUpdate object.
     *
     * @param val the JSON string to convert
     * @return the converted AISUpdate object
     */
    public static ShipInformation fromJson(String val) throws JsonProcessingException {
        return new UtilsObjectMapper().readValue(val, ShipInformation.class);
    }
}
