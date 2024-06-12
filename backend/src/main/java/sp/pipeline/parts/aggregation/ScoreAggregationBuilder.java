package sp.pipeline.parts.aggregation;

import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.parts.aggregation.aggregators.CurrentStateAggregator;
import sp.pipeline.utils.StreamUtils;

@Component
public class ScoreAggregationBuilder {

    private final CurrentStateAggregator currentStateAggregator;
    private final PipelineConfiguration configuration;
    private final StreamUtils streamUtils;

    /**
     * Constructor for the ScoreAggregationBuilder class.
     *
     * @param configuration an object that holds configuration properties
     * @param currentStateAggregator a Flink stateful map that aggregates the current state for each ship
     * @param streamUtils utility class for setting up streams
     */
    @Autowired
    public ScoreAggregationBuilder(PipelineConfiguration configuration,
                                   CurrentStateAggregator currentStateAggregator,
                                   StreamUtils streamUtils) {
        this.currentStateAggregator = currentStateAggregator;
        this.configuration = configuration;
        this.streamUtils = streamUtils;
    }

    /**
     * Method that constructs a unified stream of AnomalyInformation and AISSignal instances,
     * wrapped inside a ShipInformation class.
     *
     * @param signalStream a stream of identified signals, incoming to the application
     * @param anomalyInformationStream a stream of anomaly information, incoming to the application
     * @return unified stream
     */
    private DataStream<ShipInformation> mergeStreams(DataStream<AISSignal> signalStream,
                                                     DataStream<AnomalyInformation> anomalyInformationStream) {

        // Map both streams to ShipInformation streams
        DataStream<ShipInformation> streamAISSignals = signalStream.map(x -> new ShipInformation(x.getId(), null, x));
        DataStream<ShipInformation> streamAnomalyInformation = anomalyInformationStream.map(x ->
                new ShipInformation(x.getId(), x, null));

        return streamAISSignals.union(streamAnomalyInformation);
    }

    /**
     * Builds the score aggregation part of the pipeline. In particular, this part merges incoming (identified)
     * AIS signals and incoming corresponding Anomaly Information objects, and returns a stream of
     * CurrentShipDetails. I.e., it connects the incoming AIS signals with the corresponding anomaly scores
     * and produces a stream of new states for ships.

     * <p>The actual implementation works the following way:
     * 1. Incoming AIS signals are mapped to ShipInformation object with the anomaly score missing.
     * 2. AIS score updates are mapped to ShipInformation objects as well, with AIS signal missing.
     * 3. The AIS-signal type ShipInformation objects (usually) get to the state first and just get added there.
     * 4. The AIS-score-update type ShipInformation objects (usually) get to the state after those and just update
     * the missing anomaly score field in the corresponding places.
     * </p>
     *
     * @param signalStream a stream of identified signals, incoming to the application
     * @param anomalyInformationStream a stream of anomaly information, incoming to the application
     * @return a stream of current ship details
     */
    public DataStream<CurrentShipDetails> buildScoreAggregationPart(
            DataStream<AISSignal> signalStream,
            DataStream<AnomalyInformation> anomalyInformationStream
    ) {
        // Merge the two incoming streams
        DataStream<ShipInformation> mergedStream = mergeStreams(signalStream, anomalyInformationStream);

        // Perform aggregation for each ship (use the currentStateAggregator stateful map)
        DataStream<CurrentShipDetails> aggregatedStream = mergedStream
                .keyBy(ShipInformation::getShipId)
                .map(currentStateAggregator);

        // Sink the aggregated stream to a Kafka topic
        KafkaSink<CurrentShipDetails> kafkaSink = streamUtils.createSinkFlinkToKafka(
                configuration.getShipsHistoryTopicName()
        );
        aggregatedStream.sinkTo(kafkaSink);
        return aggregatedStream;
    }
}
