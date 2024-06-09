package sp.unit.pipeline.parts.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.model.MaxAnomalyScoreDetails;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.parts.websockets.WebSocketExtractor;
import sp.pipeline.utils.StreamUtils;
import sp.pipeline.utils.json.JsonMapper;
import sp.services.WebSocketShipsDataService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebSocketExtractorTest {

    WebSocketShipsDataService webSocketShipsDataService;
    WebSocketExtractor webSocketExtractor;

    @BeforeEach
    void setUp() {
        this.webSocketShipsDataService = mock(WebSocketShipsDataService.class);
        webSocketExtractor = new WebSocketExtractor(webSocketShipsDataService);
    }

    @Test
    void testRealConstructor() {
        StreamUtils streamUtils = mock(StreamUtils.class);
        PipelineConfiguration configuration = mock(PipelineConfiguration.class);
        assertDoesNotThrow(() -> new WebSocketExtractor(streamUtils, configuration, webSocketShipsDataService));
    }

    @Test
    void testNoOpenConnections() {
        when(webSocketShipsDataService.checkForOpenConnections()).thenReturn(false);
        // Make sure that NullPointerException is not thrown (since record is null, but it should return early)
        assertDoesNotThrow(() -> webSocketExtractor.processNewRecord(null));
    }

    @Test
    void testJsonProcessingException() {
        when(webSocketShipsDataService.checkForOpenConnections()).thenReturn(true);
        ConsumerRecord<Long, String> fakeRecord = mock(ConsumerRecord.class);
        when(fakeRecord.value()).thenReturn("invalid json value");
        assertDoesNotThrow(() -> webSocketExtractor.processNewRecord(fakeRecord));
    }

    @Test
    void testValidRecord() throws JsonProcessingException {
        when(webSocketShipsDataService.checkForOpenConnections()).thenReturn(true);
        OffsetDateTime timestamp = OffsetDateTime.now().atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        CurrentShipDetails fakeDetails = new CurrentShipDetails(
                new AnomalyInformation(12, "expl", timestamp, 1l),
                new AISSignal(),
                new MaxAnomalyScoreDetails(1.0f, timestamp)
        );
        ConsumerRecord<Long, String> fakeRecord = mock(ConsumerRecord.class);
        when(fakeRecord.value()).thenReturn(JsonMapper.toJson(fakeDetails));
        doNothing().when(webSocketShipsDataService).sendCurrentShipDetails(any());

        // Send the record
        webSocketExtractor.processNewRecord(fakeRecord);

        // Make sure that the sendCurrentShipDetails method was called with the correct argument
        verify(webSocketShipsDataService, times(1)).sendCurrentShipDetails(fakeDetails);
    }
}
