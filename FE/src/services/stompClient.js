import { Client } from '@stomp/stompjs'
import { WS_URL } from './config.js'

/**
 * IL CANALE, LATO BROWSER.
 *
 * UN SOLO Client per tutta l'applicazione: un Client e' una connessione.
 * Crearne uno per componente aprirebbe tante connessioni quanti sono i
 * componenti montati e i messaggi arriverebbero in doppio.
 */
const client = new Client({
  // ws:// e non http://: il backend registra l'endpoint /ws SENZA SockJS
  brokerURL: WS_URL,
  // Se la connessione cade (backend riavviato) il client riprova da solo ogni 5 s.
  // E' anche il motivo per cui le subscribe vanno rifatte in onConnect.
  reconnectDelay: 5000,
})

// Gestori registrati dai componenti, per destinazione: sopravvivono alle riconnessioni
const gestori = new Map()
// Subscribe realmente aperte verso il broker
const aperte = new Map()

/** Apre la subscribe verso il broker, se non c'e' gia' e se siamo connessi. */
function apri(destinazione) {
  if (aperte.has(destinazione) || !client.connected) return
  const sub = client.subscribe(destinazione, (frame) => {
    // frame.body e' sempre una stringa: il JSON.parse tocca a noi
    const corpo = JSON.parse(frame.body)
    gestori.get(destinazione)?.forEach((gestore) => gestore(corpo))
  })
  aperte.set(destinazione, sub)
}

// Le subscribe vanno qui, non subito dopo activate(): activate non e' istantaneo.
// Dopo una riconnessione il broker non ricorda nulla: riapriamo tutto.
client.onConnect = () => {
  aperte.clear()
  gestori.forEach((_, destinazione) => apri(destinazione))
}

// Gli errori del broker (es. token rifiutato sul CONNECT) altrimenti restano invisibili
client.onStompError = (frame) => {
  console.error('Errore STOMP:', frame.headers['message'])
}

/**
 * Accende il canale dopo il login. Il token va negli header del frame CONNECT:
 * e' li' che il backend (StompAuthInterceptor) lo legge per dare un'identita' alla sessione.
 */
export function attiva(token) {
  if (client.active) return
  client.connectHeaders = { Authorization: `Bearer ${token}` }
  client.activate()
}

/** Al logout: chiude la connessione e dimentica le sottoscrizioni. */
export function disattiva() {
  aperte.forEach((sub) => sub.unsubscribe())
  aperte.clear()
  gestori.clear()
  client.deactivate()
}

/** Invia un frame SEND: il backend lo riceve nel @MessageMapping corrispondente. */
export function pubblica(destinazione, corpo) {
  client.publish({ destination: destinazione, body: JSON.stringify(corpo) })
}

/**
 * L'unico modo in cui i componenti ascoltano il canale.
 * Restituisce la funzione di pulizia da usare nel return di useEffect.
 */
export function ascolta(destinazione, gestore) {
  const perDestinazione = gestori.get(destinazione) ?? new Set()
  perDestinazione.add(gestore)
  gestori.set(destinazione, perDestinazione)

  // Se siamo gia' connessi apriamo subito; altrimenti ci pensa onConnect
  apri(destinazione)

  return () => {
    perDestinazione.delete(gestore)
    // La subscribe verso il broker si chiude solo quando NESSUNO ascolta piu' quella destinazione
    if (perDestinazione.size === 0) {
      aperte.get(destinazione)?.unsubscribe()
      aperte.delete(destinazione)
      gestori.delete(destinazione)
    }
  }
}
