package it.epicode.u5d7.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import it.epicode.u5d7.model.Utente;

public interface UtenteRepository extends JpaRepository<Utente, UUID> {

	Optional<Utente> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	// "Le persone con cui puoi parlare": tutti tranne chi chiede, solo se hanno confermato l'email
	List<Utente> findByIdNotAndIsActiveTrueOrderByUsernameAsc(UUID id);
}
