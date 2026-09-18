package it.epicode.u5d7.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.RegisterRequest;
import it.epicode.u5d7.payload.UtenteResponse;
import it.epicode.u5d7.repository.UtenteRepository;
import it.epicode.u5d7.security.JwtTool;
import it.epicode.u5d7.security.TokenBlacklistService;

/** Registrazione, login/logout e lista degli utenti con cui si puo' chattare. */
@Service
public class UtenteService {

	private final UtenteRepository utenteRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTool jwtTool;
	private final TokenBlacklistService blacklist;

	public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder, JwtTool jwtTool,
			TokenBlacklistService blacklist) {
		this.utenteRepository = utenteRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTool = jwtTool;
		this.blacklist = blacklist;
	}

	@Transactional
	public UtenteResponse registra(RegisterRequest dati) {
		String email = dati.email().toLowerCase();
		if (utenteRepository.existsByUsername(dati.username())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username gia' in uso");
		}
		if (utenteRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email gia' registrata");
		}
		Utente utente = new Utente(dati.username(), email, passwordEncoder.encode(dati.password()));
		return UtenteResponse.da(utenteRepository.save(utente));
	}

	/** Restituisce il JWT. Stesso messaggio per email o password errate: non si rivela chi e' registrato. */
	public String login(String email, String password) {
		Utente utente = utenteRepository.findByEmail(email.toLowerCase())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide"));
		if (!passwordEncoder.matches(password, utente.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
		}
		return jwtTool.genera(utente);
	}

	/** Mette il token in blacklist fino alla sua scadenza naturale. */
	public void logout(String token) {
		Instant scadenza = jwtTool.verifica(token).getExpiration().toInstant();
		blacklist.aggiungi(token, scadenza);
	}

	@Transactional(readOnly = true)
	public List<UtenteResponse> altriUtenti(UUID meId) {
		return utenteRepository.findByIdNotOrderByUsernameAsc(meId).stream().map(UtenteResponse::da).toList();
	}

	@Transactional(readOnly = true)
	public Utente trova(UUID id) {
		return utenteRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
	}
}
