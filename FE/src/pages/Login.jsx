import { useState } from 'react'
import { Link } from 'react-router-dom'
import Avviso from '../components/Avviso.jsx'
import { api, post } from '../services/api.js'
import { salvaToken } from '../services/auth.js'

/** Login con email e password: il backend restituisce un JWT. */
function Login({ onEntrato }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)

  async function invia(e) {
    e.preventDefault()
    setErrore('')
    setInCorso(true)
    try {
      const { token } = await post('/api/auth/login', { email, password })
      // Salvo subito il token cosi' la GET /me che segue parte gia' autenticata
      salvaToken(token)
      const utente = await api('/api/utenti/me')
      onEntrato(token, utente)
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <main className="card">
      <h1>Accedi</h1>
      <form onSubmit={invia}>
        <label>Email<input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
        <label>Password<input type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required /></label>
        <button type="submit" disabled={inCorso}>Accedi</button>
      </form>
      <Avviso testo={errore} />
      <p>Non hai un account? <Link to="/register">Registrati</Link></p>
    </main>
  )
}

export default Login
