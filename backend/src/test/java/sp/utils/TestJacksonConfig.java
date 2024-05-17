package sp.utils;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestJacksonConfig {

    JacksonConfig jacksonConfig = new JacksonConfig();

    @Test
    void testConstructor() {
        MappingJackson2HttpMessageConverter result =  jacksonConfig.mappingJackson2HttpMessageConverter();

        assertThat(result).isNotNull();
        assertThat(result).isExactlyInstanceOf(MappingJackson2HttpMessageConverter.class);
        assertThat(result.getObjectMapper().getRegisteredModuleIds().size()).isEqualTo(1);
        assertThat(result.getObjectMapper().getRegisteredModuleIds().iterator().next().toString()).isEqualTo("jackson-datatype-jsr310");
    }
}
