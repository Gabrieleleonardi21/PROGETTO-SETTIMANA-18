package it.epicode.u5d7.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import it.epicode.u5d7.config.NotificatoreWs;
import it.epicode.u5d7.payload.MessaggioResponse;
import it.epicode.u5d7.payload.NuovoMessaggioRequest;
import it.epicode.u5d7.service.MessaggioService;

/**
 * Invio di un messaggio dal canale WebSocket: il client fa SEND su /app/messaggi.
 * @Controller e non @RestController: qui non ci sono risposte HTTP.
 */
@Controller
public class MessaggioWsController {

	private final MessaggioService messaggioService;
	private final NotificatoreWs notificatore;

	public MessaggioWsController(MessaggioService messaggioService, NotificatoreWs notificatore) {
		this.messaggioService = messaggioService;
		this.notificatore = notificatore;
	}

	/**
	 * Il mittente e' il Principal della sessione STOMP (UtentePrincipal, messo dall'interceptor
	 * sul CONNECT): il body non puo' fingersi qualcun altro.
	 * Un errore qui (destinatario inesistente, testo vuoto) non ha una risposta da restituire:
	 * un SEND STOMP e' fire-and-forget. Il broker lo logga; la POST REST resta la via con 400/404.
	 */
	@MessageMapping("/messaggi")
	public void nuovo(@Payload NuovoMessaggioRequest body, Principal principal) {
		UUID mittenteId = UUID.fromString(principal.getName());
		MessaggioResponse salvato = messaggioService.invia(mittenteId, body.destinatarioId(), body.testo());
		notificatore.consegna(salvato);
	}
}
