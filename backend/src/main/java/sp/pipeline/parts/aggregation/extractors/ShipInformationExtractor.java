package sp.pipeline.parts.aggregation.extractors;

import java.util.HashMap;
import java.util.function.Predicate;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.StreamsNotStartedException;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import sp.exceptions.PipelineException;
import sp.exceptions.PipelineStartingException;
import sp.model.CurrentShipDetails;

public class ShipInformationExtractor {

    private final KTable<Long, CurrentShipDetails> state;
    private final KafkaStreams kafkaStreams;

    public ShipInformationExtractor(KTable<Long, CurrentShipDetails> state, KafkaStreams kafkaStreams) {
        this.state = state;
        this.kafkaStreams = kafkaStreams;
    }

    /**
     * Returns the current (last updated) anomaly scores of the ships in the system.
     * Additionally return the current max anomaly score information of the ships in the system.
     *
     * @return the current and max scores of the ships in the system.
     */
    public HashMap<Long, CurrentShipDetails> getCurrentShipDetails() throws
            PipelineException, PipelineStartingException {
        return getFilteredShipDetails(x -> true);
    }


    /**
     * Returns the current (last updated) anomaly scores of the ships in the system after applying a filter operation.
     *
     * @param filter - the filter operation that we will apply on the CurrentShipDetails
     * @return the current scores of the ships in the systems after filtering them on our criteria.
     * @throws PipelineException exception thrown in case that our pipeline fails.
     */
    public HashMap<Long, CurrentShipDetails> getFilteredShipDetails(Predicate<CurrentShipDetails> filter)
            throws PipelineException, PipelineStartingException {
        try {
            // Get the current state of the KTable
            final String storeName = this.state.queryableStoreName();
            ReadOnlyKeyValueStore<Long, CurrentShipDetails> view = this.kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            );

            // Create a copy of the state considering only the current AnomalyInformation values for each ship
            return extractLatestCurrentShipDetails(view, filter);

        } catch (StreamsNotStartedException e) {
            throw new PipelineStartingException("The pipeline has not yet started");
        } catch (InvalidStateStoreException e) {
            throw new PipelineException("Error while querying the state store");
        }
    }

    /**
     * Method which extracts the latest anomalies from a given ReadOnlyKeyValueStore.
     *
     * @param view - the ReadOnlyKeyValueStore
     * @param filter - the filter that we will apply to the CurrentShipDetails
     * @return - the extracted HashMap with the required information.
     */
    public HashMap<Long, CurrentShipDetails> extractLatestCurrentShipDetails(ReadOnlyKeyValueStore<Long, CurrentShipDetails> view,
                                                                             Predicate<CurrentShipDetails> filter) {
        HashMap<Long, CurrentShipDetails> stateCopy = new HashMap<>();
        try (KeyValueIterator<Long, CurrentShipDetails> iter = view.all()) {
            iter.forEachRemaining(kv -> {
                if (filter.test(kv.value)) {
                    stateCopy.put(kv.key, kv.value);
                }
            });
        }
        return stateCopy;
    }

}
