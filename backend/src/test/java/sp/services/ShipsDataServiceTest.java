package sp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sp.dtos.AnomalyInformation;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.Exceptions.NotExistingShipException;
import sp.model.Exceptions.PipelineException;
import sp.pipeline.AnomalyDetectionPipeline;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@SpringBootTest
public class ShipsDataServiceTest {

    private ShipsDataService shipsDataService;
    private ShipsDataService shipsDataServiceBroken;
    private HashMap<String, CurrentShipDetails> map;
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
                "17/04/2015", "Klaipeda");
        signal2 = new AISSignal("hash2", 42.0f, 30.0f,
                20.0f, 60.0f, 80.0f,
                "18/04/2015", "Chisinau");
        signal3 = new AISSignal("hash1", 63.0f, 11.0f,
                20.0f, 60.0f, 80.0f,
                "18/04/2015", "Amsterdam");
        signal4 = new AISSignal("hash3", 20.0f, 90.0f,
                20.0f, 60.0f, 80.0f,
                "17/04/2015", "Delft");
        signal5 = new AISSignal("hash3", 25.0f, 90.0f,
                20.0f, 60.0f, 80.0f,
                "17/04/2015", "Zurich");
        signal6 = new AISSignal("hash4", 0.0f, 0.0f,
                20.0f, 60.0f, 80.0f,
                "17/04/2015", "Beijing");

        List<AISSignal> ship1 = List.of(signal1, signal3);
        List<AISSignal> ship2 = List.of(signal2);
        List<AISSignal> ship3 = List.of(signal4, signal5);
        List<AISSignal> ship4 = List.of(signal6);
        CurrentShipDetails currentShipDetails1 = new CurrentShipDetails();
        currentShipDetails1.setScore(0.5f);
        currentShipDetails1.setPastSignals(ship1);

        CurrentShipDetails currentShipDetails2 = new CurrentShipDetails();
        currentShipDetails2.setScore(0.2f);
        currentShipDetails2.setPastSignals(ship2);

        CurrentShipDetails currentShipDetails3 = new CurrentShipDetails();
        currentShipDetails3.setScore(0.7f);
        currentShipDetails3.setPastSignals(ship3);

        CurrentShipDetails currentShipDetails4 = new CurrentShipDetails();
        currentShipDetails4.setScore(0.1f);
        currentShipDetails4.setPastSignals(ship4);

        map = new HashMap<>(){{
            put("hash1", currentShipDetails1);
            put("hash2", currentShipDetails2);
            put("hash3", currentShipDetails3);
            put("hash4", currentShipDetails4);
        }};

        AnomalyDetectionPipeline anomalyDetectionPipeline = mock(AnomalyDetectionPipeline.class);
        shipsDataService = new ShipsDataService(anomalyDetectionPipeline);

        doReturn(map).when(anomalyDetectionPipeline).getCurrentScores();

        AnomalyDetectionPipeline anomalyDetectionPipelineBroken = mock(AnomalyDetectionPipeline.class);
        shipsDataServiceBroken = new ShipsDataService(anomalyDetectionPipelineBroken);
        doThrow(PipelineException.class).when(anomalyDetectionPipelineBroken).getCurrentScores();


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
    void getCurrentAISInformationOfAllShipsTest(){
        try {
            List<AISSignal> AISInformationOfAllShips = shipsDataService.getCurrentAISInformationOfAllShips();
            assertThat(AISInformationOfAllShips).containsExactlyInAnyOrder(signal2, signal3, signal5, signal6);
        }catch (Exception e){
          fail("Expected result but error was thrown");
        }
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

