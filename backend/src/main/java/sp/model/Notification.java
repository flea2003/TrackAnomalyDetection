package sp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    public Notification(Float score, String explanation, OffsetDateTime correspondingTimestamp, String shipHash, float longitude, float latitude) {

        this.score = score;
        this.explanation = explanation;
        this.correspondingTimestamp = correspondingTimestamp;
        this.shipHash = shipHash;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Notification(AnomalyInformation anomalyInformation) {

        this.score = anomalyInformation.getScore();
        this.explanation = anomalyInformation.getExplanation();
        this.correspondingTimestamp = anomalyInformation.getCorrespondingTimestamp();
        this.shipHash = anomalyInformation.getShipHash();
        this.longitude = 0;
        this.latitude = 0;
    }


    public AnomalyInformation getAnomalyInformation() {
        return new AnomalyInformation(score, explanation, correspondingTimestamp, shipHash);
    }

}
