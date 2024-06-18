package sp.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import sp.model.Notification;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonSerialize
public class TrajectoryObject {
    private final long shipId;
    private final float longitude;
    private final float latitude;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime timeValue;
    private final Float anomalyScore;

    /**
     * Constructor for Trajectory object, given a notifcation object.
     *
     * @param notification notification details that should be included in a ships' trajectory
     */
    public TrajectoryObject(Notification notification) {
        this.shipId = notification.getShipID();
        this.latitude = notification.getCurrentShipDetails().getCurrentAISSignal().getLatitude();
        this.longitude = notification.getCurrentShipDetails().getCurrentAISSignal().getLongitude();
        this.timeValue = notification.getCurrentShipDetails().getCurrentAISSignal().getTimestamp();
        this.anomalyScore = notification.getCurrentShipDetails().getCurrentAnomalyInformation() == null ? -1 :
                notification.getCurrentShipDetails().getCurrentAnomalyInformation().getScore();
    }

    /**
     * Constructor for Trajectory object, given a DatabaseExtractObject object.
     *
     * @param object a simplified CurrentShipDetails object without MaxAnomalyInfo and full anomaly information
     *               which has the needed trajectory information
     */
    public TrajectoryObject(DatabaseExtractObject object) {
        this.shipId = object.getAisSignal().getId();
        this.latitude = object.getAisSignal().getLatitude();
        this.longitude = object.getAisSignal().getLongitude();
        this.timeValue = object.getAisSignal().getTimestamp();
        this.anomalyScore = object.getAnomalyScore() == null ? -1 :
                object.getAnomalyScore();
    }
}
