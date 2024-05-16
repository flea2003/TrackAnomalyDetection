package sp.dtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAnomalyInformation {
    AnomalyInformation anomalyInformation;

    @BeforeEach
    void setUp() {
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", "12/12/12", "hash");
    }

    @Test
    void testToJson() {
        assertThat(anomalyInformation.toJson()).isEqualTo("{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"hash\"}");
    }

    @Test
    void testFromJson1() {
        assertThat(AnomalyInformation.fromJson(anomalyInformation.toJson())).isEqualTo(anomalyInformation);
    }

    @Test
    void testFromJson2() {
        assertThat(AnomalyInformation.fromJson("{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"hash\"}")).isEqualTo(anomalyInformation);
    }

    @Test
    void testFromJSONWithException() {
        // this JSON has field "scoreRR"
        String badJson = "{\"scoreRR\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"12/12/12\",\"shipHash\":\"hash\"}";
        assertThrows(RuntimeException.class, () -> AnomalyInformation.fromJson(badJson));
    }

}
