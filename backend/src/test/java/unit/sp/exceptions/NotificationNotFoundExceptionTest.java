package unit.sp.exceptions;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import sp.exceptions.NotificationNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class NotificationNotFoundExceptionTest {
    @Test
    void testConstructor() {
        AssertionsForClassTypes.assertThat(new NotificationNotFoundException()).isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void testMessage() {
        assertThat(new NotificationNotFoundException("message").getMessage()).isEqualTo("message");
    }


}
