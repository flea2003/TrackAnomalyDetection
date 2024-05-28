package sp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sp.model.CurrentShipDetails;
import sp.utils.WebSocketSessionManager;

@Service
public class WebSocketShipDataService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WebSocketSessionManager webSocketSessionManager;

    @Autowired
    public WebSocketShipDataService(SimpMessagingTemplate simpMessagingTemplate,
                                    WebSocketSessionManager webSocketSessionManager) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.webSocketSessionManager = webSocketSessionManager;
    }

    public void sendCurrentShipDetails(CurrentShipDetails payload) {
        System.out.println("sending message");
        this.simpMessagingTemplate.convertAndSend("/topic/details", payload);
    }

    public boolean checkForOpenConnections() {
        return this.webSocketSessionManager.checkForOpenConnections();
    }
}
