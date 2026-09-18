package it.epicode.u5d7.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.EsitoResponse;
import it.epicode.u5d7.payload.StatisticheResponse;
import it.epicode.u5d7.service.StatisticheService;

/** Statistiche personali dell'utente loggato: lettura e invio via email (template Thymeleaf). */
@RestController
@RequestMapping("/api/statistiche")
public class StatisticheController {

	private final StatisticheService statisticheService;

	public StatisticheController(StatisticheService statisticheService) {
		this.statisticheService = statisticheService;
	}

	@GetMapping
	public StatisticheResponse mie(@AuthenticationPrincipal Utente me) {
		return statisticheService.di(me);
	}

	@PostMapping("/email")
	public EsitoResponse inviaEmail(@AuthenticationPrincipal Utente me) {
		statisticheService.inviaViaEmail(me);
		return new EsitoResponse("Statistiche inviate a " + me.getEmail());
	}
}
