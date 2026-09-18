import { useEffect, useState } from 'react'
import Avviso from '../components/Avviso.jsx'
import Conversazione from '../components/Conversazione.jsx'
import Statistiche from '../components/Statistiche.jsx'
import { api, patch, post } from '../services/api.js'
import { ascolta } from '../services/stompClient.js'

/**
 * Unisce cronologia REST e messaggi WebSocket. La Map elimina i duplicati per id
 * (il mio messaggio arriva sia dall'eco WS sia da una GET successiva); il sort
 * riproduce l'ordine deciso dal server (sentAt assegnato dal backend).
 * Confronto con new Date e non fra stringhe: la frazione di secondo di un Instant
 * ha lunghezza variabile e il confronto lessicografico sbaglierebbe.
 */
function unisci(esistenti, nuovi) {
  const perId = new Map(esistenti.map((m) => [m.id, m]))
  nuovi.forEach((m) => perId.set(m.id, m))
  return [...perId.values()].sort((a, b) => new Date(a.sentAt) - new Date(b.sentAt))
}

/** Pagina principale: a sinistra le persone con cui parlare, a destra la conversazione aperta. */
function Chat({ utente, onEsci }) {
  const [utenti, setUtenti] = useState([])
  const [chatAperta, setChatAperta] = useState(null)
  const [messaggi, setMessaggi] = useState([])
  const [errore, setErrore] = useState('')
  // true = nella colonna destra si mostrano le statistiche al posto della conversazione
  const [mostraStatistiche, setMostraStatistiche] = useState(false)

  // Le persone con cui posso parlare: tutti gli altri utenti registrati
  useEffect(() => {
    api('/api/utenti').then(setUtenti).catch((e) => setErrore(e.message))
  }, [])

  // Apertura di una chat: cronologia (page 0 = piu' recenti) e PATCH che segna
  // consegnati i messaggi che ho ricevuto mentre ero offline
  useEffect(() => {
    if (!chatAperta) return
    api(`/api/chat/${chatAperta.id}/messaggi?page=0&size=50`)
      .then((pagina) => setMessaggi((prev) => unisci(prev, pagina.content)))
      .catch((e) => setErrore(e.message))
    patch(`/api/chat/${chatAperta.id}/messaggi/consegnati`).catch((e) => setErrore(e.message))
  }, [chatAperta])

  // Coda personale: arriva solo cio' che riguarda me, sia i messaggi nuovi sia quelli
  // gia' visti che cambiano stato (il destinatario li ha ricevuti: stesso id, status CONSEGNATO).
  // Tengo solo quelli della chat aperta; gli altri restano a DB e li leggero' aprendo quella chat.
  // La cleanup restituita da ascolta() evita gestori duplicati al cambio chat.
  useEffect(() => {
    return ascolta('/user/queue/messaggi', (m) => {
      if (chatAperta && m.chatId === chatAperta.id) {
        setMessaggi((prev) => unisci(prev, [m]))
      }
    })
  }, [chatAperta])

  // Click su una persona: il backend crea la chat o restituisce quella esistente
  async function apri(altro) {
    setErrore('')
    try {
      const chat = await post('/api/chat', { destinatarioId: altro.id })
      // Svuoto prima di cambiare chat: la lista non deve mostrare per un istante i messaggi di quella precedente
      setMessaggi([])
      setChatAperta(chat)
      setMostraStatistiche(false)
    } catch (e) {
      setErrore(e.message)
    }
  }

  function classeContatto(id) {
    if (chatAperta && chatAperta.altro.id === id) return 'contatto attivo'
    return 'contatto'
  }

  let colonnaDestra = <p className="nota">Scegli una persona per iniziare a chattare</p>
  if (chatAperta) {
    colonnaDestra = <Conversazione chat={chatAperta} messaggi={messaggi} meId={utente.id} />
  }
  if (mostraStatistiche) {
    colonnaDestra = <Statistiche onChiudi={() => setMostraStatistiche(false)} />
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <header>
          <strong>{utente.username}</strong>
          <div className="azioni">
            <button type="button" className="secondario" onClick={() => setMostraStatistiche(true)}>Statistiche</button>
            <button type="button" className="secondario" onClick={onEsci}>Esci</button>
          </div>
        </header>
        <ul>
          {utenti.map((u) => (
            <li key={u.id}>
              <button type="button" className={classeContatto(u.id)} onClick={() => apri(u)}>{u.username}</button>
            </li>
          ))}
        </ul>
        <Avviso testo={errore} />
      </aside>
      <main className="colonna">{colonnaDestra}</main>
    </div>
  )
}

export default Chat
