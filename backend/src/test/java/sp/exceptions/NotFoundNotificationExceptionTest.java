package sp.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class NotFoundNotificationExceptionTest {
    @Test
    void testConstructor() {
        assertThat(new NotFoundNotificationException()).isInstanceOf(NotFoundNotificationException.class);
    }

    @Test
    void testMessage() {
        assertThat(new NotFoundNotificationException("message").getMessage()).isEqualTo("message");
    }


}
