package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import sp.dtos.ExternalAISSignal;
import sp.pipeline.utils.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCurrentShipDetails {
    @Test
    void testGetSerdeIsSerde() {
        assertThat(CurrentShipDetails.getSerde()).isNotNull();
        assertThat(CurrentShipDetails.getSerde()).isExactlyInstanceOf(Jackson2Serde.class);
    }
}
