package it.epicode.u5d7.security;

import java.security.Principal;
import java.util.UUID;

/**
 * Identita' della sessione STOMP (WebSocket).
 *
 * getName() DEVE restituire l'id dell'utente: e' la chiave con cui
 * SimpMessagingTemplate.convertAndSendToUser(id, ...) e SimpUserRegistry.getUser(id)
 * cercano le sessioni. Con un nome diverso (username, email, toString di
 * un'entita') i messaggi privati partirebbero verso code che nessuno ascolta,
 * senza alcun errore.
 */
public record UtentePrincipal(UUID id) implements Principal {

	@Override
	public String getName() {
		return id.toString();
	}
}
