package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import sp.dtos.AnomalyInformation;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class ShipInformation {
    private long id;
    private AnomalyInformation anomalyInformation;
    private AISSignal aisSignal;

    /**
     * Converts a particular AISUpdate object to a JSON string.
     *
     * @return the respective JSON string
     */
    public String toJson() {
        // Assert that there are no flaws in the data
        assert anomalyInformation == null || anomalyInformation.getId() == getId();
        assert aisSignal == null || aisSignal.getId() == id;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json;
        try {
            json = mapper.writeValueAsString(this);
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
    public static ShipInformation fromJson(String val) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            return mapper.readValue(val, ShipInformation.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
