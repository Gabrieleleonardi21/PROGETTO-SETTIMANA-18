package it.epicode.u5d7.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Token JWT invalidati dal logout. Un JWT e' valido finche' non scade, quindi per
 * "spegnerlo" prima lo ricordiamo qui fino alla sua scadenza naturale.
 * In memoria: si svuota al riavvio, sufficiente per questo progetto.
 */
@Service
public class TokenBlacklistService {

	// token -> istante di scadenza del token
	private final Map<String, Instant> tokenInvalidati = new ConcurrentHashMap<>();

	public void aggiungi(String token, Instant scadenza) {
		pulisciScaduti();
		tokenInvalidati.put(token, scadenza);
	}

	public boolean contiene(String token) {
		return tokenInvalidati.containsKey(token);
	}

	// I token gia' scaduti non passerebbero comunque la verifica: inutile tenerli
	private void pulisciScaduti() {
		Instant adesso = Instant.now();
		tokenInvalidati.entrySet().removeIf(voce -> voce.getValue().isBefore(adesso));
	}
}
