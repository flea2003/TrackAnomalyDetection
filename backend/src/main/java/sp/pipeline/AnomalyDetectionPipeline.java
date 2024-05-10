package sp.pipeline;

import org.apache.kafka.streams.kstream.*;
import sp.dtos.AnomalyInformation;
import sp.model.AISSignal;
import sp.model.ShipInformation;
import sp.model.CurrentShipDetails;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.pipeline.scoreCalculators.ScoreCalculationStategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class AnomalyDetectionPipeline {
    private final static String INCOMING_AIS_TOPIC_NAME;
    private final static String CALCULATED_SCORES_TOPIC_NAME;
    private final static String KAFKA_SERVER_ADDRESS;
    private final ScoreCalculationStategy scoreCalculationStrategy;
    private StreamExecutionEnvironment flinkEnv;
    private KafkaStreams kafkaStreams;
    private KTable<String, CurrentShipDetails> state;

    /**
     * Load the needed parameters from the configurations file
     */
    static {
        try {
            INCOMING_AIS_TOPIC_NAME = StreamUtils.loadConfig().getProperty("incoming.ais.topic.name");
            CALCULATED_SCORES_TOPIC_NAME = StreamUtils.loadConfig().getProperty("calculated.scores.topic.name");
            KAFKA_SERVER_ADDRESS = StreamUtils.loadConfig().getProperty("kafka.server.address");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor for the AnomalyDetectionPipeline.
     *
     * @param scoreCalculationStrategy Strategy the strategy to use for calculating the anomaly scores
     */
    @Autowired
    public AnomalyDetectionPipeline(ScoreCalculationStategy scoreCalculationStrategy) throws IOException {
        this.scoreCalculationStrategy = scoreCalculationStrategy;
        buildPipeline();
    }

    /**
     * Private helper method for building the sp.pipeline.
     */
    private void buildPipeline() throws IOException {
        buildScoreCalculationPart();
        buildScoreAggregationPart();
    }

    /**
     * Builds the first part of the sp.pipeline - the score calculation part, done in Flink. This sp.pipeline
     * consumes AIS signals from Kafka, calculates the anomaly scores (in Flink) and sends them to back
     * to Kafka into another topic.

     * The middle part, i.e., calculating anomaly scores (using Flink) is actually defined in the
     * injected scoreCalculationStrategy class. I.e., this part only calls that method. This way the
     * anomaly detection algorithm can be easily swapped out.
     */
    private void buildScoreCalculationPart() throws IOException {
        this.flinkEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create a Flink stream that consumes AIS signals from Kafka
        KafkaSource<String> kafkaSource = StreamUtils.getFlinkStreamConsumingFromKafka(INCOMING_AIS_TOPIC_NAME);
        DataStream<String> sourceSerialized = flinkEnv.fromSource(kafkaSource,
                WatermarkStrategy.noWatermarks(), "AIS Source");

        DataStream<AISSignal> source = sourceSerialized.map((x) -> {
            System.out.println("Received AIS signal as JSON to topic SHIP. JSON: " + x);
            return AISSignal.fromJson(x);
        });

        // Set up the anomaly detection part of the sp.pipeline (happens in Flink)
        DataStream<AnomalyInformation> updateStream = scoreCalculationStrategy.setupFlinkAnomalyScoreCalculationPart(source);
        DataStream<String> updateStreamSerialized = updateStream.map(x -> {
            System.out.println("Mapping the AnomalyInformation object to JSON. Object: " + x);
            return x.toJson();
        });

        // Send the calculated scores to Kafka
        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(KAFKA_SERVER_ADDRESS)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(CALCULATED_SCORES_TOPIC_NAME)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
//                 TODO: Maybe we need this, maybe not. Not sure yet.
//                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .build();

        updateStreamSerialized.sinkTo(sink);
    }

    /**
     * Builds the second part of the sp.pipeline - the score aggregation part. In particular, this part
     * takes the calculated score updates from Kafka (which were pushed there by the previous part)
     * and aggregates them into a KTable. This KTable is then used as the state of the sp.pipeline.
     */
    private void buildScoreAggregationPart() throws IOException {
        // Create a keyed Kafka Stream of incoming AISUpdate signals
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> streamAnomalyInformationJSON = builder.stream(CALCULATED_SCORES_TOPIC_NAME);
        KStream<String, ShipInformation> streamAnomalyInformation  = streamAnomalyInformationJSON.mapValues(x -> {
                    System.out.println("Received AnomalyInformation object as JSON string in ship-scores. JSON: " + x);
                    AnomalyInformation anomalyInformation = AnomalyInformation.fromJson(x);
                    return new ShipInformation(anomalyInformation.getShipHash(), anomalyInformation, null);
                });

        KStream<String, String> streamAISSignalsJSON = builder.stream(INCOMING_AIS_TOPIC_NAME);
        KStream<String, ShipInformation> streamAISSignals = streamAISSignalsJSON
                .mapValues(x -> {
                    System.out.println("Received AIS signal " + x);
                    AISSignal aisSignal = AISSignal.fromJson(x);
                    return new ShipInformation(aisSignal.getShipHash(), null, aisSignal);
                });


        KStream<String, ShipInformation> mergedStream = streamAISSignals
                .merge(streamAISSignals)
                .selectKey((key, value) -> value.getShipHash());

        KTable<String, CurrentShipDetails> table = mergedStream
                .mapValues(ShipInformation::toJson)
                .groupByKey()
                .aggregate(
                CurrentShipDetails::new,
                (key, valueJson, aggregatedShipDetails) -> {
                    System.out.println("Started aggregating JSON value. JSON: " + valueJson);

                    if (aggregatedShipDetails.getPastInformation() == null)
                        aggregatedShipDetails.setPastInformation(new ArrayList<>());


                    ShipInformation shipInformation = ShipInformation.fromJson(valueJson);
                    AnomalyInformation anomalyInformation = shipInformation.getAnomalyInformation();

                    // If the signal is AIS signal, add it to past information
                    if (shipInformation.getAnomalyInformation() == null) {
                        aggregatedShipDetails.getPastInformation().add(shipInformation);
                    }
                    // If the signal is Anomaly Information signal, attach it to a needed AIS signal
                    else if (shipInformation.getAISSignal() == null) {
                        aggregatedShipDetails.setAnomalyInformation(shipInformation.getAnomalyInformation());
                        for (int i = aggregatedShipDetails.getPastInformation().size() - 1; i >= 0; i--) {
                            ShipInformation information = aggregatedShipDetails.getPastInformation().get(i);
                            if (information.getAISSignal().timestamp.equals(anomalyInformation.getCorrespondingTimestamp())) {
                                information.setAnomalyInformation(anomalyInformation);
                                aggregatedShipDetails.setAnomalyInformation(anomalyInformation);
                                break;
                            }
                        }
                    } else {
                        throw new RuntimeException("Something went wrong");
                    }

                    System.out.println("Current ship details after aggregation, for " + key + " ship: " + aggregatedShipDetails);
                    return aggregatedShipDetails;
                },
                Materialized
                        .<String, CurrentShipDetails, KeyValueStore<Bytes, byte[]>>as("ship-score-store")
                        .withValueSerde(CurrentShipDetails.getSerde())
        );
        builder.build();

        // Save the KTable as the state
        this.state = table;
        this.kafkaStreams = StreamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();
    }

    /**
     * Starts the stream processing: both Flink anomaly-detection part and Kafka score-aggregation part.
     * Note that this method is not blocking, and it will not be called by the constructor.
     */
    public void runPipeline() {
        try {
            this.flinkEnv.executeAsync();
            this.kafkaStreams.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the current scores of the ships in the system.
     *
     * @return the current scores of the ships in the system.
     */
    public HashMap<String, CurrentShipDetails> getCurrentScores() {
        try {
            // Get the current state of the KTable
            final String storeName = this.state.queryableStoreName();
            ReadOnlyKeyValueStore<String, CurrentShipDetails> view = this.kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            );

            // Create a copy of the state
            HashMap<String, CurrentShipDetails> stateCopy = new HashMap<>();
            try(KeyValueIterator<String, CurrentShipDetails> iter = view.all()) {
                iter.forEachRemaining(kv -> stateCopy.put(kv.key, kv.value));
            }
            return stateCopy;
        } catch (Exception e) {
            System.out.println("Failed to query store: " + e.getMessage() + ", continuing");
            return null;
        }
    }
}
