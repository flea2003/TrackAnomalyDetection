package sp.model;

import com.giladam.kafka.jacksonserde.Jackson2Serde;
import jakarta.persistence.*;
import lombok.*;
import org.apache.kafka.common.serialization.Serde;
import sp.utils.UtilsObjectMapper;
import java.io.Serializable;

@Entity
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Notification implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long shipID;
    private boolean isRead;

    @Column(length = 2048)
    private final CurrentShipDetails currentShipDetails;


    /**
     * Constructor for the notifications object.
     *
     * @param currentShipDetails CurrentShip details object
     */
    public Notification(CurrentShipDetails currentShipDetails) {
        this.currentShipDetails = currentShipDetails;
        this.isRead = false;
        this.shipID = currentShipDetails.getCurrentAnomalyInformation().getId();
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
}
