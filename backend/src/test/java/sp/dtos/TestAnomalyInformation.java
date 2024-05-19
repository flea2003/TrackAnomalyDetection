package sp.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    void testToJson() throws JsonProcessingException {
        assertThat(AnomalyInformation.fromJson(anomalyInformation.toJson())).isEqualTo(anomalyInformation);
    }

}
