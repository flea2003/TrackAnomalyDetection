package sp.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PipelineExceptionTest {
    @Test
    void testConstructor() {
        assertThat(new PipelineException()).isInstanceOf(PipelineException.class);
    }

    @Test
    void testMessage() {
        assertThat(new PipelineException("message").getMessage()).isEqualTo("message");
    }
}
