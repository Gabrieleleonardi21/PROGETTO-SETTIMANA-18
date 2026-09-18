import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import Avviso from '../components/Avviso.jsx'
import { post } from '../services/api.js'

/** Pagina aperta dal link della mail: legge email e code dalla query string e attiva l'account. */
function Verifica() {
  const [params] = useSearchParams()
  const email = params.get('email')
  const codice = params.get('code')
  const linkIncompleto = !email || !codice
  const [esito, setEsito] = useState('')
  const [errore, setErrore] = useState('')

  // Chiama il backend una sola volta al caricamento della pagina
  useEffect(() => {
    if (linkIncompleto) return
    post('/api/auth/verify', { email, codice })
      .then((r) => setEsito(r.messaggio))
      .catch((err) => setErrore(err.message))
  }, [email, codice, linkIncompleto])

  let erroreLink = ''
  if (linkIncompleto) erroreLink = 'Link di verifica incompleto'

  return (
    <main className="card">
      <h1>Verifica account</h1>
      <Avviso testo={esito} tipo="ok" />
      <Avviso testo={erroreLink} />
      <Avviso testo={errore} />
      <p><Link to="/login">Vai al login</Link></p>
    </main>
  )
}

export default Verifica
