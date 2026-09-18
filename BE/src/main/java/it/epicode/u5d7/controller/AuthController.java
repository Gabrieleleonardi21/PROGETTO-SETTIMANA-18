package it.epicode.u5d7.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.u5d7.payload.EmailRequest;
import it.epicode.u5d7.payload.EsitoResponse;
import it.epicode.u5d7.payload.JwtResponse;
import it.epicode.u5d7.payload.LoginCodeRequest;
import it.epicode.u5d7.payload.LoginRequest;
import it.epicode.u5d7.payload.RegisterRequest;
import it.epicode.u5d7.payload.UtenteResponse;
import it.epicode.u5d7.payload.VerifyRequest;
import it.epicode.u5d7.service.UtenteService;
import jakarta.validation.Valid;

/**
 * Registrazione, verifica email, login (password o codice via email) e logout.
 * Le rotte pubbliche sono elencate in SecurityConfig; /logout richiede il Bearer token.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UtenteService utenteService;

	public AuthController(UtenteService utenteService) {
		this.utenteService = utenteService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UtenteResponse register(@RequestBody @Valid RegisterRequest body) {
		return utenteService.registra(body);
	}

	// Chiamato dalla pagina /verify del frontend con email e codice presi dal link della mail
	@PostMapping("/verify")
	public EsitoResponse verify(@RequestBody @Valid VerifyRequest body) {
		utenteService.verifica(body.email(), body.codice());
		return new EsitoResponse("Account attivato, ora puoi accedere");
	}

	@PostMapping("/login")
	public JwtResponse login(@RequestBody @Valid LoginRequest body) {
		return new JwtResponse(utenteService.login(body.email(), body.password()));
	}

	// Primo passo del login con codice: invia il codice a 6 cifre via email
	@PostMapping("/request-code")
	public EsitoResponse requestCode(@RequestBody @Valid EmailRequest body) {
		utenteService.richiediCodiceLogin(body.email());
		return new EsitoResponse("Se l'email e' registrata e attiva riceverai un codice");
	}

	@PostMapping("/login-code")
	public JwtResponse loginCode(@RequestBody @Valid LoginCodeRequest body) {
		return new JwtResponse(utenteService.loginCodice(body.email(), body.codice()));
	}

	// Le credentials dell'Authentication contengono il token grezzo (vedi JwtFilter)
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(Authentication auth) {
		utenteService.logout((String) auth.getCredentials());
	}
}
