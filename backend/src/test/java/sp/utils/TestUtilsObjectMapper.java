package sp.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestUtilsObjectMapper {

    @Test
    void testConstructor() {
        assertThat(new UtilsObjectMapper()).isNotNull();
        assertThat(new UtilsObjectMapper().getRegisteredModuleIds()).isNotNull();
        assertThat(new UtilsObjectMapper().getRegisteredModuleIds().size()).isEqualTo(1);
        assertThat(new UtilsObjectMapper().getRegisteredModuleIds().iterator().next().toString()).isEqualTo("jackson-datatype-jsr310");
    }
}
