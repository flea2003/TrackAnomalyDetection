package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class AnomalyInformation implements Serializable {
    private final float score;
    private final String explanation;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime correspondingTimestamp;
    private final Long id;
}
