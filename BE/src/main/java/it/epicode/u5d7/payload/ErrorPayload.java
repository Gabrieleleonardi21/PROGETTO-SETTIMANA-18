package it.epicode.u5d7.payload;

import java.time.Instant;

/** Corpo di ogni risposta di errore: status HTTP, messaggio leggibile e timestamp. */
public record ErrorPayload(int status, String messaggio, Instant timestamp) {

	public ErrorPayload(int status, String messaggio) {
		this(status, messaggio, Instant.now());
	}
}
