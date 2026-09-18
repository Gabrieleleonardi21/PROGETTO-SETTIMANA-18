// Gestione del token JWT in localStorage: unico punto che sa dove e' salvato
const CHIAVE = 'token'

export function leggiToken() {
  return localStorage.getItem(CHIAVE)
}

export function salvaToken(token) {
  localStorage.setItem(CHIAVE, token)
}

export function cancellaToken() {
  localStorage.removeItem(CHIAVE)
}
