package sp.pipeline;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
class AnomalyDetectionPipelineTest {

    @Test
    void successfulLoad() throws Exception {
        ScoreCalculationStrategy scoreCalculationStrategy = new DefaultScoreCalculator();
        StreamUtils streamUtils = new StreamUtils("kafka-connection.properties");
        CurrentStateAggregator currentStateAggregator = new CurrentStateAggregator();
        AnomalyDetectionPipeline pipeline = new AnomalyDetectionPipeline(
                scoreCalculationStrategy, streamUtils, currentStateAggregator,  new NotificationsAggregator(mock(NotificationService.class))
        );

        assertNotNull(pipeline);
        pipeline.closePipeline();
    }

    @Test
    void loadConfigIOException() throws IOException {
        ScoreCalculationStrategy scoreCalculationStrategy = new DefaultScoreCalculator();
        StreamUtils streamUtils = mock(StreamUtils.class);

        when(streamUtils.loadConfig()).thenThrow(new IOException());

        assertThrows(IOException.class, () -> new AnomalyDetectionPipeline(
                scoreCalculationStrategy, streamUtils, new CurrentStateAggregator(),  new NotificationsAggregator(mock(NotificationService.class))
        ));
    }

    @Test
    void loadConfigNull() throws IOException {
        ScoreCalculationStrategy scoreCalculationStrategy = new DefaultScoreCalculator();
        StreamUtils streamUtils = mock(StreamUtils.class);

        when(streamUtils.loadConfig()).thenReturn(null);

        assertThrows(IOException.class, () -> new AnomalyDetectionPipeline(
                scoreCalculationStrategy, streamUtils, new CurrentStateAggregator(), new NotificationsAggregator(mock(NotificationService.class))
        ));
    }
}