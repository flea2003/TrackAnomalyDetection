package sp.model;

import com.giladam.kafka.jacksonserde.Jackson2Serde;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TestCurrentShipDetails {

    @Test
    void testGetSerdeIsSerde() {
        assertThat(CurrentShipDetails.getSerde()).isNotNull();
        assertThat(CurrentShipDetails.getSerde()).isExactlyInstanceOf(Jackson2Serde.class);
    }
}
