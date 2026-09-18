package it.epicode.u5d7.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import it.epicode.u5d7.model.Utente;

/**
 * Crea e verifica i token JWT. Il subject del token e' l'id dell'utente;
 * la firma e' HMAC con il segreto letto da app.jwt.secret.
 */
@Component
public class JwtTool {

	private final SecretKey chiave;
	private final long durataMs;

	public JwtTool(@Value("${app.jwt.secret}") String segreto,
			@Value("${app.jwt.expiration-ms}") long durataMs) {
		// Keys.hmacShaKeyFor richiede almeno 32 byte (256 bit)
		this.chiave = Keys.hmacShaKeyFor(segreto.getBytes(StandardCharsets.UTF_8));
		this.durataMs = durataMs;
	}

	public String genera(Utente utente) {
		Instant adesso = Instant.now();
		return Jwts.builder()
				.subject(utente.getId().toString())
				.issuedAt(Date.from(adesso))
				.expiration(Date.from(adesso.plusMillis(durataMs)))
				.signWith(chiave)
				.compact();
	}

	/** Verifica firma e scadenza e restituisce i claims (401 se il token non e' valido). */
	public Claims verifica(String token) {
		try {
			return Jwts.parser().verifyWith(chiave).build().parseSignedClaims(token).getPayload();
		} catch (JwtException | IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido o scaduto");
		}
	}

	public UUID estraiIdUtente(Claims claims) {
		return UUID.fromString(claims.getSubject());
	}
}
