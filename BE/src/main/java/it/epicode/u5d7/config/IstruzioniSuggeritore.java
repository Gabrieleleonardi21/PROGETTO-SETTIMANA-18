package it.epicode.u5d7.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Regole dell'IA che suggerisce il prossimo messaggio, lette UNA volta all'avvio
 * dal file agente/suggeritore.txt. Il prompt vive nel file, mai nel codice Java.
 * Se il file manca o e' vuoto l'applicazione non parte.
 */
@Component
public class IstruzioniSuggeritore {

	private final String testo;

	public IstruzioniSuggeritore(@Value("classpath:agente/suggeritore.txt") Resource risorsa) throws IOException {
		if (!risorsa.exists()) {
			throw new IllegalStateException("File delle istruzioni dell'IA mancante: agente/suggeritore.txt");
		}
		String contenuto = risorsa.getContentAsString(StandardCharsets.UTF_8).trim();
		if (contenuto.isBlank()) {
			throw new IllegalStateException("File delle istruzioni dell'IA vuoto: agente/suggeritore.txt");
		}
		this.testo = contenuto;
	}

	public String testo() {
		return testo;
	}
}
