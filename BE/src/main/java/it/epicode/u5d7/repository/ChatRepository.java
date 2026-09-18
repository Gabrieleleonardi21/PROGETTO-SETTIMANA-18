package it.epicode.u5d7.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.epicode.u5d7.model.Chat;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

	// Da chiamare con la coppia gia' ordinata (id minore in part1), come fa il costruttore di Chat
	Optional<Chat> findByPart1_IdAndPart2_Id(UUID part1Id, UUID part2Id);

	// JOIN FETCH: il DTO legge gli username dei due partecipanti e open-in-view e' spento
	@Query("SELECT c FROM Chat c JOIN FETCH c.part1 JOIN FETCH c.part2 WHERE c.part1.id = :id OR c.part2.id = :id")
	List<Chat> findDiUtente(@Param("id") UUID id);

	// Statistiche personali: numero di chat aperte dall'utente (da una parte o dall'altra)
	long countByPart1_IdOrPart2_Id(UUID part1Id, UUID part2Id);
}
