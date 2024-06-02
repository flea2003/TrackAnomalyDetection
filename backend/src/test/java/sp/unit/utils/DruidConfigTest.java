package sp.unit.utils;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class DruidConfigTest {

    @Test
    void testConfiguration(){
        try (MockedStatic<DriverManager> dummyDriver = mockStatic(DriverManager.class)) {
            dummyDriver.when(() -> DriverManager.getConnection(anyString(), any())).thenReturn(mock(Connection.class));
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DruidConfig.class);

            Connection connection1 = context.getBean(Connection.class);
            Connection connection2 = context.getBean(Connection.class);

            dummyDriver.verify(() -> DriverManager.getConnection(anyString(), any()), times(1));
        }
    }

}