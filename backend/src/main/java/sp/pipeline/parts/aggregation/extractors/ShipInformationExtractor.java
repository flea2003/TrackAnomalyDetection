package sp.pipeline.parts.aggregation.extractors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.model.CurrentShipDetails;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.utils.StreamUtils;
import sp.pipeline.utils.json.JsonMapper;

@Service
@Getter
public class ShipInformationExtractor extends GenericKafkaExtractor {

    private final Logger logger = LoggerFactory.getLogger(ShipInformationExtractor.class);
    private final ConcurrentHashMap<Long, CurrentShipDetails> state = new ConcurrentHashMap<>();

    /**
     * Constructor for ShipInformationExtractor.
     *
     * @param streamUtils an object that holds utility methods for dealing with streams
     * @param configuration an object that holds configuration properties
     */
    @Autowired
    public ShipInformationExtractor(StreamUtils streamUtils,
                                    PipelineConfiguration configuration) {
        super(streamUtils, configuration, configuration.getPollingFrequencyForCurrentDetails(),
                configuration.getShipsHistoryTopicName());
    }

    /**
     * Default constructor, used for testing purposes.
     */
    public ShipInformationExtractor() {
        super();
    }

    /**
     * Processes an incoming record from the Kafka topic. Deserializes it into a CurrentShipDetails
     * object and updates the state concurrent hashmap.
     *
     * @param record the record incoming from Kafka topic
     */
    @Override
    protected void processNewRecord(ConsumerRecord<Long, String> record) {
        CurrentShipDetails newCurrentShipDetails;
        try {
            newCurrentShipDetails = JsonMapper.fromJson(record.value(), CurrentShipDetails.class);
        } catch (JsonProcessingException e) {
            logger.error("JSON error while processing internal record, so skipping it. Error: ", e);
            return;
        }

        state.put(newCurrentShipDetails.extractId(), newCurrentShipDetails);
    }

    /**
     * Returns the current (last updated) anomaly scores of the ships in the system.
     * Additionally return the current max anomaly score information of the ships in the system.
     *
     * @return the current and max scores of the ships in the system.
     */
    public HashMap<Long, CurrentShipDetails> getCurrentShipDetails() {
        return getFilteredShipDetails(x -> true);
    }


    /**
     * Returns the current (last updated) anomaly scores of the ships in the system after applying a filter operation.
     *
     * @param filter the filter operation that we will apply on the CurrentShipDetails
     * @return the current scores of the ships in the systems after filtering them on our criteria.
     */
    public HashMap<Long, CurrentShipDetails> getFilteredShipDetails(Predicate<CurrentShipDetails> filter) {
        return state
                .entrySet()
                .stream()
                .filter(x -> filter.test(x.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }
}
