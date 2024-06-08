package sp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure the STOMP message broker.
     *
     * @param registry message broker registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Expose the STOMP endpoint over WebSocket.
     *
     * @param registry STOMP endpoints managing entity
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/details")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Updates the WebSocket transport configuration.
     *
     * @param registration WebSocket transport registration
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(2 * 1024 * 1024); // 2MB
        registration.setSendBufferSizeLimit(2 * 1024 * 1024); // 2MB
        registration.setSendTimeLimit(20 * 10000); // 20 seconds
    }
}
