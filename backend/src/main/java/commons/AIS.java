package commons;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AIS {

    @Id
    private String hash;

    @ManyToOne
    private Ship ship;
    private float speed;
    private float lon;
    private float lat;
    private int course;
    private int heading;
    private String timeStamp;
    private String departurePortName;
}
