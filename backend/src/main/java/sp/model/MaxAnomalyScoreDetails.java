package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@Setter
public class MaxAnomalyScoreDetails implements Serializable {
    private Float maxAnomalyScore;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime correspondingTimestamp;
}
