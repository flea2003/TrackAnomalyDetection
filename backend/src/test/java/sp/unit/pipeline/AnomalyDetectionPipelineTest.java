package sp.unit.pipeline;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import sp.pipeline.AnomalyDetectionPipeline;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.parts.aggregation.ScoreAggregationBuilder;
import sp.pipeline.parts.aggregation.aggregators.CurrentStateAggregator;
import sp.pipeline.parts.identification.IdAssignmentBuilder;
import sp.pipeline.parts.notifications.NotificationsAggregator;
import sp.pipeline.parts.notifications.NotificationsDetectionBuilder;
import sp.pipeline.parts.scoring.ScoreCalculationBuilder;
import sp.pipeline.parts.scoring.scorecalculators.ScoreCalculationStrategy;
import sp.pipeline.parts.scoring.scorecalculators.SimpleScoreCalculator;
import sp.pipeline.utils.StreamUtils;
import sp.services.NotificationService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.SAME_THREAD)
class AnomalyDetectionPipelineTest {

    private AnomalyDetectionPipeline anomalyDetectionPipeline;
    private NotificationService notificationService;
    private StreamExecutionEnvironment flinkEnv;

    private void setupPipelineComponents() throws IOException {
        StreamUtils streamUtils;
        IdAssignmentBuilder idAssignmentBuilder;
        ScoreCalculationBuilder scoreCalculationBuilder;
        ScoreAggregationBuilder scoreAggregationBuilder;
        NotificationsDetectionBuilder notificationsDetectionBuilder;
        ScoreCalculationStrategy scoreCalculationStrategy;
        CurrentStateAggregator currentStateAggregator;
        PipelineConfiguration config;
        NotificationsAggregator notificationsAggregator;
        flinkEnv = spy(StreamExecutionEnvironment.getExecutionEnvironment());

        // Mock the notification service class (to mock the DB)
        notificationService = mock(NotificationService.class);

        // Create the configuration
        config = new PipelineConfiguration("kafka-connection.properties");

        // Create the core objects
        scoreCalculationStrategy = new SimpleScoreCalculator();
        currentStateAggregator = new CurrentStateAggregator();
        notificationsAggregator = new NotificationsAggregator(notificationService);

        // Create the pipeline builders
        streamUtils = new StreamUtils(config);
        idAssignmentBuilder = new IdAssignmentBuilder(streamUtils, config);
        scoreCalculationBuilder = new ScoreCalculationBuilder(streamUtils, config, scoreCalculationStrategy);
        scoreAggregationBuilder = new ScoreAggregationBuilder(config, currentStateAggregator);
        notificationsDetectionBuilder = new NotificationsDetectionBuilder(notificationsAggregator);

        // Create the pipeline itself
        anomalyDetectionPipeline = new AnomalyDetectionPipeline(
                streamUtils, idAssignmentBuilder, scoreCalculationBuilder, scoreAggregationBuilder, notificationsDetectionBuilder,
                flinkEnv
        );
    }

    @Test
    void successfulPipelineBuild() throws IOException {
        assertDoesNotThrow(() -> {
            setupPipelineComponents();
            anomalyDetectionPipeline.closePipeline();
        });
    }

    @Test
    void testRunPipelineRuntimeException() throws Exception {
        setupPipelineComponents();
        doThrow(new RuntimeException()).when(flinkEnv).executeAsync();
        assertThrows(RuntimeException.class, () -> {
           anomalyDetectionPipeline.runPipeline();
        });
    }
}