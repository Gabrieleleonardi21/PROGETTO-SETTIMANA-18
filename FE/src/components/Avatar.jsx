/**
 * Cerchio con l'iniziale dello username. Il colore deriva dal nome (hash → tonalita'),
 * cosi' la stessa persona ha sempre lo stesso colore senza salvare nulla.
 */
function Avatar({ nome, grande = false }) {
  let hash = 0
  for (const c of nome) {
    hash = (hash * 31 + c.charCodeAt(0)) % 360
  }
  let classe = 'avatar'
  if (grande) classe = 'avatar grande'
  return (
    <span className={classe} style={{ background: `hsl(${hash} 45% 50%)` }} aria-hidden="true">
      {nome.charAt(0).toUpperCase()}
    </span>
  )
}

export default Avatar
