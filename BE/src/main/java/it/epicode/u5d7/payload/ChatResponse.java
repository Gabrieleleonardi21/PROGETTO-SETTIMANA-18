package it.epicode.u5d7.payload;

import java.util.UUID;

import it.epicode.u5d7.model.Chat;

/** La chat vista da chi chiede: espone solo "l'altro" partecipante. */
public record ChatResponse(UUID id, UtenteResponse altro) {

	public static ChatResponse da(Chat chat, UUID meId) {
		if (chat.getPart1().getId().equals(meId)) {
			return new ChatResponse(chat.getId(), UtenteResponse.da(chat.getPart2()));
		}
		return new ChatResponse(chat.getId(), UtenteResponse.da(chat.getPart1()));
	}
}
