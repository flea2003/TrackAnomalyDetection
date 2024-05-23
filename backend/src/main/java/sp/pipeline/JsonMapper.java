package sp.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import sp.utils.UtilsObjectMapper;

public class JsonMapper {
    /**
     * Converts a particular AnomalyInformation object to a JSON string.
     *
     * @return the respective JSON string
     */
    public static <T> String toJson(T object) throws JsonProcessingException {
        return new UtilsObjectMapper().writeValueAsString(object);
    }

    /**
     * Converts a JSON string to an AnomalyInformation object.
     *
     * @param val the JSON string to convert
     * @return the converted AISUpdate object
     */
    public static <T> T fromJson(String val, Class<T> classType) throws JsonProcessingException {
        return new UtilsObjectMapper().readValue(val, classType);
    }
}
