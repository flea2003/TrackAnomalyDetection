package sp.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import sp.model.CurrentShipDetails;
import sp.services.WebSocketShipsDataService;
import sp.utils.websockets.WebSocketSessionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class WebSocketsShipsDataServiceTest {
    WebSocketShipsDataService webSocketShipsDataService;
    SimpMessagingTemplate simpMessagingTemplate;
    WebSocketSessionManager webSocketSessionManager;

    @BeforeEach
    void setUp() {
        webSocketSessionManager = mock(WebSocketSessionManager.class);
        simpMessagingTemplate = mock(SimpMessagingTemplate.class);
        webSocketShipsDataService = new WebSocketShipsDataService(simpMessagingTemplate, webSocketSessionManager);
    }

    @Test
    public void testCheckForOpenConnections() {
        when(webSocketSessionManager.checkForOpenConnections()).thenReturn(true);
        assertThat(webSocketShipsDataService.checkForOpenConnections()).isTrue();
    }

    @Test
    public void sendShipDetails() {
        CurrentShipDetails currentShipDetails = new CurrentShipDetails();
        webSocketShipsDataService.sendCurrentShipDetails(currentShipDetails);
        verify(simpMessagingTemplate).convertAndSend("/topic/details", currentShipDetails);
    }

}
