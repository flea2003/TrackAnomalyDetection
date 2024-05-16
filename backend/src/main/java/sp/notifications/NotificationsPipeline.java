package sp.notifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.exceptions.PipelineException;
import sp.model.CurrentShipDetails;
import sp.model.ShipInformation;
import sp.pipeline.StreamUtils;
import sp.pipeline.scorecalculators.ScoreCalculationStrategy;
import sp.repositories.NotificationsRepository;

@Service
public class NotificationsPipeline {
    private final NotificationsRepository notificationsRepository;
    private final HashMap<String, AnomalyInformation> notifications;


    private static final String CALCULATED_SCORES_TOPIC_NAME;
    private static final String KAFKA_SERVER_ADDRESS;
    private static final String KAFKA_STORE_NAME;
    private KafkaStreams kafkaStreams;
    private KTable<String, CurrentShipDetails> state;

    // Load the needed parameters from the configurations file
    static {
        try {
            CALCULATED_SCORES_TOPIC_NAME = StreamUtils.loadConfig().getProperty("calculated.scores.topic.name");
            KAFKA_SERVER_ADDRESS = StreamUtils.loadConfig().getProperty("kafka.server.address");
            KAFKA_STORE_NAME = StreamUtils.loadConfig().getProperty("kafka.store.name");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    public NotificationsPipeline(NotificationsRepository notificationsRepository) throws IOException {
        this.notificationsRepository = notificationsRepository;
    }

    public void buildNotifications() {

        // Create a keyed Kafka Stream of incoming AnomalyInformation signals
        StreamsBuilder builder = new StreamsBuilder();

        // Construct two separate streams for AISSignals and computed AnomalyScores, and wrap each stream values into
        // ShipInformation object, so that we could later merge these two streams
        KStream<String, String> streamAnomalyInformationJSON = builder.stream(CALCULATED_SCORES_TOPIC_NAME);
        KStream<String, AnomalyInformation> streamAnomalyInformation  = streamAnomalyInformationJSON.mapValues(x -> {
            try {
                return AnomalyInformation.fromJson(x);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        KStream<String, AnomalyInformation> streamAnomalyInformationKeyed = streamAnomalyInformation.selectKey((key, value) -> value.getShipHash());

        // Construct the KTable (state that is stored) by aggregating the merged stream
        KTable<String, AnomalyInformation> table = streamAnomalyInformationKeyed
                .mapValues(x -> {
                    try {
                        return x.toJson();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .groupByKey()
                .aggregate(
                        AnomalyInformation::new,
                        (key, valueJson, aggregatedShipDetails) -> {
                            try {
                                return aggregateSignals(aggregatedShipDetails, valueJson, key);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Materialized
                                .<String, AnomalyInformation, KeyValueStore<Bytes, byte[]>>as(KAFKA_STORE_NAME)
                                .withValueSerde(AnomalyInformation.getSerde())
                );

        builder.build();
        this.kafkaStreams = StreamUtils.getKafkaStreamConsumingFromKafka(builder);
        this.kafkaStreams.cleanUp();
    }



    public AnomalyInformation aggregateSignals(AnomalyInformation initialInformation, String valueJson, String key) throws JsonProcessingException {
        AnomalyInformation anomalyInformation = AnomalyInformation.fromJson(valueJson);




    }

}
