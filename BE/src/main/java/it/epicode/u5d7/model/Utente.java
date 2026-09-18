package it.epicode.u5d7.model;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Utente registrato. "utenti" e non "user": user e' una parola riservata in Postgres.
 * Nasce con isActive = false e viene attivato solo aprendo il link di verifica ricevuto via email.
 */
@Entity
@Table(name = "utenti")
@Getter
@Setter
public class Utente {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false, unique = true)
	private String email;

	// Mai in output: la password e' salvata con BCrypt ma non deve comunque uscire
	@JsonIgnore
	@Column(nullable = false)
	private String password;

	@Column(name = "created_at", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant createdAt;

	// ColumnDefault: con ddl-auto=update la colonna si aggiunge anche se in tabella ci sono gia' righe
	@Column(name = "is_active", nullable = false)
	@ColumnDefault("false")
	private boolean isActive = false;

	protected Utente() {
	}

	public Utente(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
	}

	// L'istante di creazione lo decide il server, non il client
	@PrePersist
	void primaDiSalvare() {
		this.createdAt = Instant.now();
	}
}
