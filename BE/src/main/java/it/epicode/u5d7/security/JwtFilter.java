package it.epicode.u5d7.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import io.jsonwebtoken.Claims;
import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.repository.UtenteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Legge l'header "Authorization: Bearer <jwt>", verifica il token e mette
 * l'utente nel SecurityContext. Se manca l'header lascia passare: sara'
 * SecurityConfig a rifiutare le rotte protette senza autenticazione.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

	private static final String PREFISSO = "Bearer ";

	private final JwtTool jwtTool;
	private final UtenteRepository utenteRepository;
	private final TokenBlacklistService blacklist;
	// Delega la gestione delle eccezioni all'ExceptionsHandler anche dentro il filtro
	private final HandlerExceptionResolver exceptionResolver;

	public JwtFilter(JwtTool jwtTool, UtenteRepository utenteRepository, TokenBlacklistService blacklist,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
		this.jwtTool = jwtTool;
		this.utenteRepository = utenteRepository;
		this.blacklist = blacklist;
		this.exceptionResolver = handlerExceptionResolver;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith(PREFISSO)) {
			chain.doFilter(request, response);
			return;
		}
		String token = header.substring(PREFISSO.length());
		try {
			if (blacklist.contiene(token)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido: logout effettuato");
			}
			Claims claims = jwtTool.verifica(token);
			UUID idUtente = jwtTool.estraiIdUtente(claims);
			Utente utente = utenteRepository.findById(idUtente)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));

			// Principal = entita' Utente; credentials = il token grezzo (serve al logout per la blacklist)
			var auth = new UsernamePasswordAuthenticationToken(utente, token, List.of());
			SecurityContextHolder.getContext().setAuthentication(auth);
			chain.doFilter(request, response);
		} catch (ResponseStatusException e) {
			exceptionResolver.resolveException(request, response, null, e);
		}
	}
}
