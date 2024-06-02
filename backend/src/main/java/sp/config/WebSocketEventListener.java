package sp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import sp.utils.WebSocketSessionManager;

@Component
public class WebSocketEventListener implements ApplicationListener<ApplicationEvent> {

    private final WebSocketSessionManager sessionManager;

    @Autowired
    public WebSocketEventListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        if (event instanceof SessionSubscribeEvent) {
            this.handleClientSubscriptionEvent(event);
        } else if (event instanceof SessionDisconnectEvent) {
            this.handleClientDisconnectEvent(event);
        }
    }

    /**
     * Unregister client (session) upon a detected disconnection event.
     *
     * @param event recorded ApplicationEvent instance
     */
    private void handleClientDisconnectEvent(ApplicationEvent event) {
        SessionDisconnectEvent disconnectEvent = (SessionDisconnectEvent) event;
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(disconnectEvent.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (this.sessionManager.isActive(sessionId)) {
            this.sessionManager.removeSession(sessionId);
        }
    }

    /**
     * Register new client (session) upon a detected subscription request.
     *
     * @param event recorded ApplicationEvent instance
     */
    private void handleClientSubscriptionEvent(ApplicationEvent event) {
        SessionSubscribeEvent subEvent = (SessionSubscribeEvent) event;
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(subEvent.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (!this.sessionManager.isActive(sessionId)) {
            this.sessionManager.addSession(sessionId);
        }
    }

}
