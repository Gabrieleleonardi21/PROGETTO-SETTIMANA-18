package it.epicode.u5d7.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.u5d7.model.Utente;
import it.epicode.u5d7.payload.ChatResponse;
import it.epicode.u5d7.payload.NuovaChatRequest;
import it.epicode.u5d7.service.ChatService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	/** Crea la chat con il destinatario o restituisce quella esistente (per questo 200 e non 201). */
	@PostMapping
	public ChatResponse nuova(@AuthenticationPrincipal Utente me, @RequestBody @Valid NuovaChatRequest body) {
		return chatService.creaOTrova(me, body.destinatarioId());
	}

	/** Tutte le chat a cui partecipa l'utente loggato. */
	@GetMapping
	public List<ChatResponse> mie(@AuthenticationPrincipal Utente me) {
		return chatService.chatDi(me);
	}
}
