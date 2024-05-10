import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sp.model.AISSignal;

class BackendApplicationTests {
    @Test
    void test() throws JsonProcessingException {
        AISSignal signal = new AISSignal("ship1", 0.1F, 0.2F, 0.3F, 0.4F, 0.5F, "sth1", "sth2");
        String json = signal.toJson();
        System.out.println(json);
        int m = 2;

    }
}
