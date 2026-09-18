// Indirizzi del backend, relativi all'indirizzo da cui e' servito il front-end:
// il dev server Vite inoltra /api e /ws al backend (vedi vite.config.js), quindi
// funziona da localhost, da un telefono in rete locale o da un tunnel https.
export const API_URL = ''

// ws:// su http, wss:// su https (un tunnel espone sempre https)
let protocollo = 'ws'
if (window.location.protocol === 'https:') {
  protocollo = 'wss'
}
export const WS_URL = `${protocollo}://${window.location.host}/ws`
