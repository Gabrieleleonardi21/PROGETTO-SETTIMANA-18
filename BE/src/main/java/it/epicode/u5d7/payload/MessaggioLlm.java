package it.epicode.u5d7.payload;

/** Un messaggio nel formato OpenAI/OpenRouter: role = system | user | assistant. */
public record MessaggioLlm(String role, String content) {
}
