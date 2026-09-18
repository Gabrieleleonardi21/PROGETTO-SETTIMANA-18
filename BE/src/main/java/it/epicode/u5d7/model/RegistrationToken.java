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
 * Token inviato via email dopo la registrazione: aprire il link con questo
 * valore attiva l'account. Vale 24 ore e si usa una volta sola.
 */
@Entity
@Table(name = "registration_tokens")
@Getter
@Setter
public class RegistrationToken extends TokenBase {

	// Stringa random di 32 caratteri esadecimali
	@Column(nullable = false, unique = true, length = 32)
	private String value;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "utente_id", nullable = false)
	private Utente utente;

	protected RegistrationToken() {
	}

	public RegistrationToken(String value, Utente utente) {
		this.value = value;
		this.utente = utente;
	}

	@Override
	protected Duration durata() {
		return Duration.ofHours(24);
	}
}
