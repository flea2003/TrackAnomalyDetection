package sp.unit.services;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sp.model.AnomalyInformation;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.pipeline.AnomalyDetectionPipeline;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import sp.pipeline.parts.aggregation.extractors.ShipInformationExtractor;
import sp.services.ShipsDataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

// TODO include MaxAnomalyScoreDetails objects
public class TestShipsDataService {

    private ShipsDataService shipsDataService;
    private AISSignal signal3;
    CurrentShipDetails currentShipDetails1;
    CurrentShipDetails currentShipDetails2;
    CurrentShipDetails currentShipDetails3;
    CurrentShipDetails currentShipDetails4;
    ShipInformationExtractor shipInformationExtractor;

    @BeforeEach
    public void setUp() throws Exception {
        AISSignal signal1 = new AISSignal(1L, 60.0f, 10.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 17, 1, 1, 0, 0, ZoneOffset.ofHours(0)), "Klaipeda");
        AISSignal signal2 = new AISSignal(2L, 42.0f, 30.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 18, 1, 1, 0, 0, ZoneOffset.ofHours(0)), "Chisinau");
        signal3 = new AISSignal(1L, 63.0f, 11.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Amsterdam");
        AISSignal signal4 = new AISSignal(3L, 20.0f, 90.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 18, 1, 1, 0, 0, ZoneOffset.ofHours(0)), "Delft");
        AISSignal signal5 = new AISSignal(3L, 25.0f, 90.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 17, 1, 1, 0, 0, ZoneOffset.ofHours(0)), "Zurich");
        AISSignal signal6 = new AISSignal(4L, 0.0f, 0.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 18, 1, 1, 0, 0, ZoneOffset.ofHours(0)), "Beijing");

        currentShipDetails1 = new CurrentShipDetails();
        currentShipDetails1.setCurrentAnomalyInformation(new AnomalyInformation(0.5f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), 1L));
        currentShipDetails1.setCurrentAISSignal(signal3);

        currentShipDetails2 = new CurrentShipDetails();
        currentShipDetails2.setCurrentAnomalyInformation(new AnomalyInformation(0.2f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), 2L));
        currentShipDetails2.setCurrentAISSignal(signal2);

        currentShipDetails3 = new CurrentShipDetails();
        currentShipDetails3.setCurrentAnomalyInformation(new AnomalyInformation(0.7f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), 3L));
        currentShipDetails3.setCurrentAISSignal(signal5);

        currentShipDetails4 = new CurrentShipDetails();
        currentShipDetails4.setCurrentAnomalyInformation(new AnomalyInformation(0.1f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), 4L));
        currentShipDetails4.setCurrentAISSignal(signal6);

        Map<Long, CurrentShipDetails> currentShipDetailsMap = new HashMap<Long, CurrentShipDetails>() {{
            put(1L, currentShipDetails1);
            put(2L, currentShipDetails2);
            put(3L, currentShipDetails3);
            put(4L, currentShipDetails4);
        }};

        AnomalyDetectionPipeline anomalyDetectionPipeline = mock(AnomalyDetectionPipeline.class);
        shipInformationExtractor = mock(ShipInformationExtractor.class);
        shipsDataService = new ShipsDataService(anomalyDetectionPipeline, shipInformationExtractor);

        doReturn(currentShipDetailsMap).when(shipInformationExtractor)
            .getFilteredShipDetails(any(Predicate.class));

        doReturn(currentShipDetailsMap).when(shipInformationExtractor)
            .getCurrentShipDetails();
    }

    @Test
    void getIndividualAISTest(){
        try {
            assertThat(shipsDataService.getIndividualCurrentShipDetails(1L).getCurrentAISSignal()).isEqualTo(signal3);
        } catch (Exception e) {
            fail("Exception thrown but not Expected");
        }
    }

    @Test
    void getIndividualDetailsNoShipException(){
        assertThatThrownBy(() -> shipsDataService.getIndividualCurrentShipDetails(6L))
                .isInstanceOf(NotExistingShipException.class).hasMessage("Couldn't find such ship.");
    }

    @Test
    void getIndividualShipDetailsTest(){
        try{
            CurrentShipDetails currentShipDetails = shipsDataService.getIndividualCurrentShipDetails(1L);
            assertThat(currentShipDetails.getCurrentAnomalyInformation().getScore()).isEqualTo(0.5f);
            assertThat(currentShipDetails.getCurrentAnomalyInformation().getId()).isEqualTo(1L);
        } catch (Exception e){
            fail("Exception thrown but not Expected");
        }
    }

    @Test
    void ShipDetailsNotShipException(){
        assertThatThrownBy(() -> shipsDataService.getIndividualCurrentShipDetails(6L))
                .isInstanceOf(NotExistingShipException.class).hasMessage("Couldn't find such ship.");
    }


    @Test
    void getAllCurrentShipDetils(){
        try {
            assertThat(shipsDataService.getCurrentShipDetails()).containsExactlyElementsOf(List.of(currentShipDetails1,
                currentShipDetails2, currentShipDetails3, currentShipDetails4));
        } catch (Exception e){
            fail("Exception thrown but not expected");
        }
    }

    @Test
    void getCurrentShipDetailsTest() {
        List<CurrentShipDetails> result = shipsDataService.getCurrentShipDetails();
        assertThat(result).containsExactly(currentShipDetails1, currentShipDetails2, currentShipDetails3, currentShipDetails4);
    }
}

