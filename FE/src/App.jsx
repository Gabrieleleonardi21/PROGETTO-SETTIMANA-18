import { useEffect, useState } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import RottaProtetta from './components/RottaProtetta.jsx'
import Chat from './pages/Chat.jsx'
import Login from './pages/Login.jsx'
import Registrazione from './pages/Registrazione.jsx'
import { api, post } from './services/api.js'
import { cancellaToken, leggiToken, salvaToken } from './services/auth.js'
import { attiva, disattiva } from './services/stompClient.js'
import './App.css'

/**
 * Tiene la sessione (l'utente loggato) e decide quale pagina mostrare.
 * Il token vive in localStorage ma non ci fidiamo alla cieca: al primo caricamento
 * chiediamo GET /me. Se il backend risponde 401 lo buttiamo via e si torna al login.
 * Il canale WebSocket si accende qui, una volta sola, appena la sessione e' valida.
 */
function App() {
  const [utente, setUtente] = useState(null)
  // true solo se c'e' un token da verificare: evita di mostrare il login per un istante a chi e' gia' dentro
  const [inVerifica, setInVerifica] = useState(Boolean(leggiToken()))

  useEffect(() => {
    const token = leggiToken()
    if (!token) return

    async function verifica() {
      try {
        setUtente(await api('/api/utenti/me'))
        attiva(token)
      } catch {
        cancellaToken()
      } finally {
        setInVerifica(false)
      }
    }
    verifica()
  }, [])

  function entra(token, datiUtente) {
    salvaToken(token)
    setUtente(datiUtente)
    attiva(token)
  }

  async function esci() {
    // Il logout mette il token in blacklist lato server: anche se qualcuno lo copiasse non varrebbe piu'
    try {
      await post('/api/auth/logout')
    } catch {
      /* token gia' scaduto: usciamo comunque */
    }
    disattiva()
    cancellaToken()
    setUtente(null)
  }

  if (inVerifica) {
    return <p className="nota">Caricamento...</p>
  }

  // Chi e' gia' dentro non deve rivedere login/registrazione
  let paginaLogin = <Login onEntrato={entra} />
  let paginaRegistrazione = <Registrazione />
  if (utente) {
    paginaLogin = <Navigate to="/" replace />
    paginaRegistrazione = <Navigate to="/" replace />
  }

  return (
    <Routes>
      <Route path="/login" element={paginaLogin} />
      <Route path="/register" element={paginaRegistrazione} />
      <Route element={<RottaProtetta utente={utente} />}>
        <Route path="/" element={<Chat utente={utente} onEsci={esci} />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
