package sp.unit.utils.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sp.model.AISSignal;
import sp.model.CurrentShipDetails;
import sp.utils.DruidConfig;
import sp.utils.sql.FileReader;
import sp.utils.sql.QueryExecutor;
import sp.utils.sql.ResultSetReader;

public class QueryExecutorTest {

    private QueryExecutor queryExecutor;

    CurrentShipDetails currentShipDetails1;
    CurrentShipDetails currentShipDetails2;
    CurrentShipDetails currentShipDetails3;
    CurrentShipDetails currentShipDetails4;
    DruidConfig druidConfig;
    Connection connection;
    PreparedStatement preparedStatement;
    ResultSet resultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        AISSignal signal1 = new AISSignal(5L, 60.0f, 10.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 17, 1, 1, 0, 0, ZoneOffset.ofHours(0)), "Klaipeda");
        AISSignal signal2 = new AISSignal(5L, 42.0f, 30.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 18, 1, 1, 0, 0, ZoneOffset.ofHours(0)), "Chisinau");
        AISSignal signal3 = new AISSignal(5L, 63.0f, 11.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 18, 1,1,0,0, ZoneOffset.ofHours(0)), "Amsterdam");
        AISSignal signal4 = new AISSignal(5L, 20.0f, 90.0f,
            20.0f, 60.0f, 80.0f,
            OffsetDateTime.of(2015, 04, 18, 1, 1, 0, 0, ZoneOffset.ofHours(0)), "Delft");

        currentShipDetails1 = new CurrentShipDetails();
        currentShipDetails1.setCurrentAISSignal(signal1);

        currentShipDetails2 = new CurrentShipDetails();
        currentShipDetails2.setCurrentAISSignal(signal2);

        currentShipDetails3 = new CurrentShipDetails();
        currentShipDetails3.setCurrentAISSignal(signal3);

        currentShipDetails4 = new CurrentShipDetails();
        currentShipDetails4.setCurrentAISSignal(signal4);

        druidConfig = Mockito.mock(DruidConfig.class);
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        when(connection.prepareStatement("SELECT *")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(druidConfig.connection()).thenReturn(connection);

        queryExecutor = new QueryExecutor(druidConfig);
    }

    @Test
    void testQueryExtractor() throws SQLException{
        try(MockedStatic<FileReader>mockedFileReader = mockStatic(FileReader.class)) {
            mockedFileReader.when(() -> FileReader.readQueryFromFile("path"))
                .thenReturn("SELECT *");

            try(MockedStatic<ResultSetReader>mocked = mockStatic(ResultSetReader.class)){
                mocked.when(() -> ResultSetReader.extractQueryResults(any(), any()))
                    .thenReturn(List.of(currentShipDetails1, currentShipDetails2, currentShipDetails3, currentShipDetails4));

                assertThat(queryExecutor.executeQueryOneLong(5, "path", CurrentShipDetails.class))
                    .containsExactlyInAnyOrder(currentShipDetails1, currentShipDetails2, currentShipDetails3, currentShipDetails4);

                verify(preparedStatement, times(1)).setLong(1, 5);
            }
        }
    }

    @Test
    void testSQLException(){
        try(MockedStatic<FileReader>mockedFileReader = mockStatic(FileReader.class)) {
            mockedFileReader.when(() -> FileReader.readQueryFromFile("path"))
                .thenReturn("SELECT *");

            try(MockedStatic<ResultSetReader>mocked = mockStatic(ResultSetReader.class)){
                mocked.when(() -> ResultSetReader.extractQueryResults(any(), any()))
                    .thenThrow(SQLException.class);

                assertThatThrownBy(() -> queryExecutor.executeQueryOneLong(5, "path", CurrentShipDetails.class))
                    .isInstanceOf(SQLException.class);
            }
        }
    }
}