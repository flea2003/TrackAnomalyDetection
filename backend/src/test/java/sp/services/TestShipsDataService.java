package sp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.model.ShipInformation;
import sp.pipeline.AnomalyDetectionPipeline;
import java.util.HashMap;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@SpringBootTest
public class TestShipsDataService {

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

        List<ShipInformation> ship1 = List.of(new ShipInformation("hash1", new AnomalyInformation(0.5f, "", "", ""), signal1), new ShipInformation("hash3", new AnomalyInformation(0.7f, "", "", ""), signal3));

        List<ShipInformation> ship3 = List.of(new ShipInformation("hash3", new AnomalyInformation(0.7f, "", "", ""), signal4));


        CurrentShipDetails currentShipDetails1 = new CurrentShipDetails();
        currentShipDetails1.setAnomalyInformation(new AnomalyInformation(0.5f, "", "", "hash1"));
        currentShipDetails1.setPastInformation(ship1);

        CurrentShipDetails currentShipDetails2 = new CurrentShipDetails();
        currentShipDetails2.setAnomalyInformation(new AnomalyInformation(0.2f, "", "", ""));
        currentShipDetails2.setPastInformation(ship3);

        CurrentShipDetails currentShipDetails3 = new CurrentShipDetails();
        currentShipDetails3.setAnomalyInformation(new AnomalyInformation(0.7f, "", "", ""));
        currentShipDetails3.setPastInformation(ship1);

        CurrentShipDetails currentShipDetails4 = new CurrentShipDetails();
        currentShipDetails4.setAnomalyInformation(new AnomalyInformation(0.1f, "", "", ""));
        currentShipDetails4.setPastInformation(ship3);

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

