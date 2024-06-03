package sp.unit.pipeline.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import sp.pipeline.utils.json.JsonMapper;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JsonMapperTest {

    @Test
    void constructorTest() {
        JsonMapper jsonMapper = new JsonMapper();
        assertNotNull(jsonMapper);
    }

    @Test
    void toAndFromJsonTest() throws JsonProcessingException {
        String stringObject = "just a string";

        String json = JsonMapper.toJson(stringObject);
        assertEquals(stringObject, JsonMapper.fromJson(json, String.class));
    }

}