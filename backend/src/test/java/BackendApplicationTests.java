import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.Timestamp;

class BackendApplicationTests {
    @Test
    void test() throws JsonProcessingException {
        AISSignal signal = new AISSignal("ship1", 0.1F, 0.2F, 0.3F, 0.4F, 0.5F, new Timestamp("01/04/2015 20:19"), "sth2");
        String json = signal.toJson();
        System.out.println(json);
        int m = 2;

    }
}
