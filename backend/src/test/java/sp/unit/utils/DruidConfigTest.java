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
import sp.pipeline.PipelineConfiguration;
import sp.unit.pipeline.PipelineConfigurationTest;
import sp.utils.DruidConfig;

class DruidConfigTest {

    @Test
    void testConfiguration(){
        try (MockedStatic<DriverManager> dummyDriver = mockStatic(DriverManager.class)) {
            dummyDriver.when(() -> DriverManager.getConnection(anyString(), any())).thenReturn(mock(Connection.class));

            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.registerBean(PipelineConfiguration.class, () -> mock(PipelineConfiguration.class));
            context.register(DruidConfig.class);
            context.refresh();


            dummyDriver.verify(() -> DriverManager.getConnection(any(), any()), times(1));
        }
    }

}