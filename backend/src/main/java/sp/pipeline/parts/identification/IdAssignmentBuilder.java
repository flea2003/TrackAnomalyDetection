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
import sp.pipeline.utils.StreamUtils;
import java.util.Objects;

@Component
public class IdAssignmentBuilder {

    private final StreamUtils streamUtils;
    private final PipelineConfiguration config;

    /**
     * Constructor for the IdAssignmentBuilder class.
     *
     * @param streamUtils utility class for setting up streams
     * @param config an object that holds configuration properties
     */
    @Autowired
    public IdAssignmentBuilder(StreamUtils streamUtils, PipelineConfiguration config) {
        this.streamUtils = streamUtils;
        this.config = config;
    }

    /**
     * Builds the first part of the pipeline the part that takes as input the raw AIS signals from Kafka,
     * assigns an internal ID to each signal and sends them to another Kafka topic.
     * The internal ID is calculated as a hash of the producer ID and the ship hash.
     * In practice, this method first creates a consumer from Kafka and then forwards this stream to the overloaded
     * method that takes a DataStream as input. This is done to allow for testing the logic part.
     *
     * @param flinkEnv the Flink execution environment
     * @return the DataStream with the AISSignal objects that have been assigned an internal ID.
     *         Used in the next step of the pipeline.
     */
    public DataStream<AISSignal> buildIdAssignmentPart(StreamExecutionEnvironment flinkEnv) {
        // Create a Kafka source for incoming id-less AIS signals
        KafkaSource<ExternalAISSignal> kafkaSource = streamUtils.getFlinkStreamConsumingFromKafka(
                config.getRawIncomingAisTopicName(),
                ExternalAISSignal.class
        );

        // Create a stream of id-less AIS Signals from the Kafka source
        DataStream<ExternalAISSignal> sourceWithNoIDs = flinkEnv.fromSource(
                kafkaSource, WatermarkStrategy.noWatermarks(), "AIS Source"
        );

        // Map ExternalAISSignal objects to AISSignal objects by assigning an internal ID
        return sourceWithNoIDs.map(x -> {
            int calculatedID = Objects.hash(x.getProducerID(), x.getShipHash()) & 0x7FFFFFFF; // Ensure positive ID
            return new AISSignal(x, calculatedID);
        });
    }
}
