package sp.unit.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sp.controllers.ShipsDataController;
import sp.dtos.TrajectoryObject;
import sp.exceptions.DatabaseException;
import sp.model.AnomalyInformation;
import sp.exceptions.NotExistingShipException;

import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.model.MaxAnomalyScoreDetails;
import sp.services.ShipsDataService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
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
    void getCurrentShipDetailsSuccessful() throws NotExistingShipException {
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
    void getCurrentAnomalyInformationShipDoesNotExist() throws NotExistingShipException {
        long shipId = 123L;
        when(shipsDataService.getIndividualCurrentShipDetails(shipId))
                .thenThrow(new NotExistingShipException());

        ResponseEntity<CurrentShipDetails> response = shipsDataController.getIndividualCurrentShipDetails(shipId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCurrentAnomalyInformationOfAllShipsSuccessful() {
        AnomalyInformation info1 = new AnomalyInformation(1, "explanation1", time1, 1L);
        AnomalyInformation info2 = new AnomalyInformation(2, "explanation2", time1, 2L);
        AnomalyInformation info3 = new AnomalyInformation(3, "explanation3", time1, 3L);
        CurrentShipDetails details1 = new CurrentShipDetails(info1, null, null);
        CurrentShipDetails details2 = new CurrentShipDetails(info2, null, null);
        CurrentShipDetails details3 = new CurrentShipDetails(info3, null, null);
        when(shipsDataService.getCurrentShipDetailsOfAllShips())
                .thenReturn(List.of(details1, details2, details3));

        ResponseEntity<List<CurrentShipDetails>> response = shipsDataController.getCurrentShipDetails();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(details1, details2, details3), response.getBody());
    }

    @Test
    void getShipDetailsHistory() throws DatabaseException {
        AnomalyInformation info1 = new AnomalyInformation(1, "explanation1", time1, 1L);
        AnomalyInformation info2 = new AnomalyInformation(2, "explanation2", time1, 2L);
        CurrentShipDetails details1 = new CurrentShipDetails(info1, null, null);
        CurrentShipDetails details2 = new CurrentShipDetails(info2, null, null);
        when(shipsDataService.getHistoryOfShip(5))
            .thenReturn(List.of(details1, details2));

        ResponseEntity<List<CurrentShipDetails>> response = shipsDataController.getHistoryOfShip(5L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).containsExactlyInAnyOrder(details1, details2);
    }

    @Test
    void getShipDetailsServerError() throws DatabaseException{
        when(shipsDataService.getHistoryOfShip(5L)).thenThrow(new DatabaseException());
        ResponseEntity<List<CurrentShipDetails>>response = shipsDataController.getHistoryOfShip(5L);
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getSampledShipDetailsError() throws DatabaseException{
        when(shipsDataService.getSubsampledHistoryOfShip(5L)).thenThrow(new DatabaseException());
        ResponseEntity<List<TrajectoryObject>>response = shipsDataController.getSampledHistoryOfShip(5L);
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getSampledShipDetailsFine() throws DatabaseException{
        List<TrajectoryObject> answer = new ArrayList<>(Arrays
                .asList(
                        new TrajectoryObject(1, 12, 14, OffsetDateTime.now(), 12.0F),
                        new TrajectoryObject(1, 124, 146, OffsetDateTime.now(), 12.0F)
                ));

        when(shipsDataService.getSubsampledHistoryOfShip(5L)).thenReturn(answer);
        ResponseEntity<List<TrajectoryObject>>response = shipsDataController.getSampledHistoryOfShip(5L);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), answer);

    }
}