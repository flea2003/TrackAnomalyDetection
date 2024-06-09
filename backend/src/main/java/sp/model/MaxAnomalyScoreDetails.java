package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
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
}
