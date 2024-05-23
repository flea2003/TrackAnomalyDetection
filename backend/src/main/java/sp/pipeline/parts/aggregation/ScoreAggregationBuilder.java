package sp.pipeline.parts.aggregation;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.parts.aggregation.aggregators.CurrentStateAggregator;
import sp.pipeline.utils.json.KafkaJson;

@Component
public class ScoreAggregationBuilder {

    private final CurrentStateAggregator currentStateAggregator;
    private final PipelineConfiguration configuration;

    /**
     * Constructor for the ScoreAggregationBuilder class.
     *
     * @param configuration an object that holds configuration properties
     * @param currentStateAggregator a responsible for aggregating the current state of the pipeline
     */
    @Autowired
    public ScoreAggregationBuilder(PipelineConfiguration configuration,
                                   CurrentStateAggregator currentStateAggregator) {
        this.currentStateAggregator = currentStateAggregator;
        this.configuration = configuration;
    }

    /**
     * Builds a KStream object that consists of computed AnomalyInformation objects, wrapped around
     * ShipInformation class (i.e. AISSignal is set to null)
     *
     * @param builder streams builder
     * @return KStream object that consists of computed AnomalyInformation objects, wrapped around
     *     ShipInformation class
     */
    private KStream<Long, ShipInformation> streamAnomalyInformation(StreamsBuilder builder) {

        // Take JSON strings from score topic, deserialize them into AnomalyInformation
        KStream<Long, AnomalyInformation> streamAnomalyInformation = KafkaJson.deserialize(
                builder.stream(configuration.getCalculatedScoresTopicName()),
                AnomalyInformation.class
        );

        // Wrap the AnomalyInformation objects into ShipInformation objects, so we could later merge the stream
        return streamAnomalyInformation.mapValues(x -> new ShipInformation(x.getId(), x, null));
    }

    /**
     * Builds a KStream object that consists of computed AISSignal objects, wrapped around
     * ShipInformation class (i.e. AnomalyInformation is set to null)
     *
     * @param builder streams builder
     * @return a KStream object that consists of computed AISSignal objects, wrapped around
     *     ShipInformation class
     */
    private KStream<Long, ShipInformation> streamAISSignals(StreamsBuilder builder) {

        // Take the initial AISSignal and wrap them into ShipInformation objects, so we could later merge the stream
        // with already wrapped AnomalyInformation objects
        KStream<Long, AISSignal> signalsStream = KafkaJson.deserialize(
                builder.stream(configuration.getIncomingAisTopicName()),
                AISSignal.class
        );

        // Wrap the AISSignal objects into ShipInformation objects, so we could later merge the stream
        return signalsStream.mapValues(x -> new ShipInformation(x.getId(), null, x));
    }

    /**
     * Method that constructs a unified stream of AnomalyInformation and AISSignal instances,
     * wrapped inside a ShipInformation class.
     *
     * @param builder StreamsBuilder instance responsible for configuring the KStream instances
     * @return unified stream
     */
    private KStream<Long, ShipInformation> mergeStreams(StreamsBuilder builder) {
        // Construct two separate streams for AISSignals and computed AnomalyScores, and wrap each stream values into
        // ShipInformation object, so that we could later merge these two streams
        KStream<Long, ShipInformation> streamAnomalyInformation  = streamAnomalyInformation(builder);
        KStream<Long, ShipInformation> streamAISSignals = streamAISSignals(builder);

        // Merge two streams and select the ship hash as a key for the new stream.
        return streamAISSignals
                .merge(streamAnomalyInformation)
                .selectKey((key, value) -> value.getShipId());
    }

    /**
     * Builds the second part of the `sp.pipeline`the score aggregation part. In particular, this part
     * takes the calculated score updates from Kafka (which were pushed there by the previous part)
     * and aggregates them into a KTable. It also takes care of appending needed AIS signals to the KTable.
     * This KTable is then used as the state of the sp.pipeline.
     *
     * <p>The actual implementation works the following way:
     * 1. Incoming AIS signals are mapped to ShipInformation object with the anomaly score missing.
     * 2. AIS score updates are mapped to ShipInformation objects as well, with AIS signal missing.
     * 3. The AIS-signal type ShipInformation objects get to the KTable first and just get added there.
     * 4. The AIS-score-update type ShipInformation objects  get to the KTable after those and just update the missing
     * anomaly score field in the corresponding places.
     * </p>
     */
    public KTable<Long, CurrentShipDetails> buildScoreAggregationPart(StreamsBuilder builder) {
        // Construct and merge two streams and select the ship hash as a key for the new stream.
        KStream<Long, ShipInformation> mergedStream = mergeStreams(builder);

        // Construct the KTable (state that is stored) by aggregating the merged stream
        KTable<Long, CurrentShipDetails> table =
                KafkaJson.serialize(mergedStream)
                .groupByKey()
                .aggregate(
                        CurrentShipDetails::new,
                        (key, valueJson, aggregatedShipDetails) -> {
                            try {
                                return currentStateAggregator.aggregateSignals(aggregatedShipDetails, valueJson);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Materialized
                                .<Long, CurrentShipDetails, KeyValueStore<Bytes, byte[]>>as(configuration.getKafkaStoreName())
                                .withValueSerde(CurrentShipDetails.getSerde())
                );
        builder.build();
        return table;
    }
}
