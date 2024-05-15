package sp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sp.dtos.ExternalAISSignal;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@SpringBootApplication
public class BackendApplication {

    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    /**
     * Main method. Starts the application.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);

        log.info("Application running");
    }

}

