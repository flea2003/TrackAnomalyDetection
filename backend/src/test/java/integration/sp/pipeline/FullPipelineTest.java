package integration.sp.pipeline;

import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import sp.dtos.ExternalAISSignal;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.pipeline.utils.json.JsonMapper;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FullPipelineTest extends GenericPipelineTest {

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(2)
                            .setNumberTaskManagers(1)
                            .build());
    @Test
    void testKafkaProducerAndConsumer() throws Exception {
        setupPipelineComponentsAndRun();

        // Send a message to the raw ships topic
        ExternalAISSignal fakeSignal = new ExternalAISSignal("producer", "hash", 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")), "port");
        produceToTopic(rawAISTopic, List.of(JsonMapper.toJson(fakeSignal)));

        testSignalIDAssignment(fakeSignal);
        testFetchingFromService(fakeSignal);
        testSendingNonJsonMessages();
    }

    /**
     * Test whether ID assignment works properly. I.e., a simple signal was sent (in the calling method)
     * to raw ships topic and now we check if the AIS topic contains the identified signals
     *
     * @param sentSignal the raw signal that was sent
     * @throws Exception if something fails
     */
    void testSignalIDAssignment(ExternalAISSignal sentSignal) throws Exception {
        // Get the strings produced to the identified ships topic
        List<String> list = getItemsFromTopic(identifiedAISTopic, 1, 5);

        // Calculate the expected AIS signal
        long expectedID = 1098835837;
        AISSignal expectedAISSignal = new AISSignal(sentSignal, expectedID);

        // Deserialize the received AIS signal
        AISSignal aisSignal = JsonMapper.fromJson(list.get(0), AISSignal.class);

        // Make sure they are equal (ignoring received time)
        assertEquals(expectedAISSignal, aisSignal);
    }

    /**
     * Tests whether the single signal that was sent (in the calling method) can
     * be accessible by the service. The test checks if the score is indeed 0 (since
     * a single signal should not be anomalous in this case).
     *
     * @param sentSignal the raw signal that was sent
     * @throws Exception if something fails
     */
    void testFetchingFromService(ExternalAISSignal sentSignal) throws Exception {
        // Wait 5 seconds for the data to be processed
        Thread.sleep(5000);

        // Get the details of all ships
        List<CurrentShipDetails> allDetails = shipsDataService.getCurrentShipDetails();
        assertThat(allDetails.size()).isEqualTo(1);

        CurrentShipDetails details = allDetails.get(0);

        // Assert that IDs match
        long expectedID = 1098835837;
        assertThat(details.getCurrentAISSignal().getId()).isEqualTo(expectedID);

        // Asser that the AIS details match
        assertThat(details.getCurrentAISSignal().getLatitude()).isEqualTo(sentSignal.getLatitude());
        assertThat(details.getCurrentAISSignal().getLongitude()).isEqualTo(sentSignal.getLongitude());
        assertThat(details.getCurrentAISSignal().getSpeed()).isEqualTo(sentSignal.getSpeed());
        assertThat(details.getCurrentAISSignal().getCourse()).isEqualTo(sentSignal.getCourse());

        // Assert that the anomaly information is correct
        assertThat(details.getCurrentAnomalyInformation().getScore()).isEqualTo(0);
    }

    /**
     * After sending that one single signal, also send some trash signals to all topics.
     * Make sure the system does not crash and the current state still contains exactly 1
     * ship.
     *
     * @throws Exception in case something fails
     */
    void testSendingNonJsonMessages() throws Exception {
        // Send some trash messages to all 3 topics
        List<String> messages = List.of("non json trash", "some other trash", "a");
        produceToTopic(rawAISTopic, messages);
        produceToTopic(identifiedAISTopic, messages);
        produceToTopic(scoresTopic, messages);

        // Wait for 5 seconds to be fully sure
        Thread.sleep(5000);

        // Make sure that after this, the pipeline has not crashed and there still is only 1 ship
        assertThat(shipsDataService.getCurrentShipDetails().size()).isEqualTo(1);
    }
}
