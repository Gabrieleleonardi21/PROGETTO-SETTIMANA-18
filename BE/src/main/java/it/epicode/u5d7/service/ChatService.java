package it.epicode.u5d7.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import it.epicode.u5d7.model.Chat;
import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.ChatResponse;
import it.epicode.u5d7.repository.ChatRepository;

@Service
public class ChatService {

	private final ChatRepository chatRepository;
	private final UtenteService utenteService;

	public ChatService(ChatRepository chatRepository, UtenteService utenteService) {
		this.chatRepository = chatRepository;
		this.utenteService = utenteService;
	}

	/**
	 * Restituisce la chat fra i due utenti, creandola se non esiste (idempotente).
	 * Il costruttore di Chat ordina la coppia: cerchiamo con lo stesso ordine con cui salveremmo.
	 * Due creazioni simultanee della stessa coppia sono fermate dal vincolo UNIQUE a DB.
	 */
	@Transactional
	public Chat trovaOCrea(Utente a, Utente b) {
		if (a.getId().equals(b.getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Non puoi aprire una chat con te stesso");
		}
		Chat candidata = new Chat(a, b);
		return chatRepository.findByPart1_IdAndPart2_Id(candidata.getPart1().getId(), candidata.getPart2().getId())
				.orElseGet(() -> chatRepository.save(candidata));
	}

	@Transactional
	public ChatResponse creaOTrova(Utente me, UUID destinatarioId) {
		Chat chat = trovaOCrea(me, utenteService.trova(destinatarioId));
		return ChatResponse.da(chat, me.getId());
	}

	@Transactional(readOnly = true)
	public List<ChatResponse> chatDi(Utente me) {
		return chatRepository.findDiUtente(me.getId()).stream().map(c -> ChatResponse.da(c, me.getId())).toList();
	}

	/** La chat esiste (404) e chi chiede ne fa parte (403): usato da cronologia e PATCH. */
	@Transactional(readOnly = true)
	public Chat diPartecipante(UUID chatId, UUID meId) {
		Chat chat = chatRepository.findById(chatId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat non trovata"));
		boolean partecipa = chat.getPart1().getId().equals(meId) || chat.getPart2().getId().equals(meId);
		if (!partecipa) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non fai parte di questa chat");
		}
		return chat;
	}
}
