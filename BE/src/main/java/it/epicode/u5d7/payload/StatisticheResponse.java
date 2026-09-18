package it.epicode.u5d7.payload;

/** Statistiche personali dell'utente loggato. */
public record StatisticheResponse(long messaggiInviati, long messaggiRicevuti, long chatAperte) {
}
