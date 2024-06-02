package sp.pipeline.parts.websockets;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.stereotype.Component;
import sp.model.CurrentShipDetails;
import sp.services.WebSocketShipsDataService;

@Component
public class WebSocketBroadcasterBuilder {

    private final WebSocketShipsDataService webSocketShipsDataService;

    public WebSocketBroadcasterBuilder(WebSocketShipsDataService webSocketShipsDataService) {
        this.webSocketShipsDataService = webSocketShipsDataService;
    }

    public void enableBroadcasting(KTable<Long, CurrentShipDetails> currentShipDetails) {
        KStream<Long, CurrentShipDetails> aggregatedStream = currentShipDetails.toStream();
        aggregatedStream.foreach((key, value) -> {
            if (this.webSocketShipsDataService.checkForOpenConnections()) {
                this.webSocketShipsDataService.sendCurrentShipDetails(value);
            }
        });
    }
}
