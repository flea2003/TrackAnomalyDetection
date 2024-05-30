package sp.unit.exceptions;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import sp.exceptions.PipelineException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PipelineExceptionTest {
    @Test
    void testConstructor() {
        AssertionsForClassTypes.assertThat(new PipelineException()).isInstanceOf(PipelineException.class);
    }

    @Test
    void testMessage() {
        assertThat(new PipelineException("message").getMessage()).isEqualTo("message");
    }
}
