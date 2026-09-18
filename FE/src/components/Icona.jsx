// Icone SVG inline (tratto 2px, currentColor): niente font o file esterni.
// Ogni voce e' il contenuto del path; viewBox fisso 24x24.
const PATH = {
  indietro: 'M15 18l-6-6 6-6',
  invia: 'M22 2L11 13M22 2l-7 20-4-9-9-4 22-7z',
  statistiche: 'M4 20V10M10 20V4M16 20v-6M22 20H2',
  esci: 'M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9',
  scintilla: 'M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3zM19 17l.7 2 2 .7-2 .7-.7 2-.7-2-2-.7 2-.7.7-2z',
  chat: 'M21 12a8 8 0 01-11.6 7.1L4 20l1.1-4.4A8 8 0 1121 12z',
  chiudi: 'M18 6L6 18M6 6l12 12',
}

/** <Icona nome="invia" /> — nomi disponibili: le chiavi di PATH. */
function Icona({ nome, dimensione = 22 }) {
  return (
    <svg width={dimensione} height={dimensione} viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d={PATH[nome]} />
    </svg>
  )
}

export default Icona
