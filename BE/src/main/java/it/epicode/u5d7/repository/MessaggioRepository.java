package it.epicode.u5d7.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import it.epicode.u5d7.model.Messaggio;
import it.epicode.u5d7.model.StatoMessaggio;

public interface MessaggioRepository extends JpaRepository<Messaggio, UUID> {

	// L'ordinamento (sentAt DESC) arriva dal Pageable costruito nel service
	Page<Messaggio> findByChat_Id(UUID chatId, Pageable pageable);

	// I messaggi RICEVUTI da me in quella chat ancora SPEDITO: quelli inviati da me non li "consegno" io.
	// Lettura e non UPDATE massivo: servono le entita' per rimandare al mittente lo stato aggiornato
	List<Messaggio> findByChat_IdAndDestinatario_IdAndStatus(UUID chatId, UUID meId, StatoMessaggio status);

	// Statistiche personali: messaggi inviati e ricevuti dall'utente
	long countByMittente_Id(UUID mittenteId);

	long countByDestinatario_Id(UUID destinatarioId);
}
