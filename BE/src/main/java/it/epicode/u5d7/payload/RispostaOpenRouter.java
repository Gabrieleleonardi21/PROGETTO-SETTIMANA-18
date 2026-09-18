package it.epicode.u5d7.payload;

import java.util.List;

/**
 * Solo i campi della risposta OpenRouter che usiamo: gli altri (id, model, usage...)
 * vengono ignorati, perche' Jackson non fallisce sulle proprieta' sconosciute.
 */
public record RispostaOpenRouter(List<Scelta> choices) {

	public record Scelta(MessaggioLlm message) {
	}
}
