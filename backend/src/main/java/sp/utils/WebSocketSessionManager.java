package sp.utils;

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

    public void addSession(String sessionId) {
        this.activeSessions.add(sessionId);
    }

    public void removeSession(String sessionId) {
        this.activeSessions.remove(sessionId);
    }

    public boolean isActive(String sessionId) {
        return this.activeSessions.contains(sessionId);
    }

    public boolean checkForOpenConnections() {
        return !this.activeSessions.isEmpty();
    }

}