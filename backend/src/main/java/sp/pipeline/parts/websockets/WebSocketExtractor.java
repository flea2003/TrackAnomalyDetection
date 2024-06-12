package sp.pipeline.parts.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.model.CurrentShipDetails;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.parts.aggregation.extractors.GenericKafkaExtractor;
import sp.pipeline.utils.StreamUtils;
import sp.pipeline.utils.json.JsonMapper;
import sp.services.WebSocketShipsDataService;

@Service
public class WebSocketExtractor extends GenericKafkaExtractor {
    private final WebSocketShipsDataService webSocketShipsDataService;
    private final Logger logger = LoggerFactory.getLogger(WebSocketExtractor.class);

    /**
     * Constructor for WebSocketExtractor.
     *
     * @param streamUtils an object that holds utility methods for dealing with streams
     * @param configuration an object that holds configuration properties
     * @param webSocketShipsDataService an instance of websockets service
     */
    @Autowired
    public WebSocketExtractor(StreamUtils streamUtils,
                              PipelineConfiguration configuration,
                              WebSocketShipsDataService webSocketShipsDataService) {
        super(streamUtils, configuration, configuration.getPollingFrequencyForSockets(),
                configuration.getShipsHistoryTopicName());
        this.webSocketShipsDataService = webSocketShipsDataService;
    }

    /**
     * Constructor for WebSocketExtractor, used for testing purposes.
     *
     * @param webSocketShipsDataService an instance of websockets service
     */
    public WebSocketExtractor(WebSocketShipsDataService webSocketShipsDataService) {
        this.webSocketShipsDataService = webSocketShipsDataService;
    }

    /**
     * Processes an incoming record from the Kafka topic.
     *
     * @param record the record incoming from Kafka topic
     */
    @Override
    public void processNewRecord(ConsumerRecord<Long, String> record) {
        if (!this.webSocketShipsDataService.checkForOpenConnections()) {
            return;
        }
        try {
            CurrentShipDetails received = JsonMapper.fromJson(record.value(), CurrentShipDetails.class);

            this.webSocketShipsDataService.sendCurrentShipDetails(received);
        } catch (JsonProcessingException e) {
            logger.error("Failed to process incoming record. Error: ", e);
        }
    }
}
