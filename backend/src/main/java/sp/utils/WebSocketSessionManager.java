package sp.utils;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class WebSocketSessionManager {
    private List<String> activeSessions = Collections.synchronizedList(new ArrayList<>());


    public void addSession(String sessionId) {
        this.activeSessions.add(sessionId);
    }

    public void removeSession(String sessionId) {
        this.activeSessions.remove(sessionId);
    }

    public boolean checkForOpenConnections() {
        return !this.activeSessions.isEmpty();
    }

}