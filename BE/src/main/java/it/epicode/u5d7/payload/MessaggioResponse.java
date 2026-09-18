package it.epicode.u5d7.payload;

import java.time.Instant;
import java.util.UUID;

import it.epicode.u5d7.model.Messaggio;
import it.epicode.u5d7.model.StatoMessaggio;

/** Stesso DTO per la cronologia REST e per il frame WebSocket: il client li unisce per id. */
public record MessaggioResponse(UUID id, UUID chatId, UUID mittenteId, UUID destinatarioId, String testo,
		StatoMessaggio status, Instant sentAt, Instant deliveredAt) {

	// Sulle relazioni LAZY legge solo getId(): non scatena query aggiuntive
	public static MessaggioResponse da(Messaggio m) {
		return new MessaggioResponse(m.getId(), m.getChat().getId(), m.getMittente().getId(),
				m.getDestinatario().getId(), m.getTesto(), m.getStatus(), m.getSentAt(), m.getDeliveredAt());
	}
}
