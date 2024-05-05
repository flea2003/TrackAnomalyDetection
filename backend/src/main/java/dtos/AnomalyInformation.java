package dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@JsonSerialize
@Data
public class AnomalyInformation {
    private final Float score;
    private final String shipHash;
}
