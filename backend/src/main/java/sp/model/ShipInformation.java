package sp.model;

import lombok.*;
import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class ShipInformation implements Serializable {
    private long shipId;
    private AnomalyInformation anomalyInformation;
    private AISSignal aisSignal;
}
