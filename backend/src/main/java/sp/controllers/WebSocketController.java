package sp.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import sp.model.CurrentShipDetails;

@Controller
public class WebSocketController {
    @MessageMapping("/currentShipDetails")
    @SendTo("/topic/details")
    public CurrentShipDetails sendCurrentShipDetails(CurrentShipDetails currentShipDetails) {
        return currentShipDetails;
    }
}
