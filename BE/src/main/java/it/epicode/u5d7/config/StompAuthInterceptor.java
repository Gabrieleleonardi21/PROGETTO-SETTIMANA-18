package it.epicode.u5d7.config;

import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import it.epicode.u5d7.security.JwtTool;
import it.epicode.u5d7.security.TokenBlacklistService;
import it.epicode.u5d7.security.UtentePrincipal;

/**
 * Da' un'identita' alla sessione WebSocket.
 *
 * L'handshake HTTP del browser non puo' portare l'header Authorization, quindi il
 * JWT viaggia in un header del primo frame STOMP (CONNECT). Qui lo verifichiamo e
 * il suo subject (l'id utente) diventa il Principal della sessione: da quel momento
 * convertAndSendToUser(id, ...) sa a chi consegnare.
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

	private static final String PREFISSO = "Bearer ";

	private final JwtTool jwtTool;
	private final TokenBlacklistService blacklist;

	public StompAuthInterceptor(JwtTool jwtTool, TokenBlacklistService blacklist) {
		this.jwtTool = jwtTool;
		this.blacklist = blacklist;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		// getAccessor restituisce l'accessor VERO del messaggio. StompHeaderAccessor.wrap
		// ne creerebbe una copia: setUser non avrebbe effetto e nessuno lo segnalerebbe.
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
			return message;
		}

		// MessagingException arriva al client come frame ERROR con questo testo:
		// niente sessioni anonime, che riceverebbero i messaggi privati in silenzio
		String header = accessor.getFirstNativeHeader("Authorization");
		if (header == null || !header.startsWith(PREFISSO)) {
			throw new MessagingException("Token JWT mancante");
		}
		String token = header.substring(PREFISSO.length()).trim();
		if (blacklist.contiene(token)) {
			throw new MessagingException("Token non valido: logout effettuato");
		}
		try {
			UUID id = jwtTool.estraiIdUtente(jwtTool.verifica(token));
			accessor.setUser(new UtentePrincipal(id));
		} catch (ResponseStatusException e) {
			throw new MessagingException(e.getReason());
		}
		return message;
	}
}
