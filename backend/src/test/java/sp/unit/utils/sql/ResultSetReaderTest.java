package sp.unit.utils.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sp.model.AISSignal;
import sp.model.AnomalyInformation;
import sp.model.CurrentShipDetails;
import sp.model.MaxAnomalyScoreDetails;
import sp.pipeline.utils.json.JsonMapper;
import sp.utils.sql.ResultSetReader;

class ResultSetReaderTest {
    AnomalyInformation anomalyInformation1;
    AnomalyInformation anomalyInformation2;
    AISSignal aisSignal1;
    AISSignal aisSignal2;
    MaxAnomalyScoreDetails maxAnomalyScoreDetails1;
    MaxAnomalyScoreDetails maxAnomalyScoreDetails2;
    CurrentShipDetails currentShipDetails1;
    CurrentShipDetails currentShipDetails2;
    OffsetDateTime offsetDateTime1;
    OffsetDateTime offsetDateTime2;

    @BeforeEach
    public void setUp(){
        offsetDateTime1 = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));
        offsetDateTime2 = OffsetDateTime.of(2004, 1, 27, 1,2,0,0, ZoneOffset.ofHours(0));
        aisSignal1 = new AISSignal(123L, 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, offsetDateTime1, "New York");
        aisSignal2 = new AISSignal(123L, 23.5f, 130.0f, 45.0f, 180.0f, 90.0f, offsetDateTime2, "New York");
        anomalyInformation1 = new AnomalyInformation(0.5F, "explanation", offsetDateTime1, 123L);
        anomalyInformation2 = new AnomalyInformation(0.5F, "explanation", offsetDateTime2, 123L);
        maxAnomalyScoreDetails1 = new MaxAnomalyScoreDetails(33F, offsetDateTime1);
        maxAnomalyScoreDetails2 = new MaxAnomalyScoreDetails(50F, offsetDateTime2);
        currentShipDetails1 = new CurrentShipDetails(anomalyInformation1, aisSignal1, maxAnomalyScoreDetails1);
        currentShipDetails2 = new CurrentShipDetails(anomalyInformation2, aisSignal2, maxAnomalyScoreDetails2);
    }
    @Test
    void extractQueryResultsTest() throws SQLException, JsonProcessingException {
        ResultSetReader<CurrentShipDetails> resultSetReader = new ResultSetReader<>();

        ResultSet resultSet = Mockito.mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);

        ResultSetMetaData resultSetMetaData = Mockito.mock(ResultSetMetaData.class);

        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);

        when(resultSetMetaData.getColumnLabel(1)).thenReturn("maxAnomalyScoreInfo");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("currentAnomalyInformation");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("currentAISSignal");

        when(resultSet.getString(1)).thenReturn(JsonMapper.toJson(maxAnomalyScoreDetails1), JsonMapper.toJson(maxAnomalyScoreDetails2));
        when(resultSet.getString(2)).thenReturn(JsonMapper.toJson(anomalyInformation1), JsonMapper.toJson(anomalyInformation2));
        when(resultSet.getString(3)).thenReturn(JsonMapper.toJson(aisSignal1), JsonMapper.toJson(aisSignal2));

        assertThat(resultSetReader.extractQueryResults(resultSet, CurrentShipDetails.class)).containsExactlyElementsOf(
            List.of(currentShipDetails1, currentShipDetails2));
    }

    @Test
    void extractQueryResultsEmptyTest() throws SQLException{
        ResultSetReader<CurrentShipDetails> resultSetReader = new ResultSetReader<>();

        ResultSet resultSet = Mockito.mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);

        ResultSetMetaData resultSetMetaData = Mockito.mock(ResultSetMetaData.class);

        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);

        when(resultSetMetaData.getColumnLabel(1)).thenReturn("maxAnomalyScoreInfo");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("currentAnomalyInformation");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("currentAISSignal");

        assertThat(resultSetReader.extractQueryResults(resultSet, CurrentShipDetails.class)).isEmpty();
    }

}