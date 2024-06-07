package sp.unit.utils.sql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.Current;
import sp.model.CurrentShipDetails;
import sp.pipeline.utils.json.JsonMapper;
import sp.utils.DruidConfig;
import sp.utils.sql.FileReader;
import sp.utils.sql.QueryExecutor;

public class QueryExecutorTest {

//    private QueryExecutor queryExecutor;
//    private DruidConfig druidConfig;
//
//    @BeforeEach
//    public void setUp(){
//        druidConfig = Mockito.mock(DruidConfig.class);
//
//        queryExecutor = new QueryExecutor(druidConfig);
//    }
//
//    @Test
//    void testQueryExctractor(){
//        try(MockedStatic<FileReader>mockedFileReader = mockStatic(FileReader.class)) {
//            mockedFileReader.when(() -> FileReader.readQueryFromFile("path"))
//                .thenReturn();
//
//            queryExecutor.executeQueryOneLong(5, "path", CurrentShipDetails.class);
//
//        }
//    }
}