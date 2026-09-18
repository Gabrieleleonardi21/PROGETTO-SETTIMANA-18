package it.epicode.u5d7.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import it.epicode.u5d7.model.Chat;
import it.epicode.u5d7.model.Messaggio;
import it.epicode.u5d7.model.StatoMessaggio;
import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.MessaggioResponse;
import it.epicode.u5d7.repository.MessaggioRepository;

@Service
public class MessaggioService {

	private final MessaggioRepository messaggioRepository;
	private final ChatService chatService;
	private final UtenteService utenteService;
	// Conosce le sessioni STOMP autenticate: dice se il destinatario e' collegato adesso
	private final SimpUserRegistry simpUserRegistry;

	public MessaggioService(MessaggioRepository messaggioRepository, ChatService chatService,
			UtenteService utenteService, SimpUserRegistry simpUserRegistry) {
		this.messaggioRepository = messaggioRepository;
		this.chatService = chatService;
		this.utenteService = utenteService;
		this.simpUserRegistry = simpUserRegistry;
	}

	/**
	 * PRIMA salva (id e sentAt li assegna il server), POI chi chiama consegna via WS.
	 * Il mittente arriva sempre dal principal del chiamante, mai dal body.
	 * Il DTO viene costruito qui, dentro la transazione (open-in-view e' spento).
	 */
	@Transactional
	public MessaggioResponse invia(UUID mittenteId, UUID destinatarioId, String testo) {
		// Validazione manuale: sul canale STOMP @Valid non e' garantito come su HTTP
		if (testo == null || testo.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il messaggio non puo' essere vuoto");
		}
		Utente mittente = utenteService.trova(mittenteId);
		Utente destinatario = utenteService.trova(destinatarioId);
		Chat chat = chatService.trovaOCrea(mittente, destinatario);

		// Se il destinatario ha una sessione WS aperta il push che segue lo raggiunge: nasce gia' CONSEGNATO.
		// Se si disconnette fra questo controllo e il push, il messaggio resta comunque a DB e lo
		// trovera' nella cronologia alla prossima apertura della chat.
		boolean online = simpUserRegistry.getUser(destinatarioId.toString()) != null;
		StatoMessaggio stato = StatoMessaggio.SPEDITO;
		Instant deliveredAt = null;
		if (online) {
			stato = StatoMessaggio.CONSEGNATO;
			deliveredAt = Instant.now();
		}
		Messaggio salvato = messaggioRepository
				.save(new Messaggio(chat, mittente, destinatario, testo.trim(), stato, deliveredAt));
		return MessaggioResponse.da(salvato);
	}

	/** Pagina 0 = messaggi piu' recenti (sentAt DESC): il client la inverte per mostrarli in ordine. */
	@Transactional(readOnly = true)
	public Page<MessaggioResponse> cronologia(UUID chatId, UUID meId, int page, int size) {
		chatService.diPartecipante(chatId, meId);
		Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
		return messaggioRepository.findByChat_Id(chatId, pageable).map(MessaggioResponse::da);
	}

	/**
	 * Ultimi N messaggi della chat in ordine cronologico, come righe "username: testo".
	 * Serve da contesto per il suggerimento dell'IA; lo username viene letto qui, dentro la transazione.
	 */
	@Transactional(readOnly = true)
	public List<String> trascrizione(UUID chatId, UUID meId, int quanti) {
		chatService.diPartecipante(chatId, meId);
		Pageable pageable = PageRequest.of(0, quanti, Sort.by("sentAt").descending());
		List<Messaggio> recenti = new ArrayList<>(messaggioRepository.findByChat_Id(chatId, pageable).getContent());
		Collections.reverse(recenti);
		return recenti.stream().map(m -> m.getMittente().getUsername() + ": " + m.getTesto()).toList();
	}

	/**
	 * Chiamato dal destinatario quando apre la chat: segna consegnati i messaggi ricevuti mentre era offline.
	 * Restituisce i messaggi aggiornati cosi' chi chiama puo' avvisare il mittente via WS.
	 * Le entita' sono gestite dalla transazione: l'UPDATE lo fa Hibernate al commit (dirty checking).
	 */
	@Transactional
	public List<MessaggioResponse> segnaConsegnati(UUID chatId, UUID meId) {
		chatService.diPartecipante(chatId, meId);
		Instant adesso = Instant.now();
		List<Messaggio> daAggiornare = messaggioRepository.findByChat_IdAndDestinatario_IdAndStatus(chatId, meId,
				StatoMessaggio.SPEDITO);
		for (Messaggio m : daAggiornare) {
			m.setStatus(StatoMessaggio.CONSEGNATO);
			m.setDeliveredAt(adesso);
		}
		return daAggiornare.stream().map(MessaggioResponse::da).toList();
	}
}
