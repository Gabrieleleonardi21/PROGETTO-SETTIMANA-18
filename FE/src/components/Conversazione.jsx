import { useEffect, useRef, useState } from 'react'
import { post } from '../services/api.js'
import { pubblica } from '../services/stompClient.js'
import Avviso from './Avviso.jsx'

/**
 * La conversazione aperta: lista dei messaggi e form di invio.
 * L'invio passa dal canale WebSocket (SEND su /app/messaggi). Nessun inserimento
 * "ottimistico": il messaggio compare quando torna l'eco del server, con id e
 * sentAt decisi dal backend. Il mittente non viene inviato: lo mette il server dal principal.
 * Il bottone "Suggerisci" chiede all'IA un testo e lo mette nel campo: l'utente puo'
 * modificarlo o scartarlo, e nulla viene salvato finche' non preme Invia.
 */
function Conversazione({ chat, messaggi, meId }) {
  const [testo, setTesto] = useState('')
  const [erroreIa, setErroreIa] = useState('')
  const [inAttesaIa, setInAttesaIa] = useState(false)
  const fondo = useRef(null)

  // Ogni nuovo messaggio porta la lista in fondo
  useEffect(() => {
    fondo.current?.scrollIntoView()
  }, [messaggi])

  function invia(e) {
    e.preventDefault()
    if (!testo.trim()) return
    pubblica('/app/messaggi', { destinatarioId: chat.altro.id, testo })
    setTesto('')
  }

  async function suggerisci() {
    setErroreIa('')
    setInAttesaIa(true)
    try {
      const r = await post(`/api/chat/${chat.id}/suggerimento`)
      setTesto(r.testo)
    } catch (e) {
      setErroreIa(e.message)
    } finally {
      setInAttesaIa(false)
    }
  }

  function classeMessaggio(m) {
    if (m.mittenteId === meId) return 'bolla mia'
    return 'bolla'
  }

  // Lo stato lo mostro solo sui miei: sui messaggi ricevuti non ha senso
  function stato(m) {
    if (m.mittenteId !== meId) return null
    if (m.status === 'CONSEGNATO') return <span className="stato">✓✓</span>
    return <span className="stato">✓</span>
  }

  function ora(iso) {
    return new Date(iso).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
  }

  return (
    <>
      <header className="intestazione">{chat.altro.username}</header>
      <ul className="messaggi">
        {messaggi.map((m) => (
          <li key={m.id} className={classeMessaggio(m)}>
            <span>{m.testo}</span>
            <small>{ora(m.sentAt)} {stato(m)}</small>
          </li>
        ))}
        <li ref={fondo} />
      </ul>
      <Avviso testo={erroreIa} />
      <form className="invio" onSubmit={invia}>
        <input value={testo} onChange={(e) => setTesto(e.target.value)} placeholder="Scrivi un messaggio" autoFocus />
        <button type="button" className="secondario" onClick={suggerisci} disabled={inAttesaIa}>Suggerisci</button>
        <button type="submit">Invia</button>
      </form>
    </>
  )
}

export default Conversazione
