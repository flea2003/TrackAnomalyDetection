package commons;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AnomalyInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private float score;
    @ManyToOne
    private Ship ship;
    private String info;

    /**
     * Constructor
     *
     * @param score anomaly score
     * @param info anomaly explanation
     */
    public AnomalyInformation(float score, String info) {
        this.score = score;
        this.info = info;
    }

}
