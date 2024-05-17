package sp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.pipeline.AnomalyDetectionPipeline;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TestShipsDataService {

    private ShipsDataService shipsDataService;
    private ShipsDataService shipsDataServiceBroken;
    private HashMap<String, AnomalyInformation> mapAnomalyInformation;
    private HashMap<String, AISSignal> mapAISSignal;
    private AISSignal signal1;
    private AISSignal signal2;
    private AISSignal signal3;
    private AISSignal signal4;
    private AISSignal signal5;
    private AISSignal signal6;

    @BeforeEach
    public void setUp() throws Exception {
        signal1 = new AISSignal("hash1", 60.0f, 10.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 17, 1,1,0,0, ZoneOffset.ofHours(0)), "Klaipeda");
        signal2 = new AISSignal("hash2", 42.0f, 30.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Chisinau");
        signal3 = new AISSignal("hash1", 63.0f, 11.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Amsterdam");
        signal4 = new AISSignal("hash3", 20.0f, 90.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Delft");
        signal5 = new AISSignal("hash3", 25.0f, 90.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 17, 1,1,0,0, ZoneOffset.ofHours(0)), "Zurich");
        signal6 = new AISSignal("hash4", 0.0f, 0.0f,
                20.0f, 60.0f, 80.0f,
                OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Beijing");

        CurrentShipDetails currentShipDetails1 = new CurrentShipDetails();
        currentShipDetails1.setCurrentAnomalyInformation(new AnomalyInformation(0.5f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "hash1"));

        CurrentShipDetails currentShipDetails2 = new CurrentShipDetails();
        currentShipDetails2.setCurrentAnomalyInformation(new AnomalyInformation(0.2f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), ""));

        CurrentShipDetails currentShipDetails3 = new CurrentShipDetails();
        currentShipDetails3.setCurrentAnomalyInformation(new AnomalyInformation(0.7f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), ""));

        CurrentShipDetails currentShipDetails4 = new CurrentShipDetails();
        currentShipDetails4.setCurrentAnomalyInformation(new AnomalyInformation(0.1f, "", OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), ""));

        mapAnomalyInformation = new HashMap<>(){{
            put("hash1", currentShipDetails1.getCurrentAnomalyInformation());
            put("hash2", currentShipDetails2.getCurrentAnomalyInformation());
            put("hash3", currentShipDetails3.getCurrentAnomalyInformation());
            put("hash4", currentShipDetails4.getCurrentAnomalyInformation());
        }};

        mapAISSignal = new HashMap<>(){{
            put("hash1", signal3);
            put("hash2", signal2);
            put("hash3", signal5);
            put("hash4", signal6);
        }};

        AnomalyDetectionPipeline anomalyDetectionPipeline = mock(AnomalyDetectionPipeline.class);
        shipsDataService = new ShipsDataService(anomalyDetectionPipeline);

        doReturn(mapAnomalyInformation).when(anomalyDetectionPipeline).getCurrentScores();
        doReturn(mapAISSignal).when(anomalyDetectionPipeline).getCurrentAISSignals();

        AnomalyDetectionPipeline anomalyDetectionPipelineBroken = mock(AnomalyDetectionPipeline.class);
        shipsDataServiceBroken = new ShipsDataService(anomalyDetectionPipelineBroken);

        doThrow(PipelineException.class).when(anomalyDetectionPipelineBroken).getCurrentScores();
        doThrow(PipelineException.class).when(anomalyDetectionPipelineBroken).getCurrentAISSignals();
    }

    @Test
    void getCurrentAISInformationTest(){
        try {
            assertThat(shipsDataService.getCurrentAISInformation("hash1")).isEqualTo(signal3);
        } catch (Exception e) {
            fail("Exception thrown but not Expected");
        }
    }

    @Test
    void getCurrentAISInformationTestNoShipException(){
        assertThatThrownBy(() -> shipsDataService.getCurrentAISInformation("hash6"))
                .isInstanceOf(NotExistingShipException.class).hasMessage("Couldn't find such ship.");
    }

    @Test
    void getCurrentAISInformationTestPipelineException(){
        assertThatThrownBy(() -> shipsDataServiceBroken.getCurrentAISInformation("hash6"))
                .isInstanceOf(PipelineException.class);
    }

    @Test
    void getCurrentAnomalyInformationTest(){
        try{
            AnomalyInformation anomalyInformation = shipsDataService.getCurrentAnomalyInformation("hash1");
            assertThat(anomalyInformation.getScore()).isEqualTo(0.5f);
            assertThat(anomalyInformation.getShipHash()).isEqualTo("hash1");
        } catch (Exception e){
            fail("Exception thrown but not Expected");
        }
    }

    @Test
    void getCurrentAnomalyInformationTestNotShipException(){
        assertThatThrownBy(() -> shipsDataService.getCurrentAnomalyInformation("hash6"))
                .isInstanceOf(NotExistingShipException.class).hasMessage("Couldn't find such ship.");
    }

    @Test
    void getCurrentAnomalyInformationTestPipelineException(){
        assertThatThrownBy(() -> shipsDataServiceBroken.getCurrentAnomalyInformation("hash6"))
                .isInstanceOf(PipelineException.class);
    }

    @Test
    void CurrentAISOfAllShipsPipelineExceptionTest(){
        assertThatThrownBy(() -> shipsDataServiceBroken.getCurrentAISInformationOfAllShips())
                .isInstanceOf(PipelineException.class);
    }

    @Test
    void CurrentAnomalyInformationOfAllShipsExceptionTest(){
        assertThatThrownBy(() -> shipsDataServiceBroken.getCurrentAnomalyInformationOfAllShips())
                .isInstanceOf(PipelineException.class);
    }

}

