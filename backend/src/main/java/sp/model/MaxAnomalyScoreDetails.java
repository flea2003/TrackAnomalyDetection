package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonSerialize
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
public class MaxAnomalyScoreDetails implements Serializable {
    private final Float maxAnomalyScore;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime correspondingTimestamp;
}
