package integrationtests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import sp.BackendApplication;
import sp.dtos.AISSignal;
import sp.exceptions.PipelineException;
import sp.pipeline.AnomalyDetectionPipeline;
import sp.pipeline.StreamUtils;
import sp.pipeline.scorecalculators.DefaultScoreCalculator;
import sp.pipeline.scorecalculators.ScoreCalculationStrategy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
@DirtiesContext
@EmbeddedKafka(
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"},
        partitions = 2
)
class AnomalyDetectionPipelineTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AnomalyDetectionPipeline anomalyDetectionPipeline;

    @Test
    void test() throws InterruptedException, PipelineException {
        Thread.sleep(6000); // 6 s

        AISSignal signal = new AISSignal("ship1", 1, 2, 3, 4, 5, "t1", "port1");
        kafkaTemplate.send("ships-AIS", signal.toJson());

        Thread.sleep(6000); // sleep for 6s (booo... flaky tests...)

        assertEquals(new HashMap<>(), anomalyDetectionPipeline.getCurrentScores());
    }

}