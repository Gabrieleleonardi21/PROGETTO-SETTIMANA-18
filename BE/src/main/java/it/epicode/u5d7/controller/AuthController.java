package it.epicode.u5d7.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.u5d7.payload.JwtResponse;
import it.epicode.u5d7.payload.LoginRequest;
import it.epicode.u5d7.payload.RegisterRequest;
import it.epicode.u5d7.payload.UtenteResponse;
import it.epicode.u5d7.service.UtenteService;
import jakarta.validation.Valid;

/** Registrazione e login sono pubblici (SecurityConfig); logout richiede il Bearer token. */
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

	@PostMapping("/login")
	public JwtResponse login(@RequestBody @Valid LoginRequest body) {
		return new JwtResponse(utenteService.login(body.email(), body.password()));
	}

	// Le credentials dell'Authentication contengono il token grezzo (vedi JwtFilter)
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(Authentication auth) {
		utenteService.logout((String) auth.getCredentials());
	}
}
