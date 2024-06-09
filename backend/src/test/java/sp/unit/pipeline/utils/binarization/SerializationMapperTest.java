package sp.unit.pipeline.utils.binarization;

import org.junit.jupiter.api.Test;
import sp.pipeline.utils.binarization.SerializationMapper;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SerializationMapperTest {

    @Test
    void constructorTest() {
        SerializationMapper mapper = new SerializationMapper();
        assertNotNull(mapper);
    }

    @Test
    void toAndFromJsonTest() throws IOException, ClassNotFoundException {
        String stringObject = "just a string";
        String str = SerializationMapper.toSerializedString(stringObject);
        assertEquals(stringObject, SerializationMapper.fromSerializedString(str, String.class));
    }

}
