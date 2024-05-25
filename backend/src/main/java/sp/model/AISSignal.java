package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import sp.dtos.ExternalAISSignal;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonSerialize
public class AISSignal implements Serializable {
    private final long id;
    private final float speed;
    private final float longitude;
    private final float latitude;
    private final float course;
    private float heading;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime timestamp;
    private final String departurePort;

    /**
     * Constructor for the AISSignal class, which is used to create an AISSignal
     * object from an ExternalAISSignal object, with a given id.
     *
     * @param externalSignal the incoming dto
     * @param id the ID to assign
     */
    public AISSignal(ExternalAISSignal externalSignal, long id) {
        this.id = id;
        this.speed = externalSignal.getSpeed();
        this.longitude = externalSignal.getLongitude();
        this.latitude = externalSignal.getLatitude();
        this.course = externalSignal.getCourse();
        this.heading = externalSignal.getHeading();
        this.timestamp = externalSignal.getTimestamp();
        this.departurePort = externalSignal.getDeparturePort();
    }
}