package it.epicode.u5d7.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import it.epicode.u5d7.model.LoginToken;
import it.epicode.u5d7.model.RegistrationToken;
import it.epicode.u5d7.model.TokenBase;
import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.RegisterRequest;
import it.epicode.u5d7.payload.UtenteResponse;
import it.epicode.u5d7.repository.LoginTokenRepository;
import it.epicode.u5d7.repository.RegistrationTokenRepository;
import it.epicode.u5d7.repository.UtenteRepository;
import it.epicode.u5d7.security.JwtTool;
import it.epicode.u5d7.security.TokenBlacklistService;

/**
 * Registrazione con verifica via email, login (con password o con codice via email),
 * logout e lista degli utenti con cui si puo' chattare.
 * I metodi che salvano e poi inviano una mail sono @Transactional: se l'invio
 * fallisce, viene annullato anche il salvataggio.
 */
@Service
public class UtenteService {

	private final UtenteRepository utenteRepository;
	private final RegistrationTokenRepository registrationTokenRepository;
	private final LoginTokenRepository loginTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final CodiceService codiceService;
	private final EmailService emailService;
	private final JwtTool jwtTool;
	private final TokenBlacklistService blacklist;

	public UtenteService(UtenteRepository utenteRepository, RegistrationTokenRepository registrationTokenRepository,
			LoginTokenRepository loginTokenRepository, PasswordEncoder passwordEncoder, CodiceService codiceService,
			EmailService emailService, JwtTool jwtTool, TokenBlacklistService blacklist) {
		this.utenteRepository = utenteRepository;
		this.registrationTokenRepository = registrationTokenRepository;
		this.loginTokenRepository = loginTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.codiceService = codiceService;
		this.emailService = emailService;
		this.jwtTool = jwtTool;
		this.blacklist = blacklist;
	}

	/** Crea l'utente (non attivo) e manda il link di verifica. */
	@Transactional
	public UtenteResponse registra(RegisterRequest dati) {
		String email = dati.email().toLowerCase();
		if (utenteRepository.existsByUsername(dati.username())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username gia' in uso");
		}
		if (utenteRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email gia' registrata");
		}
		Utente utente = utenteRepository.save(new Utente(dati.username(), email, passwordEncoder.encode(dati.password())));

		RegistrationToken token = registrationTokenRepository.save(new RegistrationToken(codiceService.token32(), utente));
		emailService.inviaLinkVerifica(email, utente.getUsername(), token.getValue());
		return UtenteResponse.da(utente);
	}

	/** Attiva l'account se email e codice del link combaciano e il token e' ancora valido. */
	@Transactional
	public void verifica(String email, String codice) {
		RegistrationToken token = registrationTokenRepository.findByValueAndUtenteEmail(codice, email.toLowerCase())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link di verifica non valido"));
		controllaToken(token);
		token.setUsed(true);
		token.getUtente().setActive(true);
	}

	/** Restituisce il JWT. Stesso messaggio per email o password errate: non si rivela chi e' registrato. */
	public String login(String email, String password) {
		Utente utente = utenteRepository.findByEmail(email.toLowerCase())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide"));
		if (!passwordEncoder.matches(password, utente.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
		}
		controllaAttivo(utente);
		return jwtTool.genera(utente);
	}

	/**
	 * Genera un codice di login e lo spedisce. Se l'email non esiste o l'account
	 * non e' attivo non fa nulla e non lo dice: cosi' non si scopre chi e' registrato.
	 */
	@Transactional
	public void richiediCodiceLogin(String email) {
		utenteRepository.findByEmail(email.toLowerCase()).ifPresent(utente -> {
			if (!utente.isActive()) {
				return;
			}
			LoginToken token = loginTokenRepository.save(new LoginToken(codiceService.codice6Cifre(), utente));
			emailService.inviaCodiceLogin(utente.getEmail(), token.getValue());
		});
	}

	@Transactional
	public String loginCodice(String email, int codice) {
		LoginToken token = loginTokenRepository.findByValueAndUtenteEmail(codice, email.toLowerCase())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Codice non valido"));
		controllaToken(token);
		controllaAttivo(token.getUtente());
		token.setUsed(true);
		return jwtTool.genera(token.getUtente());
	}

	/** Mette il token in blacklist fino alla sua scadenza naturale. */
	public void logout(String token) {
		Instant scadenza = jwtTool.verifica(token).getExpiration().toInstant();
		blacklist.aggiungi(token, scadenza);
	}

	/** Le persone con cui si puo' parlare: gli altri utenti che hanno confermato l'email. */
	@Transactional(readOnly = true)
	public List<UtenteResponse> altriUtenti(UUID meId) {
		return utenteRepository.findByIdNotAndIsActiveTrueOrderByUsernameAsc(meId).stream().map(UtenteResponse::da)
				.toList();
	}

	@Transactional(readOnly = true)
	public Utente trova(UUID id) {
		return utenteRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
	}

	// Controlli comuni ai token (registrazione, login): gia' usato o scaduto
	private static void controllaToken(TokenBase token) {
		if (token.isUsed()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codice gia' utilizzato");
		}
		if (token.isScaduto()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codice scaduto");
		}
	}

	private static void controllaAttivo(Utente utente) {
		if (!utente.isActive()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Account non attivo: apri il link di conferma ricevuto via email");
		}
	}
}
