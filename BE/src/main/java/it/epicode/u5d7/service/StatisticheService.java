package it.epicode.u5d7.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.StatisticheResponse;
import it.epicode.u5d7.repository.ChatRepository;
import it.epicode.u5d7.repository.MessaggioRepository;

/** Statistiche personali (messaggi inviati/ricevuti, chat aperte) e loro invio via email. */
@Service
public class StatisticheService {

	private final MessaggioRepository messaggioRepository;
	private final ChatRepository chatRepository;
	private final EmailService emailService;

	public StatisticheService(MessaggioRepository messaggioRepository, ChatRepository chatRepository,
			EmailService emailService) {
		this.messaggioRepository = messaggioRepository;
		this.chatRepository = chatRepository;
		this.emailService = emailService;
	}

	@Transactional(readOnly = true)
	public StatisticheResponse di(Utente me) {
		return new StatisticheResponse(messaggioRepository.countByMittente_Id(me.getId()),
				messaggioRepository.countByDestinatario_Id(me.getId()),
				chatRepository.countByPart1_IdOrPart2_Id(me.getId(), me.getId()));
	}

	/** Le statistiche vanno all'indirizzo con cui l'utente si e' registrato, mai a un indirizzo scelto dal client. */
	public void inviaViaEmail(Utente me) {
		emailService.inviaStatistiche(me.getEmail(), me.getUsername(), di(me));
	}
}
