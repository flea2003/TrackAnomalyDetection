package sp.unit.utils;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import sp.utils.UtilsObjectMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestUtilsObjectMapper {

    @Test
    void testConstructor() {
        AssertionsForClassTypes.assertThat(new UtilsObjectMapper()).isNotNull();
        assertThat(new UtilsObjectMapper().getRegisteredModuleIds()).isNotNull();
        assertThat(new UtilsObjectMapper().getRegisteredModuleIds().size()).isEqualTo(1);
        assertThat(new UtilsObjectMapper().getRegisteredModuleIds().iterator().next().toString()).isEqualTo("jackson-datatype-jsr310");
    }
}
