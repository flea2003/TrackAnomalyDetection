package sp.utils.websockets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@NoArgsConstructor
@Getter
public class WebSocketSessionManager {
    private Set<String> activeSessions = Collections.synchronizedSet(new HashSet<>());

    /**
     * Utility method for registering the new client sessions.
     *
     * @param sessionId id of the new client session
     */
    public void addSession(String sessionId) {
        this.activeSessions.add(sessionId);
    }

    /**
     * Utility method for removing outdated client sessions.
     *
     * @param sessionId id of the new client session
     */
    public void removeSession(String sessionId) {
        this.activeSessions.remove(sessionId);
    }

    /**
     * Utility method for checking whether a client session is active or not.
     *
     * @param sessionId id of the new client session
     * @return boolean value
     */
    public boolean isActive(String sessionId) {
        return this.activeSessions.contains(sessionId);
    }

    /**
     * Utility method for checking whether there are any open client connections.
     *
     * @return boolean value
     */
    public boolean checkForOpenConnections() {
        return !this.activeSessions.isEmpty();
    }

}