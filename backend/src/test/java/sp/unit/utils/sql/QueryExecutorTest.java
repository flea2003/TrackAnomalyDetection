package sp.unit.utils.sql;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import sp.utils.DruidConfig;
import sp.utils.sql.QueryExecutor;

public class QueryExecutorTest {

    private QueryExecutor queryExecutor;
    private DruidConfig druidConfig;

    @BeforeEach
    public void setUp(){
        druidConfig = Mockito.mock(DruidConfig.class);

        queryExecutor = new QueryExecutor(druidConfig);
    }

    @Test
    void testQueryExctractor(){
        QueryExecutor.executeQueryOneLong(5,)
    }
}