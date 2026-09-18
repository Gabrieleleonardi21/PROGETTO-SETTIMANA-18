import { useEffect, useState } from 'react'
import { api, post } from '../services/api.js'
import Avviso from './Avviso.jsx'

/**
 * Riquadro con le statistiche personali (letto da GET /api/statistiche) e il bottone
 * che chiede al backend di spedirle via email con il template Thymeleaf.
 */
function Statistiche({ onChiudi }) {
  const [stat, setStat] = useState(null)
  const [info, setInfo] = useState('')
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)

  useEffect(() => {
    api('/api/statistiche').then(setStat).catch((e) => setErrore(e.message))
  }, [])

  async function inviaEmail() {
    setInfo('')
    setErrore('')
    setInCorso(true)
    try {
      const r = await post('/api/statistiche/email')
      setInfo(r.messaggio)
    } catch (e) {
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  let tabella = <p className="nota">Caricamento...</p>
  if (stat) {
    tabella = (
      <dl className="statistiche">
        <dt>Messaggi inviati</dt><dd>{stat.messaggiInviati}</dd>
        <dt>Messaggi ricevuti</dt><dd>{stat.messaggiRicevuti}</dd>
        <dt>Chat aperte</dt><dd>{stat.chatAperte}</dd>
      </dl>
    )
  }

  return (
    <section className="card">
      <h2>Le tue statistiche</h2>
      {tabella}
      <div className="azioni">
        <button type="button" onClick={inviaEmail} disabled={inCorso || !stat}>Inviamele via email</button>
        <button type="button" className="secondario" onClick={onChiudi}>Chiudi</button>
      </div>
      <Avviso testo={info} tipo="ok" />
      <Avviso testo={errore} />
    </section>
  )
}

export default Statistiche
