package it.epicode.u5d7.model;

import java.time.Duration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Codice a 6 cifre inviato via email per il login senza password.
 * Vale 10 minuti e si usa una volta sola.
 */
@Entity
@Table(name = "login_tokens")
@Getter
@Setter
public class LoginToken extends TokenBase {

	// Codice numerico a 6 cifre (100000-999999)
	@Column(nullable = false)
	private int value;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "utente_id", nullable = false)
	private Utente utente;

	protected LoginToken() {
	}

	public LoginToken(int value, Utente utente) {
		this.value = value;
		this.utente = utente;
	}

	@Override
	protected Duration durata() {
		return Duration.ofMinutes(10);
	}
}
