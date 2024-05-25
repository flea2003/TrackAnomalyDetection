package sp.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestExternalAISSignal {
    @Test
    void testSerializationMethodNoProducer() throws JsonProcessingException {
        ExternalAISSignal externalAISSignal = new ExternalAISSignal(null, "123", 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0)), "New York");
        String json = externalAISSignal.toJson();
        ExternalAISSignal deserialized = ExternalAISSignal.fromJson(json);
        assertThat(externalAISSignal).isEqualTo(deserialized);
    }

    @Test
    void testSerializationMethodWithProducer() throws JsonProcessingException {
        ExternalAISSignal externalAISSignal = new ExternalAISSignal("producer", "123", 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0)), "New York");
        String json = externalAISSignal.toJson();
        ExternalAISSignal deserialized = ExternalAISSignal.fromJson(json);
        assertThat(externalAISSignal).isEqualTo(deserialized);
    }
}
