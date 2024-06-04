package sp.unit.utils.websockets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sp.utils.websockets.WebSocketSessionManager;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class WebSocketSessionManagerTest {

    WebSocketSessionManager manager;

    @BeforeEach
    public void setUp() {
        this.manager = new WebSocketSessionManager();
        this.manager.addSession("s1");
    }

    @Test
    public void testGetActiveSessions() {
        assertThat(this.manager.getActiveSessions().size()).isEqualTo(1);
    }

    @Test
    public void testAddNewSession() {
        this.manager.addSession("s2");
        assertThat(this.manager.getActiveSessions().contains("s2")).isTrue();
        assertThat(this.manager.getActiveSessions().size()).isEqualTo(2);
    }


    @Test
    public void testRemoveExistingSession() {
        this.manager.removeSession("s1");
        assertThat(this.manager.getActiveSessions().isEmpty()).isTrue();
    }


    @Test
    public void testIsActiveExistingSession() {
        assertThat(this.manager.isActive("s1")).isTrue();
    }

    @Test
    public void testIsActiveNonExistingSession() {
        assertThat(this.manager.isActive("s2")).isFalse();
    }

    @Test
    public void testForOpenConnections() {
        assertThat(this.manager.checkForOpenConnections()).isTrue();
    }

}
