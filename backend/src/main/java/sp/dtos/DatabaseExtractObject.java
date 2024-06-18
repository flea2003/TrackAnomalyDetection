package sp.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import sp.model.AISSignal;

@Getter
@Setter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonSerialize
public class DatabaseExtractObject {
    private final AISSignal aisSignal;
    private final Float anomalyScore;
}
