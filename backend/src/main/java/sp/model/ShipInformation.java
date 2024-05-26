package sp.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class ShipInformation {
    private long shipId;
    private AnomalyInformation anomalyInformation;
    private AISSignal aisSignal;
}
