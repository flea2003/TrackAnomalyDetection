package sp.unit.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sp.controllers.ShipsDataController;
import sp.exceptions.PipelineStartingException;
import sp.model.AnomalyInformation;
import sp.exceptions.NotExistingShipException;
import sp.exceptions.PipelineException;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.MaxAnomalyScoreDetails;
import sp.services.ShipsDataService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipsDataControllerTest {

    private ShipsDataService shipsDataService;
    private ShipsDataController shipsDataController;

    private final OffsetDateTime time1 = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));

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
    void getCurrentShipDetailsSuccessful() throws PipelineException, NotExistingShipException, PipelineStartingException {
        long shipId = 123L;
        when(shipsDataService.getIndividualCurrentShipDetails(shipId)).thenReturn(
                new CurrentShipDetails(new AnomalyInformation(1.0f, "explanation1", time1, shipId),
                        new AISSignal(shipId, 1F,1F,1F,1F,1F,time1,""),
                        new MaxAnomalyScoreDetails(0F,time1))
        );

        ResponseEntity<CurrentShipDetails> response = shipsDataController.getIndividualCurrentShipDetails(shipId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new CurrentShipDetails(new AnomalyInformation(1.0f, "explanation1", time1, shipId),
                new AISSignal(shipId, 1F,1F,1F,1F,1F,time1,""),
                new MaxAnomalyScoreDetails(0F,time1)), response.getBody());
    }

    @Test
    void getCurrentAnomalyInformationShipDoesNotExist() throws PipelineException, NotExistingShipException, PipelineStartingException {
        long shipId = 123L;
        when(shipsDataService.getIndividualCurrentShipDetails(shipId))
                .thenThrow(new NotExistingShipException());

        ResponseEntity<CurrentShipDetails> response = shipsDataController.getIndividualCurrentShipDetails(shipId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCurrentAnomalyInformationPipelineException() throws PipelineException, NotExistingShipException, PipelineStartingException {
        long shipId = 123L;
        when(shipsDataService.getIndividualCurrentShipDetails(shipId))
                .thenThrow(new PipelineException());

        ResponseEntity<CurrentShipDetails> response = shipsDataController.getIndividualCurrentShipDetails(shipId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCurrentAnomalyInformationPipelineStartingException() throws PipelineException, NotExistingShipException, PipelineStartingException {
        long shipId = 123L;
        when(shipsDataService.getIndividualCurrentShipDetails(shipId))
                .thenThrow(new PipelineStartingException());

        ResponseEntity<CurrentShipDetails> response = shipsDataController.getIndividualCurrentShipDetails(shipId);

        assertEquals(HttpStatus.TOO_EARLY, response.getStatusCode());
    }



    @Test
    void getCurrentAnomalyInformationOfAllShipsSuccessful() throws PipelineException, PipelineStartingException {
        AnomalyInformation info1 = new AnomalyInformation(1, "explanation1", time1, 1L);
        AnomalyInformation info2 = new AnomalyInformation(2, "explanation2", time1, 2L);
        AnomalyInformation info3 = new AnomalyInformation(3, "explanation3", time1, 3L);
        CurrentShipDetails details1 = new CurrentShipDetails(info1, null, null);
        CurrentShipDetails details2 = new CurrentShipDetails(info2, null, null);
        CurrentShipDetails details3 = new CurrentShipDetails(info3, null, null);
        when(shipsDataService.getCurrentShipDetails())
                .thenReturn(List.of(details1, details2, details3));

        ResponseEntity<List<CurrentShipDetails>> response = shipsDataController.getCurrentShipDetails();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(details1, details2, details3), response.getBody());
    }

    @Test
    void getCurrentShipDetailsOfAllShipsPipelineException() throws PipelineException, PipelineStartingException {
        when(shipsDataService.getCurrentShipDetails())
                .thenThrow(new PipelineException());

        ResponseEntity<List<CurrentShipDetails>> response = shipsDataController.getCurrentShipDetails();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCurrentShipDetailsOfAllShipsPipelineStartingException() throws PipelineException, PipelineStartingException {
        when(shipsDataService.getCurrentShipDetails())
                .thenThrow(new PipelineStartingException());

        ResponseEntity<List<CurrentShipDetails>> response = shipsDataController.getCurrentShipDetails();

        assertEquals(HttpStatus.TOO_EARLY, response.getStatusCode());
    }

    @Test
    void getShipDetailsHistory() throws PipelineException{
        AnomalyInformation info1 = new AnomalyInformation(1, "explanation1", time1, 1L);
        AnomalyInformation info2 = new AnomalyInformation(2, "explanation2", time1, 2L);
        CurrentShipDetails details1 = new CurrentShipDetails(info1, null, null);
        CurrentShipDetails details2 = new CurrentShipDetails(info2, null, null);
        when(shipsDataService.getHistoryOfShip(5))
            .thenReturn(List.of(details1, details2));

        ResponseEntity<List<CurrentShipDetails>> response = shipsDataController.getHistoryShip(5L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).containsExactlyInAnyOrder(details1, details2);
    }

    @Test
    void getShipDetailsServerError() throws PipelineException{
        when(shipsDataService.getHistoryOfShip(5L)).thenThrow(new PipelineException());
        ResponseEntity<List<CurrentShipDetails>>response = shipsDataController.getHistoryShip(5L);
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}