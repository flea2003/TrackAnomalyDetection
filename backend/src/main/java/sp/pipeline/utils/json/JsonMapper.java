package sp.pipeline.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sp.utils.UtilsObjectMapper;

public class JsonMapper {
    private static final ObjectMapper mapper = new UtilsObjectMapper();

    /**
     * Converts a particular AnomalyInformation object to a JSON string.
     *
     * @param object the object to be converted to JSON
     * @param <T> the type of the object
     * @return the respective JSON string
     */
    public static <T> String toJson(T object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    /**
     * Converts a JSON string to an AnomalyInformation object.
     *
     * @param val the JSON string to convert
     * @param classType the java class representing the class for the resulting object
     * @param <T> the type of the resulting object
     * @return the converted AISUpdate object
     */
    public static <T> T fromJson(String val, Class<T> classType) throws JsonProcessingException {
        return mapper.readValue(val, classType);
    }
}
