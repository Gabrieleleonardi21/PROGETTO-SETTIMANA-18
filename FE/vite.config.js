import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Ridireziona l'header Origin verso quello che il backend conosce (localhost:5173):
// cosi' CORS e handshake WebSocket passano anche quando il front-end e' raggiunto
// da un altro indirizzo (IP in rete locale o tunnel https verso l'esterno).
function origineLocale(proxy) {
  proxy.on('proxyReq', (req) => req.setHeader('origin', 'http://localhost:5173'))
  proxy.on('proxyReqWs', (req) => req.setHeader('origin', 'http://localhost:5173'))
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Necessario per raggiungere il dev server da un tunnel (hostname diverso da localhost)
    allowedHosts: true,
    // Il browser parla solo con Vite: /api e /ws vengono inoltrati al backend sulla 3001.
    // Un solo indirizzo pubblico basta per tutto.
    proxy: {
      '/api': { target: 'http://localhost:3001', configure: origineLocale },
      '/ws': { target: 'ws://localhost:3001', ws: true, configure: origineLocale },
    },
  },
})
