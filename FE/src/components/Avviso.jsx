// Riquadro per errori (rosso) o conferme (verde); non renderizza nulla se il testo e' vuoto
function Avviso({ testo, tipo = 'errore' }) {
  if (!testo) return null
  return <p className={`avviso ${tipo}`}>{testo}</p>
}

export default Avviso
