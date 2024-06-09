package sp.pipeline.parts.websockets;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sp.model.CurrentShipDetails;
import sp.services.WebSocketShipsDataService;

@Component
public class WebSocketBroadcasterBuilder {

    private final WebSocketShipsDataService webSocketShipsDataService;

    /**
     * Constructor for the WebSocketBroadcasterBuilder class.
     *
     * @param webSocketShipsDataService injected WebSockets broadcasting service
     */
    @Autowired
    public WebSocketBroadcasterBuilder(WebSocketShipsDataService webSocketShipsDataService) {
        this.webSocketShipsDataService = webSocketShipsDataService;
    }

    /**
     * Attach a broadcasting "head" to the incoming data streams, such that data is published to the clients.
     *
     * @param currentShipDetails current view of the aggregated ship data
     */
    public void buildWebSocketBroadcastingPart(KTable<Long, CurrentShipDetails> currentShipDetails) {
        KStream<Long, CurrentShipDetails> aggregatedStream = currentShipDetails.toStream();
        aggregatedStream.foreach((key, value) -> {
            if (this.webSocketShipsDataService.checkForOpenConnections()) {
                this.webSocketShipsDataService.sendCurrentShipDetails(value);
            }
        });
    }
}
