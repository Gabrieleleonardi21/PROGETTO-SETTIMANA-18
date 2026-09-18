package it.epicode.u5d7.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Richiesta di invio del codice di login via email. */
public record EmailRequest(
		@NotBlank(message = "L'email e' obbligatoria") @Email(message = "Email non valida") String email) {
}
