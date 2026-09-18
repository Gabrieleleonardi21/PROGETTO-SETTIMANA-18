package it.epicode.u5d7.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Login con email e codice a 6 cifre ricevuto via email. */
public record LoginCodeRequest(
		@NotBlank(message = "L'email e' obbligatoria") @Email(message = "Email non valida") String email,
		@NotNull(message = "Il codice e' obbligatorio")
		@Min(value = 100000, message = "Il codice deve avere 6 cifre")
		@Max(value = 999999, message = "Il codice deve avere 6 cifre")
		Integer codice) {
}
