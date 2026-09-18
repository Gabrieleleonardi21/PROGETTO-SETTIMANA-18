import { useState } from 'react'
import { Link } from 'react-router-dom'
import Avviso from '../components/Avviso.jsx'
import { api, post } from '../services/api.js'
import { salvaToken } from '../services/auth.js'

/**
 * Login in due modalita': con la password scelta alla registrazione oppure con un
 * codice a 6 cifre richiesto e ricevuto via email. Entrambe restituiscono un JWT.
 */
function Login({ onEntrato }) {
  const [modo, setModo] = useState('password')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [codice, setCodice] = useState('')
  const [codiceInviato, setCodiceInviato] = useState(false)
  const [errore, setErrore] = useState('')
  const [info, setInfo] = useState('')
  const [inCorso, setInCorso] = useState(false)

  // Esegue una chiamata gestendo errore/caricamento, per non ripetere try/catch tre volte
  async function esegui(azione) {
    setErrore('')
    setInfo('')
    setInCorso(true)
    try {
      await azione()
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(false)
    }
  }

  // Salvo subito il token cosi' la GET /me che segue parte gia' autenticata
  async function entra({ token }) {
    salvaToken(token)
    const utente = await api('/api/utenti/me')
    onEntrato(token, utente)
  }

  function loginPassword(e) {
    e.preventDefault()
    esegui(async () => entra(await post('/api/auth/login', { email, password })))
  }

  function richiediCodice(e) {
    e.preventDefault()
    esegui(async () => {
      const r = await post('/api/auth/request-code', { email })
      setInfo(r.messaggio)
      setCodiceInviato(true)
    })
  }

  function loginCodice(e) {
    e.preventDefault()
    esegui(async () => entra(await post('/api/auth/login-code', { email, codice: Number(codice) })))
  }

  // Classe del tab selezionato (evita un ternario dentro il JSX)
  function classeTab(nome) {
    if (modo === nome) return 'attivo'
    return ''
  }

  function cambiaModo(nuovo) {
    setModo(nuovo)
    setErrore('')
    setInfo('')
    setCodiceInviato(false)
  }

  let form = null
  if (modo === 'password') {
    form = (
      <form onSubmit={loginPassword}>
        <label>Email<input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
        <label>Password<input type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required /></label>
        <button type="submit" disabled={inCorso}>Accedi</button>
      </form>
    )
  }
  // Con il codice ci sono due passi: prima lo chiedi, poi lo inserisci
  if (modo === 'codice' && !codiceInviato) {
    form = (
      <form onSubmit={richiediCodice}>
        <label>Email<input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
        <button type="submit" disabled={inCorso}>Inviami il codice</button>
      </form>
    )
  }
  if (modo === 'codice' && codiceInviato) {
    form = (
      <form onSubmit={loginCodice}>
        <p>Codice inviato a <strong>{email}</strong> (valido 10 minuti).</p>
        <label>Codice a 6 cifre<input inputMode="numeric" pattern="[0-9]{6}" value={codice} onChange={(e) => setCodice(e.target.value)} required /></label>
        <button type="submit" disabled={inCorso}>Accedi</button>
        <button type="button" className="secondario" onClick={richiediCodice} disabled={inCorso}>Rinvia il codice</button>
      </form>
    )
  }

  return (
    <main className="card">
      <h1>Accedi</h1>
      <div className="tabs">
        <button type="button" className={classeTab('password')} onClick={() => cambiaModo('password')}>Con password</button>
        <button type="button" className={classeTab('codice')} onClick={() => cambiaModo('codice')}>Con codice via email</button>
      </div>
      {form}
      <Avviso testo={info} tipo="ok" />
      <Avviso testo={errore} />
      <p>Non hai un account? <Link to="/register">Registrati</Link></p>
    </main>
  )
}

export default Login
