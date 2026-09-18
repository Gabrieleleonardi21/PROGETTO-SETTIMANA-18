package it.epicode.u5d7.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Conversazione fra due utenti. Ne esiste UNA sola per coppia: il costruttore
 * ordina i due partecipanti per id, cosi' (A,B) e (B,A) producono la stessa riga
 * e il vincolo UNIQUE sulle due colonne impedisce i duplicati anche a DB.
 */
@Entity
@Table(name = "chat", uniqueConstraints = @UniqueConstraint(columnNames = { "part1_id", "part2_id" }))
@Getter
@Setter
public class Chat {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "part1_id")
	private Utente part1;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "part2_id")
	private Utente part2;

	protected Chat() {
	}

	public Chat(Utente a, Utente b) {
		// L'id minore va sempre in part1: e' l'unica regola di normalizzazione della coppia
		if (a.getId().compareTo(b.getId()) < 0) {
			this.part1 = a;
			this.part2 = b;
		} else {
			this.part1 = b;
			this.part2 = a;
		}
	}
}
