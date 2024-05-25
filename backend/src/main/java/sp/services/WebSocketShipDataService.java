package sp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sp.model.CurrentShipDetails;

@Service
public class WebSocketShipDataService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public WebSocketShipDataService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendCurrentShipDetails(CurrentShipDetails payload) {
        this.simpMessagingTemplate.convertAndSend("/topic/details", payload);
    }
}
