package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import org.junit.jupiter.api.Test;
import sp.model.AnomalyInformation;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCurrentShipDetails {

    @Test
    void testGetSerdeIsSerde() {
        assertThat(CurrentShipDetails.getSerde()).isNotNull();
        assertThat(CurrentShipDetails.getSerde()).isExactlyInstanceOf(Jackson2Serde.class);
    }

    @Test
    void testJsonSerialize() throws JsonProcessingException {
        CurrentShipDetails result = new CurrentShipDetails(
                new AnomalyInformation(1F, "explanation", null, 1L),
                new AISSignal(1L, 0F, 0F, 0F, 0F, 0F, null, "KLAIPEDA"),
                new MaxAnomalyScoreDetails()
        );

        assertThat(CurrentShipDetails.fromJson(result.toJson())).isEqualTo(result);
    }
}
