import { useState } from 'react'
import { Link } from 'react-router-dom'
import Avviso from '../components/Avviso.jsx'
import Icona from '../components/Icona.jsx'
import { post } from '../services/api.js'

/** Form di registrazione: dopo l'invio l'account resta spento finche' non si apre il link nella mail. */
function Registrazione() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState('')
  const [inviato, setInviato] = useState(false)
  const [inCorso, setInCorso] = useState(false)

  async function invia(e) {
    e.preventDefault()
    setErrore('')
    setInCorso(true)
    try {
      await post('/api/auth/register', { username, email, password })
      setInviato(true)
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(false)
    }
  }

  if (inviato) {
    return (
      <main className="card">
        <div className="logo"><Icona nome="chat" dimensione={40} /><span>Chat</span></div>
        <h1>Controlla la tua email</h1>
        <p>Ti abbiamo inviato un link a <strong>{email}</strong>. Aprilo entro 24 ore per attivare l'account.</p>
        <p><Link to="/login">Vai al login</Link></p>
      </main>
    )
  }

  return (
    <main className="card">
      <div className="logo"><Icona nome="chat" dimensione={40} /><span>Chat</span></div>
      <h1>Registrati</h1>
      <form onSubmit={invia}>
        <label>Username<input autoComplete="username" value={username} onChange={(e) => setUsername(e.target.value)} minLength={3} maxLength={30} required /></label>
        <label>Email<input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
        <label>Password<input type="password" autoComplete="new-password" value={password} onChange={(e) => setPassword(e.target.value)} minLength={8} required /></label>
        <button type="submit" disabled={inCorso}>Crea account</button>
      </form>
      <Avviso testo={errore} />
      <p>Hai gia' un account? <Link to="/login">Accedi</Link></p>
    </main>
  )
}

export default Registrazione
