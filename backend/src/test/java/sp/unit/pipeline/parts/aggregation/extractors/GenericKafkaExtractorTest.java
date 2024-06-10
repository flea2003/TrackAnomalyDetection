package sp.unit.pipeline.parts.aggregation.extractors;

import org.junit.jupiter.api.Test;
import sp.pipeline.PipelineConfiguration;
import sp.pipeline.parts.aggregation.extractors.ShipInformationExtractor;
import sp.pipeline.utils.StreamUtils;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenericKafkaExtractorTest {
    private final StreamUtils streamUtils = mock(StreamUtils.class);
    private final PipelineConfiguration configuration = mock(PipelineConfiguration.class);
    private final int pollingFrequency = 1000000 - 1;
    private final String topic = "whateva";

    @Test
    void testConsumerFails() {
        when(streamUtils.getConsumer()).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, () -> {
            new ShipInformationExtractor(streamUtils, configuration).stateUpdatingThread();
        });
    }

    @Test
    void testConsumerIsFine() {
        when(streamUtils.getConsumer()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> {
            new ShipInformationExtractor(streamUtils, configuration).stateUpdatingThread();
        });
    }
}
