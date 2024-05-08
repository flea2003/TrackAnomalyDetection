package sp.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

@JsonSerialize
@Data
public class AnomalyInformation implements Serializable {
    private final Float score;
    private final String explanation;
    private final String correspondingTimestamp;
    private final String shipHash;
}
