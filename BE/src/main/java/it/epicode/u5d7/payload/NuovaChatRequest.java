package it.epicode.u5d7.payload;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** Chi crea la chat e' il principal: nel body viaggia solo l'altro partecipante. */
public record NuovaChatRequest(@NotNull(message = "Il destinatario e' obbligatorio") UUID destinatarioId) {
}
