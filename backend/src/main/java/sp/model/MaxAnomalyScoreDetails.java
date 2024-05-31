package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class MaxAnomalyScoreDetails implements Serializable {
    private Float maxAnomalyScore;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime correspondingTimestamp;

    public static MaxAnomalyScoreDetails fromJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, MaxAnomalyScoreDetails.class);
    }
}
