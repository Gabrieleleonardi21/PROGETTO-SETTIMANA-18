package it.epicode.u5d7.exceptions;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import it.epicode.u5d7.payload.ErrorPayload;

/**
 * Traduce le eccezioni in risposte JSON uniformi (ErrorPayload).
 * I service lanciano ResponseStatusException con status e messaggio in italiano.
 */
@RestControllerAdvice
public class ExceptionsHandler {

	private static final Logger log = LoggerFactory.getLogger(ExceptionsHandler.class);

	private ResponseEntity<ErrorPayload> errore(HttpStatus status, String messaggio) {
		return ResponseEntity.status(status).body(new ErrorPayload(status.value(), messaggio));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorPayload> gestisciResponseStatus(ResponseStatusException e) {
		String messaggio = e.getReason();
		if (messaggio == null) {
			messaggio = "Richiesta non valida";
		}
		return ResponseEntity.status(e.getStatusCode())
				.body(new ErrorPayload(e.getStatusCode().value(), messaggio));
	}

	// Errori di Bean Validation sui DTO (@Valid): concatena i messaggi dei campi
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorPayload> gestisciValidazione(MethodArgumentNotValidException e) {
		String messaggio = e.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return errore(HttpStatus.BAD_REQUEST, messaggio);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorPayload> gestisciAutenticazione(AuthenticationException e) {
		return errore(HttpStatus.UNAUTHORIZED, "Autenticazione richiesta");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorPayload> gestisciAccessoNegato(AccessDeniedException e) {
		return errore(HttpStatus.FORBIDDEN, "Accesso negato");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorPayload> gestisciGenerica(Exception e) {
		// Eccezioni di Spring MVC che portano gia' uno status (404, 405, JSON malformato...)
		if (e instanceof ErrorResponse err) {
			return ResponseEntity.status(err.getStatusCode())
					.body(new ErrorPayload(err.getStatusCode().value(), e.getMessage()));
		}
		log.error("Errore non gestito", e);
		return errore(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno del server");
	}
}
