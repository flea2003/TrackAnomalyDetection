package sp.model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
public class TestCurrentShipDetails {
    @Test
    void test() {
        assertThat(CurrentShipDetails.getSerde()).isNotNull();
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        assertThat(CurrentShipDetails.getSerde()).isExactlyInstanceOf(Jackson2Serde.class);
    }
}
