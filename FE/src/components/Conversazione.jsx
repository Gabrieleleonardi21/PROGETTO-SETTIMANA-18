import { useEffect, useRef, useState } from 'react'
import { post } from '../services/api.js'
import { pubblica } from '../services/stompClient.js'
import Avatar from './Avatar.jsx'
import Avviso from './Avviso.jsx'
import Icona from './Icona.jsx'

/**
 * La conversazione aperta: lista dei messaggi e form di invio.
 * L'invio passa dal canale WebSocket (SEND su /app/messaggi). Nessun inserimento
 * "ottimistico": il messaggio compare quando torna l'eco del server, con id e
 * sentAt decisi dal backend. Il mittente non viene inviato: lo mette il server dal principal.
 * Il bottone "Suggerisci" chiede all'IA un testo e lo mette nel campo: l'utente puo'
 * modificarlo o scartarlo, e nulla viene salvato finche' non preme Invia.
 */
function Conversazione({ chat, messaggi, meId, onIndietro }) {
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
    if (m.status === 'CONSEGNATO') return <span className="stato consegnato">✓✓</span>
    return <span className="stato">✓</span>
  }

  function ora(iso) {
    return new Date(iso).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
  }

  let classeSuggerisci = 'icona'
  if (inAttesaIa) classeSuggerisci = 'icona pulsante'

  let lista = <p className="nota">Nessun messaggio: scrivi tu il primo, o fatti aiutare dall'IA ✨</p>
  if (messaggi.length > 0) {
    lista = messaggi.map((m) => (
      <li key={m.id} className={classeMessaggio(m)}>
        <span className="testo">{m.testo}</span>
        <small>{ora(m.sentAt)} {stato(m)}</small>
      </li>
    ))
  }

  return (
    <>
      <header className="intestazione">
        <button type="button" className="icona solo-mobile" onClick={onIndietro} aria-label="Indietro"><Icona nome="indietro" /></button>
        <Avatar nome={chat.altro.username} />
        <strong>{chat.altro.username}</strong>
      </header>
      <ul className="messaggi">
        {lista}
        <li ref={fondo} />
      </ul>
      <Avviso testo={erroreIa} />
      <form className="invio" onSubmit={invia}>
        <button type="button" className={classeSuggerisci} onClick={suggerisci} disabled={inAttesaIa} title="Suggerisci con l'IA" aria-label="Suggerisci con l'IA">
          <Icona nome="scintilla" />
        </button>
        <input value={testo} onChange={(e) => setTesto(e.target.value)} placeholder="Scrivi un messaggio" autoFocus />
        <button type="submit" className="tondo" aria-label="Invia" disabled={!testo.trim()}><Icona nome="invia" dimensione={20} /></button>
      </form>
    </>
  )
}

export default Conversazione
