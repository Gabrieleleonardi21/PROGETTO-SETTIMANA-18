package it.epicode.u5d7.config;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import it.epicode.u5d7.payload.MessaggioResponse;

/**
 * Consegna un messaggio gia' salvato sulle code personali di destinatario e mittente.
 * Va chiamato DOPO il return del service transazionale: a quel punto il commit e'
 * avvenuto e il messaggio esiste sicuramente a DB. Chi non e' collegato non riceve
 * nulla, ma lo trovera' nella cronologia REST.
 */
@Component
public class NotificatoreWs {

	private static final String CODA = "/queue/messaggi";

	private final SimpMessagingTemplate template;

	public NotificatoreWs(SimpMessagingTemplate template) {
		this.template = template;
	}

	public void consegna(MessaggioResponse m) {
		// Il nome utente e' l'id: coincide con UtentePrincipal.getName()
		template.convertAndSendToUser(m.destinatarioId().toString(), CODA, m);
		// Anche al mittente: cosi' vede id, sentAt e stato decisi dal server (e le sue altre schede aperte si allineano)
		template.convertAndSendToUser(m.mittenteId().toString(), CODA, m);
	}

	/**
	 * Il destinatario ha aperto la chat: i messaggi sono passati a CONSEGNATO.
	 * Li rimandiamo al mittente sulla stessa coda: stesso id, stato nuovo. Il client
	 * li unisce per id e cosi' le spunte si aggiornano senza riaprire la chat.
	 */
	public void aggiornaStato(List<MessaggioResponse> aggiornati) {
		for (MessaggioResponse m : aggiornati) {
			template.convertAndSendToUser(m.mittenteId().toString(), CODA, m);
		}
	}
}
