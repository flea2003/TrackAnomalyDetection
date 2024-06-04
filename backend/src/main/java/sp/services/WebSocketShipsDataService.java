package sp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sp.model.CurrentShipDetails;
import sp.utils.websockets.WebSocketSessionManager;

@Service
public class WebSocketShipsDataService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WebSocketSessionManager webSocketSessionManager;

    /**
     * Constructor for the WebSocketShipsDataService class.
     *
     * @param simpMessagingTemplate SimpMessagingTemplate instance
     * @param webSocketSessionManager WebSocketSessionManager instance
     */
    @Autowired
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
