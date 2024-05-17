package sp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import sp.dtos.AnomalyInformation;
import java.time.OffsetDateTime;

@Entity
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Notification {
    private final Float score;
    private final String explanation;
    private final OffsetDateTime correspondingTimestamp;
    private final String shipHash;
    private final float longitude;
    private final float latitude;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Constructor for the notification object
     *
     * @param score anomaly score
     * @param explanation anomaly explanation
     * @param correspondingTimestamp timestamp for the notification
     * @param shipHash hash of the ship
     * @param longitude longitude
     * @param latitude longitude
     */
    public Notification(Float score, String explanation, OffsetDateTime correspondingTimestamp, String shipHash, float longitude, float latitude) {
        this.score = score;
        this.explanation = explanation;
        this.correspondingTimestamp = correspondingTimestamp;
        this.shipHash = shipHash;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Constructor of notification with given Anomaly Information object.
     * This will alter also include AIS information, but that will be done
     * when the database is configured
     *
     * @param anomalyInformation anomaly information object
     */
    public Notification(AnomalyInformation anomalyInformation) {

        this.score = anomalyInformation.getScore();
        this.explanation = anomalyInformation.getExplanation();
        this.correspondingTimestamp = anomalyInformation.getCorrespondingTimestamp();
        this.shipHash = anomalyInformation.getShipHash();
        this.longitude = 0;
        this.latitude = 0;
    }

    /**
     * Method that is used by the pipeline
     *
     * @return AnomalyInformation object
     */
    public AnomalyInformation getAnomalyInformation() {
        return new AnomalyInformation(score, explanation, correspondingTimestamp, shipHash);
    }
}
