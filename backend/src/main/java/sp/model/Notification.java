package sp.model;

import com.giladam.kafka.jacksonserde.Jackson2Serde;
import jakarta.persistence.*;
import lombok.*;
import org.apache.kafka.common.serialization.Serde;
import sp.utils.UtilsObjectMapper;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Notification implements Serializable, Comparable<Notification> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long shipID;

    @Column(length = 2048)
    private final CurrentShipDetails currentShipDetails;


    /**
     * Constructor for the notifications object.
     *
     * @param currentShipDetails CurrentShip details object
     */
    public Notification(CurrentShipDetails currentShipDetails) {
        this.currentShipDetails = currentShipDetails;
        this.shipID = currentShipDetails.getCurrentAnomalyInformation().getId();

        // Assign a unique id to the notification
        this.id = Math.abs((long) Objects.hash(currentShipDetails.extractId(), OffsetDateTime.now()));
    }

    /**
     * Get serializer+deserializer for CurrentShipDetails. I am using simple JSON serialization here.
     * With the help of Jackson2Serde, I don't have to write the object.
     *
     * @return Serde object for this class.
     */
    public static Serde<Notification> getSerde() {
        return new Jackson2Serde<>(new UtilsObjectMapper(), Notification.class);
    }

    /**
     * Comparable method that compares two dates for notifications. If this notification
     * was computed later than otherNotification, then -1 is returned, if they are equal, 0, and
     * else 1 is returned.
     *
     * @param otherNotification the object to be compared.
     * @return integer representing which notification was computed earlier
     */
    @Override
    public int compareTo(Notification otherNotification) {
        return -this.currentShipDetails.getCurrentAISSignal().getTimestamp()
                .compareTo(otherNotification.currentShipDetails.getCurrentAISSignal().getTimestamp());
    }
}
