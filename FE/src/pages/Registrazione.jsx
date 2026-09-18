import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Avviso from '../components/Avviso.jsx'
import { post } from '../services/api.js'

function Registrazione() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)
  const naviga = useNavigate()

  async function invia(e) {
    e.preventDefault()
    setErrore('')
    setInCorso(true)
    try {
      await post('/api/auth/register', { username, email, password })
      // Registrazione riuscita: si passa dal login per ottenere il token
      naviga('/login')
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <main className="card">
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
