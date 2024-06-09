package sp.unit.exceptions;

import org.junit.jupiter.api.Test;
import sp.exceptions.NotificationNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class NotificationNotFoundExceptionTest {
    @Test
    void testConstructor() {
        assertThat(new NotificationNotFoundException()).isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void testMessage() {
        assertThat(new NotificationNotFoundException("message").getMessage()).isEqualTo("message");
    }


}
