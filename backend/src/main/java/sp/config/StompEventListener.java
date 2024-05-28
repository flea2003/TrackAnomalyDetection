package sp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import sp.utils.WebSocketSessionManager;

@Component
public class StompEventListener {

    private final WebSocketSessionManager sessionManager;

    @Autowired
    public StompEventListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventListener
    public void handleStompConnectionOpenEvents(SessionConnectedEvent sessionConnectedEvent) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(sessionConnectedEvent.getMessage());
        String sessionId = headerAccessor.getSessionId();
        System.out.println("connected sessionId: " + sessionId);
        sessionManager.addSession(sessionId);
    }

    @EventListener
    public void handleStompConnectionClosedEvents(SessionDisconnectEvent sessionDisconnectEvent) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(sessionDisconnectEvent.getMessage());
        String sessionId = headerAccessor.getSessionId();
        System.out.println("disconnected sessionId: " + sessionId);
        sessionManager.removeSession(sessionId);
    }
}
