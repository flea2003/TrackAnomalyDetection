import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class BackendApplication {

    private final static Logger log = LoggerFactory.getLogger(BackendApplication.class);

    /**
     *
     *
     * @param args arguments
     */
    public static void main(String[] args) {

        SpringApplication.run(BackendApplication.class, args);

        log.info("Application running");
    }

}

