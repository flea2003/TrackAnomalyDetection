package sp.unit.pipeline.parts.extractors;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.AnomalyInformation;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.MaxAnomalyScoreDetails;
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


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
class ShipInformationExtractorTest {

    AnomalyInformation anomalyInformation1;
    AnomalyInformation anomalyInformation2;
    AnomalyInformation anomalyInformation3;
    AISSignal aisSignal1;
    AISSignal aisSignal2;
    AISSignal aisSignal3;
    CurrentShipDetails currentShipDetails1;
    CurrentShipDetails currentShipDetails2;
    CurrentShipDetails currentShipDetails3;
    MaxAnomalyScoreDetails maxAnomalyScoreDetails1;
    MaxAnomalyScoreDetails maxAnomalyScoreDetails2;
    MaxAnomalyScoreDetails maxAnomalyScoreDetails3;
    OffsetDateTime offsetDateTime1;
    OffsetDateTime offsetDateTime2;
    OffsetDateTime offsetDateTime3;
    private AnomalyDetectionPipeline anomalyDetectionPipeline;
    private NotificationService notificationService;

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
            streamUtils, idAssignmentBuilder, scoreCalculationBuilder, scoreAggregationBuilder, notificationsDetectionBuilder
        );
    }


    @BeforeEach
    public void setup(){
        anomalyInformation1 = new AnomalyInformation(0.5F, "explanation1", OffsetDateTime.of(2004, 01, 27, 1,1,0,0, ZoneOffset.ofHours(0)), 1L);
        anomalyInformation2 = new AnomalyInformation(0.7F, "explanation2", OffsetDateTime.of(2004, 01, 27, 1,1,0,0, ZoneOffset.ofHours(0)), 2L);
        anomalyInformation3 = new AnomalyInformation(0.9F, "explanation3", OffsetDateTime.of(2004, 01, 27, 1,1,0,0, ZoneOffset.ofHours(0)), 3L);
        offsetDateTime1 = OffsetDateTime.of(2004, 1, 27, 1,59,0,0, ZoneOffset.ofHours(0));
        offsetDateTime2 = OffsetDateTime.of(2004, 1, 27, 1,10,0,0, ZoneOffset.ofHours(0));
        offsetDateTime3 = OffsetDateTime.of(2004, 1, 27, 1,58,0,0, ZoneOffset.ofHours(0));
        aisSignal1 =   new AISSignal(1, 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, OffsetDateTime.of(2004, 1, 27, 1,58,0,0, ZoneOffset.ofHours(0)), "New York");
        aisSignal1.setReceivedTime(offsetDateTime1);
        aisSignal2 =   new AISSignal(2, 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, OffsetDateTime.of(2004, 1, 27, 1,10,0,0, ZoneOffset.ofHours(0)), "New York");
        aisSignal2.setReceivedTime(offsetDateTime2);
        aisSignal3 =   new AISSignal(3, 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, OffsetDateTime.of(2004, 1, 27, 1,59,0,0, ZoneOffset.ofHours(0)), "New York");
        aisSignal3.setReceivedTime(offsetDateTime3);
        maxAnomalyScoreDetails1 = new MaxAnomalyScoreDetails(30f, offsetDateTime1);
        maxAnomalyScoreDetails2 = new MaxAnomalyScoreDetails(35f, offsetDateTime2);
        maxAnomalyScoreDetails3 = new MaxAnomalyScoreDetails(40f, offsetDateTime3);
        currentShipDetails1 = new CurrentShipDetails(anomalyInformation1, aisSignal1, maxAnomalyScoreDetails1);
        currentShipDetails2 = new CurrentShipDetails(anomalyInformation2, aisSignal2, maxAnomalyScoreDetails2);
        currentShipDetails3 = new CurrentShipDetails(anomalyInformation3, aisSignal3, maxAnomalyScoreDetails3);
    }

    @Test
    void testGetCurrentSimpleFilterAISSignals() throws Exception{

        setupPipelineComponents();

        ReadOnlyKeyValueStore<Long, CurrentShipDetails> keyValueStore = mock(ReadOnlyKeyValueStore.class);
        KeyValueIterator<Long, CurrentShipDetails> iterator = spy(KeyValueIterator.class);
        when(iterator.hasNext()).thenReturn(true, true, true, false);
        when(iterator.next()).thenReturn(KeyValue.pair(1L, currentShipDetails1), KeyValue.pair(2L, currentShipDetails2), KeyValue.pair(3L, currentShipDetails3));
        when(keyValueStore.all()).thenReturn(iterator);

        Map<Long, CurrentShipDetails> result = new HashMap<>();
        result.put(1L, currentShipDetails1);
        result.put(2L, currentShipDetails2);
        result.put(3L, currentShipDetails3);
        assertThat(anomalyDetectionPipeline.getShipInformationExtractor()
            .extractLatestCurrentShipDetails(keyValueStore, x -> true)).isEqualTo(result);
        anomalyDetectionPipeline.closePipeline();
    }

    @Test
    void testGetCurrentComplicateFilterAISSignals() throws Exception{

        ReadOnlyKeyValueStore<Long, CurrentShipDetails>keyValueStore = mock(ReadOnlyKeyValueStore.class);
        KeyValueIterator<Long, CurrentShipDetails> iterator = spy(KeyValueIterator.class);
        when(iterator.hasNext()).thenReturn(true, true, true, false);
        when(iterator.next()).thenReturn(KeyValue.pair(1L, currentShipDetails1), KeyValue.pair(2L, currentShipDetails2), KeyValue.pair(3L, currentShipDetails3));
        when(keyValueStore.all()).thenReturn(iterator);

        setupPipelineComponents();

        OffsetDateTime currentTime = OffsetDateTime.of(2004, 1, 27, 1,59,0,0, ZoneOffset.ofHours(0));
        Map<Long, CurrentShipDetails> result = new HashMap<>();
        result.put(1L, currentShipDetails1);
        result.put(3L, currentShipDetails3);
        assertThat(anomalyDetectionPipeline.getShipInformationExtractor().extractLatestCurrentShipDetails(keyValueStore, x -> Duration.between(x.getCurrentAISSignal()
            .getReceivedTime(), currentTime).toMinutes() <= 30)).isEqualTo(result);
        anomalyDetectionPipeline.closePipeline();
    }

}