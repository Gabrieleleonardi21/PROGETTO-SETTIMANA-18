package it.epicode.u5d7.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.UtenteResponse;
import it.epicode.u5d7.service.UtenteService;

@RestController
@RequestMapping("/api/utenti")
public class UtenteController {

	private final UtenteService utenteService;

	public UtenteController(UtenteService utenteService) {
		this.utenteService = utenteService;
	}

	@GetMapping("/me")
	public UtenteResponse me(@AuthenticationPrincipal Utente me) {
		return UtenteResponse.da(me);
	}

	/** Le persone con cui l'utente loggato puo' parlare: tutti gli altri registrati. */
	@GetMapping
	public List<UtenteResponse> altri(@AuthenticationPrincipal Utente me) {
		return utenteService.altriUtenti(me.getId());
	}
}
