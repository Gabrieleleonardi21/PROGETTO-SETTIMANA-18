package it.epicode.u5d7.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Il canale in tempo reale: un endpoint STOMP su WebSocket e un broker in memoria.
 * Destinazioni usate:
 *   /app/messaggi            client -> server: invio di un nuovo messaggio (@MessageMapping)
 *   /user/queue/messaggi     server -> client: coda personale, riceve solo il proprietario
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final List<String> allowedOrigins;
	private final StompAuthInterceptor authInterceptor;

	public WebSocketConfig(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins,
			StompAuthInterceptor authInterceptor) {
		this.allowedOrigins = allowedOrigins;
		this.authInterceptor = authInterceptor;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// setAllowedOrigins e' obbligatorio: senza, l'handshake risponde 403 e in console
		// del browser si legge solo "WebSocket connection failed".
		// Niente SockJS: il client si collega con ws://localhost:3001/ws diretto.
		registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins.toArray(String[]::new));
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// I frame SEND verso /app/** finiscono ai metodi @MessageMapping, non al broker
		registry.setApplicationDestinationPrefixes("/app");
		// Broker in memoria: smista tutto cio' che parte verso /queue
		registry.enableSimpleBroker("/queue");
		// Il client si iscrive a /user/queue/messaggi e Spring lo traduce in una coda
		// privata della sua sessione: nessun altro puo' iscriversi alla coda di un altro utente
		registry.setUserDestinationPrefix("/user");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		// Tutto quello che arriva dai client passa di qui: e' dove il CONNECT riceve il Principal
		registration.interceptors(authInterceptor);
	}
}
