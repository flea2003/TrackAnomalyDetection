package unit.sp.model;

import com.giladam.kafka.jacksonserde.Jackson2Serde;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sp.model.CurrentShipDetails;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCurrentShipDetails {

    @Test
    void testGetSerdeIsSerde() {
        Assertions.assertThat(CurrentShipDetails.getSerde()).isNotNull();
        assertThat(CurrentShipDetails.getSerde()).isExactlyInstanceOf(Jackson2Serde.class);
    }
}
