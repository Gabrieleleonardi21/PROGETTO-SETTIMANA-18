package it.epicode.u5d7.payload;

import java.util.List;

/**
 * Corpo della POST /chat/completions. "models" e' l'estensione OpenRouter per il fallback:
 * prova i modelli in ordine e passa al successivo se uno e' saturo o non disponibile.
 */
public record RichiestaLlm(String model, List<String> models, List<MessaggioLlm> messages) {
}
