package it.epicode.u5d7.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import it.epicode.u5d7.payload.MessaggioLlm;
import it.epicode.u5d7.payload.RichiestaLlm;
import it.epicode.u5d7.payload.RispostaOpenRouter;

/**
 * Chiamata HTTP al servizio LLM (OpenRouter). Volutamente senza transazioni: una richiesta puo' durare
 * decine di secondi e non deve tenere occupata una connessione al database.
 */
@Service
public class ClientLlm {

	private static final Logger log = LoggerFactory.getLogger(ClientLlm.class);
	private static final int TENTATIVI = 3;
	private static final long ATTESA_MS = 1000;

	private final RestClient restClient;
	// Elenco in ordine di preferenza (proprieta' separata da virgole): il primo e' il modello principale
	private final List<String> modelli;

	public ClientLlm(RestClient llmRestClient, @Value("${app.llm.modelli}") List<String> modelli) {
		this.restClient = llmRestClient;
		this.modelli = modelli;
	}

	/**
	 * Fino a 3 tentativi; Optional vuoto se falliscono tutti.
	 * Nessuna eccezione esce da qui: cosa fare in caso di errore lo decide chi chiama.
	 */
	public Optional<String> completa(List<MessaggioLlm> messaggi) {
		RichiestaLlm richiesta = new RichiestaLlm(modelli.get(0), modelli, messaggi);
		for (int tentativo = 1; tentativo <= TENTATIVI; tentativo++) {
			try {
				return Optional.of(chiama(richiesta));
			} catch (RuntimeException e) {
				// RestClientException (timeout, 4xx/5xx) e IllegalStateException (risposta malformata) sono entrambe RuntimeException
				log.warn("Chiamata LLM fallita (tentativo {}/{}): {}", tentativo, TENTATIVI, e.getMessage());
				if (tentativo < TENTATIVI) {
					attendi();
				}
			}
		}
		return Optional.empty();
	}

	private String chiama(RichiestaLlm richiesta) {
		RispostaOpenRouter r = restClient.post()
				.uri("/chat/completions")
				.contentType(MediaType.APPLICATION_JSON)
				.body(richiesta)
				.retrieve()
				.body(RispostaOpenRouter.class);
		if (r == null || r.choices() == null || r.choices().isEmpty()) {
			throw new IllegalStateException("Risposta LLM incompleta");
		}
		String testo = r.choices().get(0).message().content();
		if (testo == null || testo.isBlank()) {
			throw new IllegalStateException("Risposta LLM vuota");
		}
		return testo.trim();
	}

	// Piccola pausa tra un tentativo e l'altro: da' tempo al servizio di riprendersi (es. rate limit)
	private void attendi() {
		try {
			Thread.sleep(ATTESA_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
