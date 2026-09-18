package it.epicode.u5d7.payload;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "L'email e' obbligatoria") String email,
		@NotBlank(message = "La password e' obbligatoria") String password) {
}
