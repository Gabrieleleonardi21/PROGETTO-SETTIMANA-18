import { useEffect, useState } from 'react'
import { api, post } from '../services/api.js'
import Avviso from './Avviso.jsx'
import Icona from './Icona.jsx'

/**
 * Consegna, punto 3: pannello con le statistiche personali (GET /api/statistiche) e il bottone
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

  // Tre riquadri con il numero grande; finche' non arrivano i dati mostro un trattino
  const voci = [
    ['Messaggi inviati', 'messaggiInviati'],
    ['Messaggi ricevuti', 'messaggiRicevuti'],
    ['Chat aperte', 'chatAperte'],
  ]

  return (
    <>
      <header className="intestazione">
        <button type="button" className="icona solo-mobile" onClick={onChiudi} aria-label="Indietro"><Icona nome="indietro" /></button>
        <strong>Le tue statistiche</strong>
        <button type="button" className="icona" onClick={onChiudi} aria-label="Chiudi"><Icona nome="chiudi" /></button>
      </header>
      <section className="pannello">
        <div className="statistiche">
          {voci.map(([etichetta, chiave]) => (
            <div key={chiave} className="stat">
              <strong>{stat ? stat[chiave] : '–'}</strong>
              <span>{etichetta}</span>
            </div>
          ))}
        </div>
        <p className="nota">Ricevi il riepilogo via email, all'indirizzo con cui ti sei registrato.</p>
        <button type="button" onClick={inviaEmail} disabled={inCorso || !stat}>Inviamele via email</button>
        <Avviso testo={info} tipo="ok" />
        <Avviso testo={errore} />
      </section>
    </>
  )
}

export default Statistiche
