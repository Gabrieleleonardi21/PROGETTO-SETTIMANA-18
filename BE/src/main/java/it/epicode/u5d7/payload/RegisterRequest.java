package it.epicode.u5d7.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Lo username e' obbligatorio") @Size(min = 3, max = 30, message = "Lo username deve avere tra 3 e 30 caratteri") String username,
		@NotBlank(message = "L'email e' obbligatoria") @Email(message = "Email non valida") String email,
		@NotBlank(message = "La password e' obbligatoria") @Size(min = 8, message = "La password deve avere almeno 8 caratteri") String password) {
}
