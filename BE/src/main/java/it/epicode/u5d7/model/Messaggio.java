package it.epicode.u5d7.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/** Un messaggio dentro una chat. id e sentAt li assegna il server al salvataggio. */
@Entity
@Table(name = "messaggi")
@Getter
@Setter
public class Messaggio {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "chat_id")
	private Chat chat;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mittente_id")
	private Utente mittente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "destinatario_id")
	private Utente destinatario;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String testo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatoMessaggio status;

	@Column(name = "sent_at", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant sentAt;

	// Null finche' il messaggio non risulta consegnato
	@Column(name = "delivered_at")
	private Instant deliveredAt;

	protected Messaggio() {
	}

	public Messaggio(Chat chat, Utente mittente, Utente destinatario, String testo, StatoMessaggio status,
			Instant deliveredAt) {
		this.chat = chat;
		this.mittente = mittente;
		this.destinatario = destinatario;
		this.testo = testo;
		this.status = status;
		this.deliveredAt = deliveredAt;
	}

	// L'ordine dei messaggi lo decide il server: sentAt viene assegnato qui, mai dal client
	@PrePersist
	void primaDiSalvare() {
		this.sentAt = Instant.now();
	}
}
