package sp.unit.pipeline.parts.extractors;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.model.AnomalyInformation;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.MaxAnomalyScoreDetails;
import sp.pipeline.parts.aggregation.extractors.ShipInformationExtractor;
import static org.assertj.core.api.Assertions.assertThat;

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

    ShipInformationExtractor shipInformationExtractor;

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

        shipInformationExtractor = new ShipInformationExtractor();
    }

    @Test
    void testGetCurrentSimpleFilterAISSignals() throws Exception{

        shipInformationExtractor.getState().put(1L, currentShipDetails1);
        shipInformationExtractor.getState().put(2L, currentShipDetails2);
        shipInformationExtractor.getState().put(3L, currentShipDetails3);

        Map<Long, CurrentShipDetails> expectedResult = new HashMap<>();
        expectedResult.put(1L, currentShipDetails1);
        expectedResult.put(2L, currentShipDetails2);
        expectedResult.put(3L, currentShipDetails3);

        assertThat(shipInformationExtractor.getFilteredShipDetails(x -> true)).isEqualTo(expectedResult);
    }

    @Test
    void testGetCurrentComplicateFilterAISSignals() throws Exception{

        shipInformationExtractor.getState().put(1L, currentShipDetails1);
        shipInformationExtractor.getState().put(2L, currentShipDetails2);
        shipInformationExtractor.getState().put(3L, currentShipDetails3);


        Map<Long, CurrentShipDetails> expectedResult = new HashMap<>();
        expectedResult.put(1L, currentShipDetails1);
        expectedResult.put(3L, currentShipDetails3);

        OffsetDateTime currentTime = OffsetDateTime.of(2004, 1, 27, 1,59,0,0, ZoneOffset.ofHours(0));

        assertThat(shipInformationExtractor.getFilteredShipDetails(x -> Duration.between(x.getCurrentAISSignal()
                .getReceivedTime(), currentTime).toMinutes() <= 30)).isEqualTo(expectedResult);
    }
}