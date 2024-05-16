package sp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class UtilsObjectMapper extends ObjectMapper {
    public UtilsObjectMapper() {
        super();
        this.registerModule(new JavaTimeModule());
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
