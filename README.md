# PROGETTO-SETTIMANA-18 – Mini clone di WhatsApp

Chat 1-a-1 con Spring Boot (REST + WebSocket/STOMP, login JWT) e front-end React.

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

## Avvio

Back-end (porta 3001) – richiede PostgreSQL locale e il file `BE/env.properties` (non versionato):

```properties
DB_PASSWORD=...
JWT_SECRET=una-stringa-di-almeno-32-caratteri
```

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
| POST | `/api/auth/register` | no | `{ username, email, password }` → 201 |
| POST | `/api/auth/login` | no | `{ email, password }` → `{ token }` |
| POST | `/api/auth/logout` | sì | token in blacklist → 204 |
| GET | `/api/utenti/me` | sì | utente loggato |
| GET | `/api/utenti` | sì | tutti gli altri utenti |
| POST | `/api/chat` | sì | `{ destinatarioId }` → crea o restituisce la chat esistente |
| GET | `/api/chat` | sì | chat dell'utente loggato |
| POST | `/api/messaggi` | sì | `{ destinatarioId, testo }` → 201, poi push WS |
| GET | `/api/chat/{chatId}/messaggi?page=0&size=50` | sì | cronologia, pagina 0 = più recenti |
| PATCH | `/api/chat/{chatId}/messaggi/consegnati` | sì | i miei messaggi ricevuti `SPEDITO` → `CONSEGNATO`; il mittente riceve lo stato aggiornato via WS |

WebSocket: endpoint `ws://localhost:3001/ws`, JWT nell'header `Authorization` del frame CONNECT.
`SEND /app/messaggi` con `{ destinatarioId, testo }`; `SUBSCRIBE /user/queue/messaggi` per ricevere.
