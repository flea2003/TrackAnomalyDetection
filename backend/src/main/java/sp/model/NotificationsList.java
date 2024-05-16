package sp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@EqualsAndHashCode
@ToString
@Setter
@NoArgsConstructor
public class NotificationsList {

    @Id
    private String shipHash;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ShipInformation> notifications;

    public NotificationsList(String shipHash, List<ShipInformation> notifications) {
        this.shipHash = shipHash;
        this.notifications = notifications;
    }

    public void addNotification(ShipInformation shipInformation) {
        notifications.add(shipInformation);
    }

}
