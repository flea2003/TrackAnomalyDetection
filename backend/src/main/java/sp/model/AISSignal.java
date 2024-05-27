package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.*;
import sp.dtos.ExternalAISSignal;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor(force = true)
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
@JsonSerialize
public class AISSignal implements Serializable {
    private static final int NO_HEADING = 511;

    private final long id;
    private final float speed;
    private final float longitude;
    private final float latitude;
    private final float course;
    private final float heading;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime timestamp;
    private final String departurePort;
    @EqualsAndHashCode.Exclude
    private OffsetDateTime receivedTime;

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
        this.receivedTime = OffsetDateTime.now();
    }

//    /**
//     * Updates the heading if the heading is not reported.
//     * Not reported heading is denoted as the number 511.
//     */
//    public void updateHeading() {
//        // A 511 heading means that no heading is reported, so we just set it to be equal to the heading value of the ship
//        if (this.heading == NO_HEADING) {
//            this.heading = this.course;
//        }
//    }
}