package integration.sp.pipeline;

import org.junit.jupiter.api.Test;
import sp.dtos.ExternalAISSignal;
import sp.model.AISSignal;
import sp.pipeline.utils.json.JsonMapper;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FullPipelineTest extends GenericPipelineTest {

    @Test
    void testKafkaProducerAndConsumer() throws ExecutionException, InterruptedException, IOException {
        setupPipelineComponents();
        anomalyDetectionPipeline.runPipeline();
        Thread.sleep(5000);

        // Send a message to the raw ships topic
        ExternalAISSignal fakeSignal = new ExternalAISSignal("producer", "hash", 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")), "port");
        produceToTopic(rawAISTopic, List.of(JsonMapper.toJson(fakeSignal)));

        // Get the strings produced to the identified ships topic
        List<String> list = getItemsFromTopic(identifiedAISTopic, 1, 5);

        // Calculate the expected AIS signal
        long expectedID = 1098835837;
        AISSignal expectedAISSignal = new AISSignal(fakeSignal, expectedID);

        // Deserialize the received AIS signal
        AISSignal aisSignal = JsonMapper.fromJson(list.get(0), AISSignal.class);

        // Make sure they are equal (ignoring received time)
        assertEquals(expectedAISSignal, aisSignal);
    }

}
