package sp.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sp.pipeline.PipelineConfiguration;
import sp.utils.DruidConfig;
import sp.utils.sql.ResultSetReader;

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

    @Test
    void testDruidConnectionNull(){
        PipelineConfiguration pipelineConfiguration = mock(PipelineConfiguration.class);
        DruidConfig druidConfig = new DruidConfig(pipelineConfiguration);
        doThrow(SQLException.class).when(pipelineConfiguration.getDruidUrl());
        assertThat(druidConfig.connection()).isNull();
    }

    @Test
    void testDruidConnectionNotNull(){
        PipelineConfiguration pipelineConfiguration = mock(PipelineConfiguration.class);
        DruidConfig druidConfig = new DruidConfig(pipelineConfiguration);

        try(MockedStatic<DriverManager>mocked = mockStatic(DriverManager.class)) {
            mocked.when(() -> DriverManager.getConnection(any(), any()))
                .thenReturn(mock(Connection.class));
            assertThat(druidConfig.connection()).isNotNull();
        }
    }

}