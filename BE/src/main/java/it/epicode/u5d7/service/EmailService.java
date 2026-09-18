package it.epicode.u5d7.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;

import it.epicode.u5d7.payload.StatisticheResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Invio delle email tramite JavaMailSender (SMTP Gmail, vedi application.properties).
 * Verifica e codice di login sono testo semplice; le statistiche sono HTML
 * generato dal template Thymeleaf templates/email/statistiche.html.
 */
@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	private final String mittente;
	private final String frontendUrl;

	public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
			@Value("${spring.mail.username}") String mittente, @Value("${app.frontend.url}") String frontendUrl) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
		this.mittente = mittente;
		this.frontendUrl = frontendUrl;
	}

	/** Link che apre la pagina /verify del frontend con email e codice nella query string. */
	public void inviaLinkVerifica(String email, String username, String codice) {
		String link = frontendUrl + "/verify?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
				+ "&code=" + codice;
		inviaTesto(email, "Conferma la tua registrazione",
				"Ciao " + username + ",\n\nper attivare il tuo account apri questo link (valido 24 ore):\n" + link
						+ "\n\nSe non ti sei registrato tu, ignora questa email.");
	}

	public void inviaCodiceLogin(String email, int codice) {
		inviaTesto(email, "Il tuo codice di accesso",
				"Il codice per accedere alla chat e': " + codice + "\n\nScade fra 10 minuti.");
	}

	/**
	 * Email HTML con le statistiche. Le variabili passate al Context finiscono nel
	 * template tramite th:text, che fa l'escape HTML: lo username non puo' iniettare markup.
	 */
	public void inviaStatistiche(String email, String username, StatisticheResponse stat) {
		Context ctx = new Context();
		ctx.setVariable("username", username);
		ctx.setVariable("stat", stat);
		String html = templateEngine.process("email/statistiche", ctx);
		inviaHtml(email, "Le tue statistiche della chat", html);
	}

	private void inviaTesto(String a, String oggetto, String testo) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(mittente);
		msg.setTo(a);
		msg.setSubject(oggetto);
		msg.setText(testo);
		try {
			mailSender.send(msg);
		} catch (MailException e) {
			gestisciErrore(a, e);
		}
	}

	private void inviaHtml(String a, String oggetto, String html) {
		try {
			MimeMessage msg = mailSender.createMimeMessage();
			// true nel setText = il corpo e' HTML
			MimeMessageHelper helper = new MimeMessageHelper(msg, StandardCharsets.UTF_8.name());
			helper.setFrom(mittente);
			helper.setTo(a);
			helper.setSubject(oggetto);
			helper.setText(html, true);
			mailSender.send(msg);
		} catch (MailException | MessagingException e) {
			gestisciErrore(a, e);
		}
	}

	// Il dettaglio (credenziali, rete...) resta nel log del server, al client va un messaggio generico
	private void gestisciErrore(String a, Exception e) {
		log.error("Invio email fallito verso {}", a, e);
		throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
				"Impossibile inviare l'email, riprova piu' tardi");
	}
}
