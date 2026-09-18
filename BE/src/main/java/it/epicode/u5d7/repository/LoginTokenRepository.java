package it.epicode.u5d7.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import it.epicode.u5d7.model.LoginToken;

public interface LoginTokenRepository extends JpaRepository<LoginToken, UUID> {

	// Cerca il codice per valore e email dell'utente collegato (join automatica su utente.email)
	Optional<LoginToken> findByValueAndUtenteEmail(int value, String email);
}
