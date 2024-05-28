package sp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import sp.utils.WebSocketSessionManager;

@Component
public class SubscribeEventListener implements ApplicationListener {

    private final WebSocketSessionManager sessionManager;

    @Autowired
    public SubscribeEventListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        SessionSubscribeEvent subEvent = (SessionSubscribeEvent) event;
        StompHeaderAccessor headerAccessor =
                StompHeaderAccessor.wrap(subEvent.getMessage());
        String sessionId = headerAccessor.getSessionId();
        this.sessionManager.addSession(sessionId);
        System.out.println("connection to " + sessionId);
    }
}
