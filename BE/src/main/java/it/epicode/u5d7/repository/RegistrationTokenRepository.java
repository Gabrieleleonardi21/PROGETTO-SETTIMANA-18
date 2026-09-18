package it.epicode.u5d7.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import it.epicode.u5d7.model.RegistrationToken;

public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, UUID> {

	// Cerca il token per valore e email dell'utente collegato (join automatica su utente.email)
	Optional<RegistrationToken> findByValueAndUtenteEmail(String value, String email);
}
