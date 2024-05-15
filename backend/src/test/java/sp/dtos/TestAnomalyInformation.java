package sp.dtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAnomalyInformation {
    AnomalyInformation anomalyInformation;

    @BeforeEach
    void setUp() {
        anomalyInformation = new AnomalyInformation(0.5F, "explanation", OffsetDateTime.of(2004, 01, 27, 1,1,0,0, ZoneOffset.ofHours(0)), 1L);
    }

    @Test
    void testToJson() {
        assertThat(anomalyInformation.toJson()).isEqualTo("{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"id\":1}");
    }

    @Test
    void testFromJson1() {
        assertThat(AnomalyInformation.fromJson(anomalyInformation.toJson())).isEqualTo(anomalyInformation);
    }

    @Test
    void testFromJson2() {
        assertThat(AnomalyInformation.fromJson("{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":\"2004-01-27T01:01:00Z\",\"id\":1}")).isEqualTo(anomalyInformation);
    }


}
