package sp.pipeline;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.RemoteStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import sp.pipeline.utils.OffsetDateTimeSerializer;

@org.springframework.context.annotation.Configuration
public class FlinkEnvironment {

    /**
     * Creates a distributed Flink environment, i.e., a one where the Flink cluster
     * is running externally.
     *
     * @param configuration an object that holds pipeline configuration properties
     * @return stream execution environment for Flink
     */
    @Lazy
    @Bean
    public StreamExecutionEnvironment distributedFlinkEnv(PipelineConfiguration configuration) {
        Configuration config = new Configuration();
        config.setString("pipeline.default-kryo-serializers",
                "class:java.time.OffsetDateTime,serializer:" + OffsetDateTimeSerializer.class.getName());

        return new RemoteStreamEnvironment(
                configuration.getFlinkJobManagerIp(),
                configuration.getFlinkJobManagerPort(),
                config,
                "build/libs/" + configuration.getFlinkDependencyJarPath()
        );
    }

    /**
     * Creates a local Flink environment, i.e., a one where the Flink cluster
     * is spawned by the application itself and runs locally.
     *
     * @return local stream execution environment for Flink
     */
    @Lazy
    @Bean
    public StreamExecutionEnvironment localFlinkEnv() {
        Configuration config = new Configuration();
        config.setString("pipeline.default-kryo-serializers",
                "class:java.time.OffsetDateTime,serializer:" + OffsetDateTimeSerializer.class.getName());
        config.setString("taskmanager.memory.fraction", "0.3");
        config.setString("taskmanager.memory.network.max", "300 mb");
        return StreamExecutionEnvironment.getExecutionEnvironment(config);
    }
}
