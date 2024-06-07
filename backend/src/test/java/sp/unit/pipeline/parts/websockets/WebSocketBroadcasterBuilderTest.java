package sp.unit.pipeline.parts.websockets;

import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sp.model.CurrentShipDetails;
import sp.pipeline.parts.websockets.WebSocketBroadcasterBuilder;
import sp.services.WebSocketShipsDataService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebSocketBroadcasterBuilderTest {

    WebSocketShipsDataService webSocketShipsDataService;
    WebSocketBroadcasterBuilder webSocketBroadcasterBuilder;

    @BeforeEach
    void setUp() {
        this.webSocketShipsDataService = mock(WebSocketShipsDataService.class);
        this.webSocketBroadcasterBuilder = new WebSocketBroadcasterBuilder(webSocketShipsDataService);
    }

    @Test
    public void testBroadcastingClosedConnections() {
        Mockito.when(this.webSocketShipsDataService.checkForOpenConnections()).thenReturn(false);
        KTable<Long, CurrentShipDetails> currentState = mock(KTable.class);
        KStream<Long, CurrentShipDetails> currentStream = mock(KStream.class);

        ArgumentCaptor<ForeachAction<Long, CurrentShipDetails>> foreachActionCaptor = ArgumentCaptor.forClass(ForeachAction.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                ForeachAction<Long, CurrentShipDetails> action = invocationOnMock.getArgument(0);
                action.apply(1L, new CurrentShipDetails());
                return null;
            }
        }).when(currentStream).foreach(foreachActionCaptor.capture());

        Mockito.when(currentState.toStream()).thenReturn(currentStream);

        webSocketBroadcasterBuilder.buildWebSocketBroadcastingPart(currentState);

        verify(webSocketShipsDataService, never()).sendCurrentShipDetails(any(CurrentShipDetails.class));

    }

    @Test
    public void testBroadcastingOpenConnections() {
        Mockito.when(this.webSocketShipsDataService.checkForOpenConnections()).thenReturn(true);
        KTable<Long, CurrentShipDetails> currentState = mock(KTable.class);
        KStream<Long, CurrentShipDetails> currentStream = mock(KStream.class);

        ArgumentCaptor<ForeachAction<Long, CurrentShipDetails>> foreachActionCaptor = ArgumentCaptor.forClass(ForeachAction.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                ForeachAction<Long, CurrentShipDetails> action = invocationOnMock.getArgument(0);
                action.apply(1L, new CurrentShipDetails());
                return null;
            }
        }).when(currentStream).foreach(foreachActionCaptor.capture());

        Mockito.when(currentState.toStream()).thenReturn(currentStream);

        webSocketBroadcasterBuilder.buildWebSocketBroadcastingPart(currentState);

        verify(webSocketShipsDataService).sendCurrentShipDetails(any(CurrentShipDetails.class));
    }





}
