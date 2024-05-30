package sp.integration.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.hash.HashCode;
import sp.dtos.ExternalAISSignal;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.Notification;
import sp.pipeline.utils.json.JsonMapper;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class FullPipelineTest extends GenericPipelineTest {

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(2)
                            .setNumberTaskManagers(1)
                            .build());

    /**
     * This test sends a single (very simple) AIS signal to the system. It checks if that signal had an ID assigned
     * and if the final score for that ship is accessible in the service.
     * Additionally, some trash strings is sent to all topics to check if the system can handle non-json items being sent.
     * @throws Exception in case anything fails. Should not be thrown if all goes well
     */
    @Test
    void testSimpleFlow() throws Exception {
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

    /**
     * Simulates some ships behaving in an anomalous way and asserts their behaviour.
     * In doing so, also tests:
     *      - Ship ID calculation
     *      - Getting all current ship details from a service
     *      - Getting individual current ship details from a service
     *      - Notifications: if score is above something, if a notification was sent or was not sent
     */
    @Test
    void testAnomalousShips() throws Exception {
        setupPipelineComponentsAndRun();
        Thread.sleep(5000);

        testSingleShipBadSpeed();
    }

    void testSingleShipBadSpeed() throws Exception {

        // Make sure that notificationRepository does nothing when something is added (Mockito)
        Notification fakeNotification = new Notification();
        when(notificationRepository.save(any())).thenReturn(fakeNotification);

        // Create 2 different ships with anomalous speeds. The first and third signals are from the same ship and the second
        // is from antoher. Additionally, the first 2 ships have the same shipHash (but different producer ID),
        // so we are also testing if ID assignment works
        List<ExternalAISSignal> signals = List.of(
                new ExternalAISSignal("producer1", "1", 80f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")), "port"),
                new ExternalAISSignal("producer2", "1", 100f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")), "port"),
                new ExternalAISSignal("producer1", "1", 80f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")).plusMinutes(1), "port")
        );

        List<String> messages = new ArrayList<>();
        for (ExternalAISSignal signal : signals) {
            String json = JsonMapper.toJson(signal);
            messages.add(json);
        }

        // Send the ship signals
        produceToTopic(rawAISTopic, messages);

        // Wait 5 seconds to make sure they pass through
        Thread.sleep(5000);

        // Get the details
        List<CurrentShipDetails> details = shipsDataService.getCurrentShipDetails();

        // Extract the ships
        assertThat(details.size()).isEqualTo(2);

        long expectedIDShip1 = Objects.hash("producer1", "1") & 0x7FFFFFFF;
        long expectedIDShip2 = Objects.hash("producer2", "1") & 0x7FFFFFFF;

        // Keep in mind that ships can come in any order
        if (details.get(0).getCurrentAISSignal().getId() == expectedIDShip1) {
            assertThat(details.get(1).getCurrentAISSignal().getId()).isEqualTo(expectedIDShip2);
        } else {
            assertThat(details.get(0).getCurrentAISSignal().getId()).isEqualTo(expectedIDShip2);
            assertThat(details.get(1).getCurrentAISSignal().getId()).isEqualTo(expectedIDShip1);
            // Swap the elements in the list
            details = List.of(details.get(1), details.get(0));
        }

        // Now test getIndividualCurrentShipDetails, assert that the elements returned by the method match the ones
        // returned by the one that returns all ship details
        CurrentShipDetails individualDetailsShip1 = shipsDataService.getIndividualCurrentShipDetails(expectedIDShip1);
        CurrentShipDetails individualDetailsShip2 = shipsDataService.getIndividualCurrentShipDetails(expectedIDShip2);
        assertThat(individualDetailsShip1).isEqualTo(details.get(0));
        assertThat(individualDetailsShip2).isEqualTo(details.get(1));

        float score1 = details.get(0).getCurrentAnomalyInformation().getScore();
        float score2 = details.get(1).getCurrentAnomalyInformation().getScore();

        // Assert that the first one is an anomaly (since 2 signals were sent and it is fast)
        // And the second one is not an anomaly, since only 1 signal was sent
        assertThat(score1).isGreaterThan(0);
        assertThat(score2).isEqualTo(0);

        // Assert max anomaly scores match current anomaly scores
        assertThat(details.get(0).getCurrentAnomalyInformation().getScore()).isEqualTo(details.get(0).getMaxAnomalyScoreInfo().getMaxAnomalyScore());
        assertThat(details.get(1).getCurrentAnomalyInformation().getScore()).isEqualTo(details.get(1).getMaxAnomalyScoreInfo().getMaxAnomalyScore());

        // Make sure that a single notification was added to the notification repository
        verify(notificationRepository, times(1)).save(any());

        // Reset the mock (would not be needed if we did not cramp multiple tests into one)
        reset(notificationRepository);
    }
}
