package it.epicode.u5d7.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import it.epicode.u5d7.config.IstruzioniSuggeritore;
import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.MessaggioLlm;
import it.epicode.u5d7.payload.SuggerimentoResponse;

/**
 * Consegna, punto 2: si puo' chiedere all'IA di proporre un messaggio per continuare
 * la conversazione e la risposta dell'IA NON deve essere salvata nel DB.
 * Il suggerimento viene solo restituito al client, che decide se inviarlo: qui non c'e' nessuna save().
 * Senza @Transactional: la lettura della cronologia e' una transazione breve di
 * MessaggioService, mentre la chiamata all'LLM resta fuori da qualsiasi transazione.
 */
@Service
public class SuggerimentoService {

	private final MessaggioService messaggioService;
	private final ClientLlm clientLlm;
	private final IstruzioniSuggeritore istruzioni;
	private final int maxMessaggiContesto;

	public SuggerimentoService(MessaggioService messaggioService, ClientLlm clientLlm,
			IstruzioniSuggeritore istruzioni, @Value("${app.llm.max-messaggi-contesto}") int maxMessaggiContesto) {
		this.messaggioService = messaggioService;
		this.clientLlm = clientLlm;
		this.istruzioni = istruzioni;
		this.maxMessaggiContesto = maxMessaggiContesto;
	}

	public SuggerimentoResponse suggerisci(UUID chatId, Utente me) {
		List<String> righe = messaggioService.trascrizione(chatId, me.getId(), maxMessaggiContesto);
		String testo = clientLlm.completa(costruisciContesto(righe, me.getUsername()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
						"L'IA non e' disponibile al momento, riprova piu' tardi"));
		return new SuggerimentoResponse(testo);
	}

	// system = regole fisse dal file; user = per chi suggerire + trascrizione degli ultimi messaggi
	private List<MessaggioLlm> costruisciContesto(List<String> righe, String username) {
		StringBuilder sb = new StringBuilder("Suggerisci il prossimo messaggio per l'utente \"").append(username)
				.append("\".\n\n");
		if (righe.isEmpty()) {
			sb.append("La conversazione e' ancora vuota.");
		} else {
			sb.append("Trascrizione:\n").append(String.join("\n", righe));
		}
		return List.of(new MessaggioLlm("system", istruzioni.testo()), new MessaggioLlm("user", sb.toString()));
	}
}
