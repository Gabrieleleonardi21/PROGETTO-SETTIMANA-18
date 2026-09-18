package it.epicode.u5d7.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Email e codice presi dal link di verifica inviato via mail. */
public record VerifyRequest(
		@NotBlank(message = "L'email e' obbligatoria") @Email(message = "Email non valida") String email,
		@NotBlank(message = "Il codice e' obbligatorio")
		@Size(min = 32, max = 32, message = "Codice di verifica non valido")
		String codice) {
}
