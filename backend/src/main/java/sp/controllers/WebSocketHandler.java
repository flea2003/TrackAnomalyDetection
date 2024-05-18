package sp.controllers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sp.model.CurrentShipDetails;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class WebSocketHandler extends TextWebSocketHandler {

    private final static Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    public void sendShipDetailsToAllClients(CurrentShipDetails currentShipDetails) {
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                try {
                    session.sendMessage(new TextMessage(currentShipDetails.toString()));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

}
