package sp.unit.utils.websockets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import sp.utils.websockets.WebSocketEventListener;
import sp.utils.websockets.WebSocketSessionManager;

import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class WebSocketEventListenerTest {
    WebSocketEventListener webSocketEventListener;
    WebSocketSessionManager webSocketSessionManager;

    @BeforeEach
    void setUp() {
        webSocketSessionManager = new WebSocketSessionManager();
        webSocketEventListener = new WebSocketEventListener(webSocketSessionManager);
    }

    @AfterEach
    void clean() {
        webSocketSessionManager = null;
        webSocketEventListener = null;
    }

    @Test
    public void sessionSubscribeEventNewClientTest() {

        assertThat(webSocketSessionManager.isActive("s1")).isFalse();

        /*
        Create the mock headers of the subscription message
         */
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        stompHeaderAccessor.setSessionId("s1");
        Map<String, Object> headers = new HashMap<>(stompHeaderAccessor.toMap());

        Message<byte[]> subMessage = new GenericMessage<>(new byte[0], headers);

        SessionSubscribeEvent sessionSubscribeEvent = new SessionSubscribeEvent(this, subMessage);

        webSocketEventListener.onApplicationEvent(sessionSubscribeEvent);

        assertThat(webSocketSessionManager.isActive("s1")).isTrue();

    }

    @Test
    public void sessionSubscribeEventExistingClientTest() {
        webSocketSessionManager.addSession("s1");

        /*
        Create the mock headers of the subscription message
         */
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        stompHeaderAccessor.setSessionId("s1");
        Map<String, Object> headers = new HashMap<>(stompHeaderAccessor.toMap());

        Message<byte[]> subMessage = new GenericMessage<>(new byte[0], headers);

        SessionSubscribeEvent sessionSubscribeEvent = new SessionSubscribeEvent(this, subMessage);

        webSocketEventListener.onApplicationEvent(sessionSubscribeEvent);

        assertThat(webSocketSessionManager.isActive("s1")).isTrue();
    }

    @Test
    public void sessionDisconnectEventConnectedClientTest() {
        webSocketSessionManager.addSession("s1");
        assertThat(webSocketSessionManager.isActive("s1")).isTrue();


        // Create the mock headers of the subscription message
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        stompHeaderAccessor.setSessionId("s1");
        Map<String, Object> headers = new HashMap<>(stompHeaderAccessor.toMap());

        Message<byte[]> disconnectMessage = new GenericMessage<>(new byte[0], headers);

        SessionDisconnectEvent sessionDisconnectEvent
                = new SessionDisconnectEvent(this, disconnectMessage, "s1", new CloseStatus(1006));

        webSocketEventListener.onApplicationEvent(sessionDisconnectEvent);

        assertThat(webSocketSessionManager.isActive("s1")).isFalse();
    }

    @Test
    public void sessionDisconnectEventUnknownClientTest() {
        assertThat(webSocketSessionManager.isActive("s1")).isFalse();

        // Create the mock headers of the subscription message
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        stompHeaderAccessor.setSessionId("s1");
        Map<String, Object> headers = new HashMap<>(stompHeaderAccessor.toMap());

        Message<byte[]> disconnectMessage = new GenericMessage<>(new byte[0], headers);

        SessionDisconnectEvent sessionDisconnectEvent
                = new SessionDisconnectEvent(this, disconnectMessage, "s1", new CloseStatus(1006));

        webSocketEventListener.onApplicationEvent(sessionDisconnectEvent);

    }

    @Test
    public void otherSessionEventTest() {
        assertThat(webSocketSessionManager.isActive("s1")).isFalse();

        // Create the mock headers of the subscription message
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        stompHeaderAccessor.setSessionId("s1");
        Map<String, Object> headers = new HashMap<>(stompHeaderAccessor.toMap());

        Message<byte[]> message = new GenericMessage<>(new byte[0], headers);

        ApplicationEvent sessionDisconnectEvent
                = new SessionConnectEvent(this, message);

        webSocketEventListener.onApplicationEvent(sessionDisconnectEvent);
    }

}
