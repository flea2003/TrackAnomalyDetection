package sp.unit.pipeline;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;
import sp.pipeline.FlinkEnvironment;
import sp.pipeline.PipelineConfiguration;

import java.io.IOException;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class FlinkEnvironmentTest {
    @Test
    void testDistributedFlinkEnv() throws IOException {
        PipelineConfiguration configuration = new PipelineConfiguration("kafka-connection.properties");
        StreamExecutionEnvironment env = new FlinkEnvironment().distributedFlinkEnv(configuration);

        // Make sure a serializer for OffsetDateTime is present
        assertThat(env.getConfig().getSerializerConfig().getDefaultKryoSerializerClasses().keySet())
                .contains(OffsetDateTime.class);
    }
}
