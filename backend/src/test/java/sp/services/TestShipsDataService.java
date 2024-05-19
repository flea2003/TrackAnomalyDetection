package sp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.Mockito.*;

// TODO include MaxAnomalyScoreDetails objects
public class TestShipsDataService {

    private ShipsDataService shipsDataService;
    private ShipsDataService shipsDataServiceBroken;
    private Map<Long, CurrentShipDetails> currentShipDetailsMap;
    private AISSignal signal1;
    private AISSignal signal2;
    private AISSignal signal3;
    private AISSignal signal4;
    private AISSignal signal5;
    private AISSignal signal6;

    @BeforeEach
    public void setUp() throws Exception {
        signal1 = new AISSignal(1L, 60.0f, 10.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 17, 1,1,0,0, ZoneOffset.ofHours(0)), "Klaipeda");
        signal2 = new AISSignal(2L, 42.0f, 30.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Chisinau");
        signal3 = new AISSignal(1L, 63.0f, 11.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Amsterdam");
        signal4 = new AISSignal(3L, 20.0f, 90.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Delft");
        signal5 = new AISSignal(3L, 25.0f, 90.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 17, 1,1,0,0, ZoneOffset.ofHours(0)), "Zurich");
        signal6 = new AISSignal(4L, 0.0f, 0.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Beijing");

        CurrentShipDetails currentShipDetails1 = new CurrentShipDetails();
        currentShipDetails1.setCurrentAnomalyInformation(new AnomalyInformation(0.5f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), 1L));
        currentShipDetails1.setCurrentAISSignal(signal3);

        CurrentShipDetails currentShipDetails2 = new CurrentShipDetails();
        currentShipDetails2.setCurrentAnomalyInformation(new AnomalyInformation(0.2f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), 2L));
        currentShipDetails2.setCurrentAISSignal(signal2);

        CurrentShipDetails currentShipDetails3 = new CurrentShipDetails();
        currentShipDetails3.setCurrentAnomalyInformation(new AnomalyInformation(0.7f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), 3L));
        currentShipDetails3.setCurrentAISSignal(signal5);

        CurrentShipDetails currentShipDetails4 = new CurrentShipDetails();
        currentShipDetails4.setCurrentAnomalyInformation(new AnomalyInformation(0.1f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), 4L));
        currentShipDetails4.setCurrentAISSignal(signal6);

        currentShipDetailsMap = new HashMap<Long, CurrentShipDetails>(){{
            put(1L, currentShipDetails1);
            put(2L, currentShipDetails2);
            put(3L, currentShipDetails3);
            put(4L, currentShipDetails4);
        }};


        AnomalyDetectionPipeline anomalyDetectionPipeline = mock(AnomalyDetectionPipeline.class);
        shipsDataService = new ShipsDataService(anomalyDetectionPipeline);

        doReturn(currentShipDetailsMap).when(anomalyDetectionPipeline).getCurrentShipDetails();

        AnomalyDetectionPipeline anomalyDetectionPipelineBroken = mock(AnomalyDetectionPipeline.class);
        shipsDataServiceBroken = new ShipsDataService(anomalyDetectionPipelineBroken);

        doThrow(PipelineException.class).when(anomalyDetectionPipelineBroken).getCurrentShipDetails();
        doThrow(PipelineException.class).when(anomalyDetectionPipelineBroken).getCurrentShipDetails();
    }

    @Test
    void getCurrentAISInformationTest(){
        try {
            assertThat(shipsDataService.getIndividualCurrentShipDetails(1L).getCurrentAISSignal()).isEqualTo(signal3);
        } catch (Exception e) {
            fail("Exception thrown but not Expected");
        }
    }

    @Test
    void getCurrentAISInformationTestNoShipException(){
        assertThatThrownBy(() -> shipsDataService.getIndividualCurrentShipDetails(6L))
                .isInstanceOf(NotExistingShipException.class).hasMessage("Couldn't find such ship.");
    }

    @Test
    void getCurrentAISInformationTestPipelineException(){
        assertThatThrownBy(() -> shipsDataServiceBroken.getIndividualCurrentShipDetails(6L))
                .isInstanceOf(PipelineException.class);
    }

    @Test
    void getCurrentAnomalyInformationTest(){
        try{
            CurrentShipDetails anomalyInformation = shipsDataService.getIndividualCurrentShipDetails(1L);
            assertThat(anomalyInformation.getCurrentAnomalyInformation().getScore()).isEqualTo(0.5f);
            assertThat(anomalyInformation.getCurrentAnomalyInformation().getId()).isEqualTo(1L);
        } catch (Exception e){
            fail("Exception thrown but not Expected");
        }
    }

    @Test
    void getCurrentAnomalyInformationTestNotShipException(){
        assertThatThrownBy(() -> shipsDataService.getIndividualCurrentShipDetails(6L))
                .isInstanceOf(NotExistingShipException.class).hasMessage("Couldn't find such ship.");
    }

    @Test
    void getCurrentAnomalyInformationTestPipelineException(){
        assertThatThrownBy(() -> shipsDataServiceBroken.getIndividualCurrentShipDetails(6L))
                .isInstanceOf(PipelineException.class);
    }

    @Test
    void CurrentAISOfAllShipsPipelineExceptionTest(){
        assertThatThrownBy(() -> shipsDataServiceBroken.getCurrentShipDetails())
                .isInstanceOf(PipelineException.class);
    }

    @Test
    void CurrentAnomalyInformationOfAllShipsExceptionTest(){
        assertThatThrownBy(() -> shipsDataServiceBroken.getCurrentShipDetails())
                .isInstanceOf(PipelineException.class);
    }

}

