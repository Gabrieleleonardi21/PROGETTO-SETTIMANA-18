package it.epicode.u5d7.payload;

import java.time.Instant;
import java.util.UUID;

import it.epicode.u5d7.model.Utente;

public record UtenteResponse(UUID id, String username, String email, Instant createdAt) {

	public static UtenteResponse da(Utente u) {
		return new UtenteResponse(u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt());
	}
}
