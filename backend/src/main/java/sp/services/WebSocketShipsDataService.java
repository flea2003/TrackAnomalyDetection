package sp.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import sp.model.CurrentShipDetails;
import sp.utils.websockets.WebSocketSessionManager;

public class WebSocketShipsDataService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WebSocketSessionManager webSocketSessionManager;

    /**
     * Constructor for the WebSocketShipsDataService class.
     *
     * @param simpMessagingTemplate SimpMessagingTemplate instance
     * @param webSocketSessionManager WebSocketSessionManager instance
     */
    public WebSocketShipsDataService(SimpMessagingTemplate simpMessagingTemplate,
                                     WebSocketSessionManager webSocketSessionManager) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.webSocketSessionManager = webSocketSessionManager;
    }

    /**
     * Leveraging the SIMP template, broadcast the new CurrentShipDetails payload.
     * The payload is sent to all the subscribed clients.
     *
     * @param payload latest CurrentShipDetails instance
     */
    public void sendCurrentShipDetails(CurrentShipDetails payload) {
        this.simpMessagingTemplate.convertAndSend("/topic/details", payload);
    }

    /**
     * Utility method for checking the existence of connected frontend clients.
     *
     * @return boolean flag indicating presence/absence of WebSocket connections
     */
    public boolean checkForOpenConnections() {
        return this.webSocketSessionManager.checkForOpenConnections();
    }
}
