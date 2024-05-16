package sp.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sp.dtos.AISSignal;
import sp.dtos.AnomalyInformation;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.services.ShipsDataService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipsDataControllerTest {

    private ShipsDataService shipsDataService;
    private ShipsDataController shipsDataController;

    @BeforeEach
    void setUp() {
        shipsDataService = mock(ShipsDataService.class);
        shipsDataController = new ShipsDataController(shipsDataService);
    }

    @AfterEach
    void tearDown() {
        shipsDataController = null;
        shipsDataService = null;
    }

    @Test
    void getCurrentAISInformationSuccessful() throws PipelineException, NotExistingShipException {
        String shipId = "ship1";
        when(shipsDataService.getCurrentAISInformation(shipId)).thenReturn(
                new AISSignal(shipId, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, "time1", "port1")
        );

        ResponseEntity<AISSignal> response = shipsDataController.getCurrentAISInformation(shipId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new AISSignal(
                shipId, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, "time1", "port1"
        ), response.getBody());
    }

    @Test
    void getCurrentAISInformationShipDoesNotExist() throws PipelineException, NotExistingShipException {
        String shipId = "ship1";
        when(shipsDataService.getCurrentAISInformation(shipId))
                .thenThrow(new NotExistingShipException());

        ResponseEntity<AISSignal> response = shipsDataController.getCurrentAISInformation(shipId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCurrentAISInformationPipelineException() throws PipelineException, NotExistingShipException {
        String shipId = "ship1";
        when(shipsDataService.getCurrentAISInformation(shipId))
                .thenThrow(new PipelineException());

        ResponseEntity<AISSignal> response = shipsDataController.getCurrentAISInformation(shipId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCurrentAnomalyInformationSuccessful() throws PipelineException, NotExistingShipException {
        String shipId = "ship1";
        when(shipsDataService.getCurrentAnomalyInformation(shipId)).thenReturn(
                new AnomalyInformation(1.0f, "explanation1", "time1", "ship1")
        );

        ResponseEntity<AnomalyInformation> response = shipsDataController.getCurrentAnomalyInformation(shipId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new AnomalyInformation(
                1.0f, "explanation1", "time1", "ship1"
        ), response.getBody());
    }

    @Test
    void getCurrentAnomalyInformationShipDoesNotExist() throws PipelineException, NotExistingShipException {
        String shipId = "ship1";
        when(shipsDataService.getCurrentAnomalyInformation(shipId))
                .thenThrow(new NotExistingShipException());

        ResponseEntity<AnomalyInformation> response = shipsDataController.getCurrentAnomalyInformation(shipId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCurrentAnomalyInformationPipelineException() throws PipelineException, NotExistingShipException {
        String shipId = "ship1";
        when(shipsDataService.getCurrentAnomalyInformation(shipId))
                .thenThrow(new PipelineException());

        ResponseEntity<AnomalyInformation> response = shipsDataController.getCurrentAnomalyInformation(shipId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCurrentAISInformationOfAllShipsSuccessful() throws PipelineException {
        AISSignal signal1 = new AISSignal("ship1", 1, 2, 3, 4, 5, "time1", "port1");
        AISSignal signal2 = new AISSignal("ship2", 6, 7, 8, 9, 10, "time2", "port2");
        AISSignal signal3 = new AISSignal("ship3", 11, 12, 13, 14, 15, "time3", "port3");

        when(shipsDataService.getCurrentAISInformationOfAllShips())
                .thenReturn(List.of(signal1, signal2, signal3));

        ResponseEntity<List<AISSignal>> response = shipsDataController.getCurrentAISInformationOfAllShips();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(signal1, signal2, signal3), response.getBody());
    }

    @Test
    void getCurrentAISInformationOfAllShipsPipelineException() throws PipelineException {
        when(shipsDataService.getCurrentAISInformationOfAllShips())
                .thenThrow(new PipelineException());

        ResponseEntity<List<AISSignal>> response = shipsDataController.getCurrentAISInformationOfAllShips();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCurrentAnomalyInformationOfAllShipsSuccessful() throws PipelineException {
        AnomalyInformation info1 = new AnomalyInformation(1, "explanation1", "time1", "ship1");
        AnomalyInformation info2 = new AnomalyInformation(2, "explanation2", "time2", "ship2");
        AnomalyInformation info3 = new AnomalyInformation(3, "explanation3", "time3", "ship3");

        when(shipsDataService.getCurrentAnomalyInformationOfAllShips())
                .thenReturn(List.of(info1, info2, info3));

        ResponseEntity<List<AnomalyInformation>> response = shipsDataController.getCurrentAnomalyInformationOfAllShips();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(info1, info2, info3), response.getBody());
    }

    @Test
    void getCurrentAnomalyInformationOfAllShipsPipelineException() throws PipelineException {
        when(shipsDataService.getCurrentAnomalyInformationOfAllShips())
                .thenThrow(new PipelineException());

        ResponseEntity<List<AnomalyInformation>> response = shipsDataController.getCurrentAnomalyInformationOfAllShips();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}