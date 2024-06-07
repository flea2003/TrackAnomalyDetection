package sp.integration.pipeline;

import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import sp.dtos.ExternalAISSignal;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.NotificationNotFoundException;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.Notification;
import sp.pipeline.utils.binarization.SerializationMapper;
import sp.pipeline.utils.json.JsonMapper;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class FullPipelineTest extends GenericPipelineTest {

    // Specify as a rule what type of Flink cluster to create. I.e.,
    // in this case, a simple Flink mini-cluster is created
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

        // Wait 3 seconds for the data to be processed
        Thread.sleep(3000);

        testSignalIDAssignment(fakeSignal);
        testFetchingFromService(fakeSignal);
        testSendingNonJsonMessages();
    }

    /**
     * Test whether ID assignment works properly. I.e., a simple signal was sent (in the calling method)
     * to raw ships topic, and now we check if the AIS topic contains the identified signals
     *
     * @param sentSignal the raw signal that was sent
     * @throws Exception if something fails
     */
    void testSignalIDAssignment(ExternalAISSignal sentSignal) throws Exception {
        // Get the strings produced to the identified ships topic
        List<String> list = getItemsFromTopic(currentShipDetailsTopic, 2, 5);

        // Calculate the expected AIS signal
        long expectedID = 1098835837;

        // Deserialize the received AIS signal
        CurrentShipDetails receivedDetails = JsonMapper.fromJson(list.get(1), CurrentShipDetails.class);

        // Make sure they are equal (ignoring received time)
        assertEquals(receivedDetails.extractId(), expectedID);
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
        List<String> messages = List.of(
                "non json trash",
                "some other trash",
                "{a: b}",
                "{\"trashKey\": \"trashValueNoBracket\""
        );

        produceToTopic(rawAISTopic, messages);
        produceToTopic(notificationsTopic, messages);
        produceToTopic(currentShipDetailsTopic, messages);

        // Wait for 5 seconds to be fully sure
        Thread.sleep(5000);

        // Make sure that after this, the there still is only 1 ship
        assertThat(shipsDataService.getCurrentShipDetails().size()).isEqualTo(1);

        // Send some new proper signals to the raw ships topic (to make sure the pipeline is still working)
        ExternalAISSignal fakeSignal = new ExternalAISSignal("newProducer", "hash", 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")), "port");
        produceToTopic(rawAISTopic, List.of(JsonMapper.toJson(fakeSignal)));

        // Wait 10 seconds for the data to be processed
        Thread.sleep(10000);

        // Make sure that after this, the there still are now 2 ships (i.e., the pipeline has not crashed)
        assertThat(shipsDataService.getCurrentShipDetails().size()).isEqualTo(2);

    }

    /**
     * Simulates some ships behaving in an anomalous way and asserts their behaviour.
     */
    @Test
    void testAnomalousShips() throws Exception {
        setupPipelineComponentsAndRun();

        testForOneVeryAnomalousShipAndOneNot();
    }

    /**
     * Performs a test where one of the ships is anomalous, but since it sends only one signal, it is not considered
     * anomalous. And the second ship is anomalous for 2 reasons: strange speed and too large interval between signals.
     * In doing so, this particular test also tests:
     *      - notifications
     *      - ID assignment
     *      - anomalous behaviour calculation
     *      - getting individual ship details
     * @throws Exception in case something goes wrong
     */
    void testForOneVeryAnomalousShipAndOneNot() throws Exception {

        // Create 2 different ships with anomalous speeds. The first and third signals are from the same ship and the second
        // is from another. Additionally, the first 2 ships have the same shipHash (but different producer ID),
        // so we are also testing if ID assignment works.
        List<ExternalAISSignal> signals = List.of(
                new ExternalAISSignal("producer1", "1", 80f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")), "port"),
                new ExternalAISSignal("producer2", "1", 100f, 0.1f, 0.1f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")), "port"),
                new ExternalAISSignal("producer1", "1", 80f, 10f, 10f, 0.1f, 0.1f, java.time.OffsetDateTime.now(ZoneId.of("Z")).plusMinutes(31), "port")
        );

        List<String> messages = new ArrayList<>();
        for (ExternalAISSignal signal : signals) {
            String json = JsonMapper.toJson(signal);
            messages.add(json);
        }

        // Send the ship signals
        produceToTopic(rawAISTopic, messages);

        // Wait 5 seconds to make sure they pass through
        Thread.sleep(10000);

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

        // Make sure that getting some random ID that does not exist throws an exception
        long badID = expectedIDShip1 + expectedIDShip2 + 1;
        assertThrows(NotExistingShipException.class, () -> shipsDataService.getIndividualCurrentShipDetails(badID));

        float score1 = details.get(0).getCurrentAnomalyInformation().getScore();
        float score2 = details.get(1).getCurrentAnomalyInformation().getScore();

        // Assert that the first one is an anomaly (since 2 signals were sent, and it is fast)
        // And the second one is not an anomaly, since only 1 signal was sent
        assertThat(score1).isGreaterThan(0);
        assertThat(score2).isEqualTo(0);

        // Assert max anomaly scores match current anomaly scores
        assertThat(details.get(0).getCurrentAnomalyInformation().getScore()).isEqualTo(details.get(0).getMaxAnomalyScoreInfo().getMaxAnomalyScore());
        assertThat(details.get(1).getCurrentAnomalyInformation().getScore()).isEqualTo(details.get(1).getMaxAnomalyScoreInfo().getMaxAnomalyScore());

        // Make sure that a single notification was added to the notification repository
        assertThat(notificationService.getAllNotifications().size()).isEqualTo(1);
        assertThat(notificationService.getAllNotificationForShip(expectedIDShip1).size()).isEqualTo(1);

        // Test getting that notification by ID
        Notification expected = notificationService.getAllNotificationForShip(expectedIDShip1).get(0);
        Notification received = notificationService.getNotificationById(expected.getId());
        assertThat(expected).isEqualTo(received);

        // Get some random notification with random ID that does not exist and make sure that exception is thrown
        assertThrows(NotificationNotFoundException.class, () -> notificationService.getNotificationById(expected.getId()-1));
    }
}
