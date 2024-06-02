package sp.pipeline.utils.binarization;

import java.io.*;
import java.util.Base64;

public class SerializationMapper {

    /**
     * Converts a particular AnomalyInformation object to a binary-encoded string.
     *
     * @param object the object to be converted to binary-encoded string
     * @param <T> the type of the object
     * @return the respective binary-encoded string
     */
    public static <T> String toSerializedString(T object) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            byte[] byteArray = baos.toByteArray();
            return Base64.getEncoder().encodeToString(byteArray);
        }
    }

    /**
     * Converts a binary-encoded string to an AnomalyInformation object.
     *
     * @param val the binary-encoded string to convert
     * @param classType the java class representing the class for the resulting object
     * @param <T> the type of the resulting object
     * @return the converted AISUpdate object
     */
    public static <T> T fromSerializedString(String val, Class<T> classType) throws IOException, ClassNotFoundException {
        byte[] byteArray = Base64.getDecoder().decode(val);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteArray))) {
            return (T) ois.readObject();
        }
    }
}
