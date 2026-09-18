# PROGETTO-SETTIMANA-18 – Mini clone di WhatsApp

Chat 1-a-1 con Spring Boot (REST + WebSocket/STOMP, login JWT) e front-end React.
Registrazione con conferma via email, login anche con codice via email, suggerimento IA del prossimo
messaggio e statistiche personali spedite via email con un template Thymeleaf.

## Come funziona

- Ogni utente registrato vede l'elenco degli altri utenti e apre una chat cliccando su una persona.
- Il messaggio viene **prima salvato** a DB (id e `sent_at` assegnati dal server) e **poi** consegnato
  via WebSocket sulla coda personale `/user/queue/messaggi` di destinatario e mittente: nessun broadcast.
- Il **mittente** non è nel body: è il principal della sessione (JWT su HTTP, `UtentePrincipal` sulla sessione STOMP).
- Se il destinatario è online il messaggio nasce `CONSEGNATO`; altrimenti resta `SPEDITO` a DB e viene
  segnato consegnato (`PATCH`) quando il destinatario apre la chat.
- Aprendo una chat il client legge la cronologia via REST e la unisce ai messaggi WS **senza duplicati** (per id)
  e nell'ordine deciso dal server (`sent_at`).
- Una sola chat per coppia di utenti: la coppia viene ordinata per id e c'è un vincolo `UNIQUE(part1_id, part2_id)`.

### Auth via email (Gmail SMTP)

- Alla registrazione l'utente nasce **non attivo** (`is_active = false`) e riceve un link
  `FRONTEND_URL/verify?email=...&code=...` (token di 32 caratteri, valido 24 ore, monouso).
  La pagina `/verify` del FE chiama `POST /api/auth/verify` e attiva l'account.
- Login con password **oppure** con codice: `POST /api/auth/request-code` spedisce un codice a 6 cifre
  (valido 10 minuti, monouso), `POST /api/auth/login-code` lo scambia con il JWT.
- Se l'email non esiste o l'account non è attivo `request-code` risponde comunque 200: non si rivela chi è registrato.
- Salvataggio e invio mail stanno nella stessa transazione: se l'SMTP fallisce, non resta nulla a DB.
- Nella lista "persone con cui parlare" compaiono solo gli utenti che hanno confermato l'email.

### Suggerimento IA

- `POST /api/chat/{chatId}/suggerimento` manda a OpenRouter le regole in `BE/src/main/resources/agente/suggeritore.txt`
  più la trascrizione degli ultimi N messaggi (`app.llm.max-messaggi-contesto`) e restituisce `{ testo }`.
- Il suggerimento **non viene salvato**: il FE lo mette nel campo di scrittura e l'utente decide se inviarlo.

### Statistiche

- `GET /api/statistiche` → `{ messaggiInviati, messaggiRicevuti, chatAperte }` dell'utente loggato.
- `POST /api/statistiche/email` le spedisce all'indirizzo di registrazione come email HTML generata dal
  template Thymeleaf `BE/src/main/resources/templates/email/statistiche.html` (`th:text` fa l'escape dei valori).

## Avvio

Back-end (porta 3001) – richiede PostgreSQL locale e il file `BE/env.properties` (non versionato):

```properties
DB_PASSWORD=...
JWT_SECRET=una-stringa-di-almeno-32-caratteri
```

Email e IA si configurano con **variabili d'ambiente** (hanno precedenza sul file; se mancano l'avvio fallisce subito):

```bash
export MAIL_USERNAME=tuo.indirizzo@gmail.com      # mittente Gmail
export MAIL_PASSWORD=app-password-16-caratteri    # Google Account > Sicurezza > Password per le app
export OPENROUTER_API_KEY=sk-or-...               # chiave OpenRouter per il suggerimento IA
```

Se il DB esiste già da prima, gli utenti vecchi risultano non attivi: `UPDATE utenti SET is_active = true;`

```bash
createdb u5d7
cd BE && mvn spring-boot:run
```

Front-end (porta 5173):

```bash
cd FE && npm install && npm run dev
```

## Endpoint

| Metodo | Path | Auth | Body / note |
|---|---|---|---|
| POST | `/api/auth/register` | no | `{ username, email, password }` → 201, invia il link di verifica |
| POST | `/api/auth/verify` | no | `{ email, codice }` → attiva l'account |
| POST | `/api/auth/login` | no | `{ email, password }` → `{ token }` (solo account attivi) |
| POST | `/api/auth/request-code` | no | `{ email }` → invia il codice a 6 cifre |
| POST | `/api/auth/login-code` | no | `{ email, codice }` → `{ token }` |
| POST | `/api/auth/logout` | sì | token in blacklist → 204 |
| GET | `/api/utenti/me` | sì | utente loggato |
| GET | `/api/utenti` | sì | gli altri utenti attivi |
| POST | `/api/chat` | sì | `{ destinatarioId }` → crea o restituisce la chat esistente |
| GET | `/api/chat` | sì | chat dell'utente loggato |
| POST | `/api/chat/{chatId}/suggerimento` | sì | `{ testo }` proposto dall'IA, non salvato |
| GET | `/api/statistiche` | sì | `{ messaggiInviati, messaggiRicevuti, chatAperte }` |
| POST | `/api/statistiche/email` | sì | spedisce le statistiche via email (Thymeleaf) |
| POST | `/api/messaggi` | sì | `{ destinatarioId, testo }` → 201, poi push WS |
| GET | `/api/chat/{chatId}/messaggi?page=0&size=50` | sì | cronologia, pagina 0 = più recenti |
| PATCH | `/api/chat/{chatId}/messaggi/consegnati` | sì | i miei messaggi ricevuti `SPEDITO` → `CONSEGNATO`; il mittente riceve lo stato aggiornato via WS |

WebSocket: endpoint `ws://localhost:3001/ws`, JWT nell'header `Authorization` del frame CONNECT.
`SEND /app/messaggi` con `{ destinatarioId, testo }`; `SUBSCRIBE /user/queue/messaggi` per ricevere.
