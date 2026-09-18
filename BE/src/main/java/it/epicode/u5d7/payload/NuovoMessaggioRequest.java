package it.epicode.u5d7.payload;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Il mittente NON c'e': viene sempre dal principal (HTTP o sessione STOMP). */
public record NuovoMessaggioRequest(
		@NotNull(message = "Il destinatario e' obbligatorio") UUID destinatarioId,
		@NotBlank(message = "Il messaggio non puo' essere vuoto") String testo) {
}
