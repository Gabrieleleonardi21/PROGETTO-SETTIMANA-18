import { leggiToken } from './auth.js'
import { API_URL } from './config.js'

/**
 * Wrapper unico su fetch: aggiunge base URL, Content-Type JSON e, se presente,
 * l'header Authorization con il JWT. Gli errori del backend (ErrorPayload)
 * diventano un throw con il messaggio in italiano, cosi' le pagine fanno solo try/catch.
 */
export async function api(percorso, opzioni = {}) {
  const headers = { 'Content-Type': 'application/json', ...opzioni.headers }
  const token = leggiToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  const risposta = await fetch(`${API_URL}${percorso}`, { ...opzioni, headers })
  if (!risposta.ok) {
    throw new Error(await leggiMessaggioErrore(risposta))
  }
  // 204 (logout, PATCH) non ha corpo
  if (risposta.status === 204) {
    return null
  }
  return risposta.json()
}

// Scorciatoie per non ripetere method + JSON.stringify in ogni pagina
export function post(percorso, body) {
  return api(percorso, { method: 'POST', body: JSON.stringify(body) })
}

export function patch(percorso) {
  return api(percorso, { method: 'PATCH' })
}

async function leggiMessaggioErrore(risposta) {
  try {
    const corpo = await risposta.json()
    if (corpo.messaggio) return corpo.messaggio
  } catch {
    /* corpo non JSON */
  }
  return `Errore ${risposta.status}`
}
