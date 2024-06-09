package sp.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonSerialize
public class ExternalAISSignal implements Serializable {
    private final String producerID;
    private final String shipHash;
    private final float speed;
    private final float longitude;
    private final float latitude;
    private final float course;
    private float heading;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime timestamp;
    private final String departurePort;
}