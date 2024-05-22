package sp.pipeline.parts.identification;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sp.dtos.ExternalAISSignal;
import sp.model.AISSignal;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.StreamUtils;

import java.io.IOException;
import java.util.Objects;

@Component
public class IdAssignmentBuilder {

    private StreamUtils streamUtils;
    private PipelineConfiguration configuration;

    @Autowired
    public IdAssignmentBuilder(StreamUtils streamUtils, PipelineConfiguration configuration) {
        this.streamUtils = streamUtils;
        this.configuration = configuration;
    }

    /**
     * Builds the first part of the pipeline - the part that takes as input the raw AIS signals from Kafka,
     * assigns an internal ID to each signal and sends them to another Kafka topic.
     * The internal ID is calculated as a hash of the producer ID and the ship hash.
     *
     * @return the DataStream with the AISSignal objects that have been assigned an internal ID.
     *         Used in the next step of the pipeline.
     */
    public DataStream<AISSignal> buildIdAssignmentPart(StreamExecutionEnvironment flinkEnv) throws IOException {
        // Create a Flink stream that consumes AIS signals from Kafka
        KafkaSource<String> kafkaSource = streamUtils.getFlinkStreamConsumingFromKafka(configuration.rawIncomingAisTopicName);
        DataStream<String> rawSourceSerialized = flinkEnv.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "AIS Source");

        // Map stream from JSON strings to ExternalAISSignal objects
        DataStream<ExternalAISSignal> sourceWithNoIDs = rawSourceSerialized.map(ExternalAISSignal::fromJson);

        // Map ExternalAISSignal objects to AISSignal objects by assigning an internal ID
        return sourceWithNoIDs.map(x -> {
            int calculatedID = Objects.hash(x.getProducerID(), x.getShipHash()) & 0x7FFFFFFF; // Ensure positive ID
            return new AISSignal(x, calculatedID);
        });
    }
}
