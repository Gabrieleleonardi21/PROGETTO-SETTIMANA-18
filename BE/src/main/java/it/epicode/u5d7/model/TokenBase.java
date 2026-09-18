package it.epicode.u5d7.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Campi comuni ai token inviati via email (registrazione, login): id, date di
 * creazione/scadenza e flag "usato". Ogni sottoclasse dichiara la propria durata
 * tramite durata(); la scadenza viene calcolata al salvataggio.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TokenBase {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;

	@Column(name = "created_at", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant createdAt;

	@Column(name = "expire_at", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant expireAt;

	@Column(name = "is_used", nullable = false)
	private boolean isUsed = false;

	/** Quanto dura il token dal momento della creazione. */
	protected abstract Duration durata();

	public boolean isScaduto() {
		return Instant.now().isAfter(expireAt);
	}

	@PrePersist
	void impostaDate() {
		this.createdAt = Instant.now();
		this.expireAt = this.createdAt.plus(durata());
	}
}
