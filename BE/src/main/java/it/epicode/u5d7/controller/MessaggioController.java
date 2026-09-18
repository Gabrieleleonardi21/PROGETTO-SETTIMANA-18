package it.epicode.u5d7.controller;

import java.util.UUID;

import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.u5d7.config.NotificatoreWs;
import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.MessaggioResponse;
import it.epicode.u5d7.payload.NuovoMessaggioRequest;
import it.epicode.u5d7.service.MessaggioService;
import jakarta.validation.Valid;

/** Messaggi via REST: invio, cronologia paginata e conferma di consegna. */
@RestController
@RequestMapping("/api")
public class MessaggioController {

	private final MessaggioService messaggioService;
	private final NotificatoreWs notificatore;

	public MessaggioController(MessaggioService messaggioService, NotificatoreWs notificatore) {
		this.messaggioService = messaggioService;
		this.notificatore = notificatore;
	}

	/** Stesso flusso del canale WS: prima il salvataggio (transazione chiusa), poi il push. */
	@PostMapping("/messaggi")
	@ResponseStatus(HttpStatus.CREATED)
	public MessaggioResponse nuovo(@AuthenticationPrincipal Utente me, @RequestBody @Valid NuovoMessaggioRequest body) {
		MessaggioResponse salvato = messaggioService.invia(me.getId(), body.destinatarioId(), body.testo());
		notificatore.consegna(salvato);
		return salvato;
	}

	// PagedModel: JSON stabile { content: [...], page: { size, number, totalElements, totalPages } }
	@GetMapping("/chat/{chatId}/messaggi")
	public PagedModel<MessaggioResponse> cronologia(@AuthenticationPrincipal Utente me, @PathVariable UUID chatId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
		return new PagedModel<>(messaggioService.cronologia(chatId, me.getId(), page, size));
	}

	/**
	 * Il destinatario apre la chat: i suoi messaggi SPEDITO in quella chat diventano CONSEGNATO
	 * e il mittente, se collegato, riceve subito lo stato aggiornato via WS.
	 */
	@PatchMapping("/chat/{chatId}/messaggi/consegnati")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void consegnati(@AuthenticationPrincipal Utente me, @PathVariable UUID chatId) {
		notificatore.aggiornaStato(messaggioService.segnaConsegnati(chatId, me.getId()));
	}
}
