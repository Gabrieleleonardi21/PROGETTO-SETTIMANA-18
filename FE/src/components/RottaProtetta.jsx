import { Navigate, Outlet } from 'react-router-dom'

/**
 * Avvolge le rotte che richiedono il login: se non c'e' un utente in sessione
 * rimanda a /login. replace: la pagina protetta non resta nella cronologia,
 * cosi' il tasto "indietro" dopo il login non riporta al redirect.
 */
function RottaProtetta({ utente }) {
  if (!utente) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}

export default RottaProtetta
