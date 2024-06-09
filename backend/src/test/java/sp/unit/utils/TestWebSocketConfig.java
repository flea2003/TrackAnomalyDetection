package sp.unit.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import static org.assertj.core.api.Assertions.assertThat;
import sp.config.WebSocketConfig;

import static org.mockito.Mockito.*;

public class TestWebSocketConfig {
    WebSocketConfig webSocketConfig = new WebSocketConfig();
    ArgumentCaptor<String> stringArgumentCaptor;
    ArgumentCaptor<String[]> stringArrayArgCaptor;

    @BeforeEach
    public void setUp() {
        stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        stringArrayArgCaptor = ArgumentCaptor.forClass(String[].class);

    }

    @Test
    public void testConfigureMessageBroker() {
        SubscribableChannel subscribableChannel = mock(SubscribableChannel.class);
        MessageChannel messageChannel = mock(MessageChannel.class);
        MessageBrokerRegistry messageBrokerRegistry = new MessageBrokerRegistry(subscribableChannel, messageChannel);
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);
    }

    @Test
    public void testRegisterSTOMPEndpoints() {
        StompEndpointRegistry stompEndpointRegistry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration handlerRegistration = mock(StompWebSocketEndpointRegistration.class);
        when(stompEndpointRegistry.addEndpoint(stringArgumentCaptor.capture())).thenReturn(handlerRegistration);
        when(handlerRegistration.setAllowedOriginPatterns(stringArrayArgCaptor.capture())).thenReturn(handlerRegistration);

        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("/details");
        assertThat(stringArrayArgCaptor.getValue()[0]).isEqualTo("*");
    }

}
