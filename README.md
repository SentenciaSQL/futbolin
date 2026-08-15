# Futbolín

Trivia de fútbol multijugador para **Android e iOS** (Flutter) con backend **Spring Boot** y panel admin **Angular**.

La partida no es un cuestionario lineal: dos jugadores compiten en tiempo real sobre un campo (portería → defensa → mediocampo → ataque → portería). El servidor es la autoridad de tiempos, respuestas, posesión, goles y resultado.

## Arquitectura

```
mobile/     Flutter + Riverpod + GoRouter + Dio + WebSocket
backend/    Spring Boot 3.4 / Java 21 / PostgreSQL / Redis / JWT / WebSocket
admin/      Angular 19 + Tailwind
```

Clean Architecture en el backend:

- `api` presentación REST + WebSocket
- `application` casos de uso
- `domain` motor de partido, Elo y progresión (sin I/O)
- `data` JPA + repositorios
- `core` seguridad, errores, config

## Fase cubierta

1. Fundación: auth JWT (access + refresh), perfil, preguntas, admin, Flyway, Docker
2. Trivia individual: supervivencia y pregunta del día
3. Multijugador: matchmaking, salas privadas `FUT-XXXX`, WebSocket, reconexión 15s
4. Mecánica de campo: posesión, avance, ocasión y gol
5. Competitividad: Elo, divisiones, temporadas, historial, rivalidades
6. Retención: XP, misiones, logros, racha diaria, cosméticos (solo cosmetics, no pay-to-win)
7. Observabilidad: Actuator, métricas Prometheus, logs, health, rate limit, anti-cheat básico

## Cómo levantar el stack

```bash
docker compose up --build
```

Servicios:

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Admin: http://localhost:4200
- PostgreSQL `football_trivia` / `postgres` / `postgres` (solo desarrollo)
- Redis: 6379

Credenciales de desarrollo del panel:

- `admin@futbolin.app` / `Admin123!`

Nunca uses estas credenciales en producción. Define `JWT_SECRET`, `DATABASE_PASSWORD` y OAuth por variables de entorno.

## Backend local (sin Docker)

Requiere Java 21, PostgreSQL y Redis.

```bash
cd backend
./gradlew test
./gradlew bootRun
```

Los tests usan H2 y no necesitan Redis (`app.redis.enabled=false`).

## App móvil

```bash
cd mobile
flutter pub get
flutter run --dart-define=API_URL=http://10.0.2.2:8080 --dart-define=WS_URL=ws://10.0.2.2:8080/ws/match?token=
```

Si el proyecto iOS no tiene `.xcodeproj` (se genera en un Mac):

```bash
flutter create . --platforms=ios
```

Idiomas: ES, EN, PT, FR, IT, DE (`assets/i18n`). Celebración de gol con Lottie (`assets/animations/goal.json`). Enlaces de invitación: `futbolin://join/FUT-XXXX`.

## WebSocket

`/ws/match?token=<access JWT>`

Eventos cliente: `QUEUE`, `CANCEL_QUEUE`, `ANSWER`, `REMATCH`, `EMOJI`, `MUTE`, `HEARTBEAT`.
Eventos servidor: `MATCH_FOUND`, `QUESTION`, `ANSWER_RESULT`, `BALL_MOVED`, `GOAL`, `MATCH_FINISHED`, `PLAYER_DISCONNECTED`, `PLAYER_RECONNECTED`, `EMOJI`, `MUTED`.

El cliente **no** envía el tiempo de respuesta. El servidor calcula `responseMs` al recibir el mensaje y rechaza respuestas imposiblemente rápidas.

## API versionada

`/api/v1/auth/register|login|refresh|social`
`/api/v1/users/me` · historial, rivalidades, perfil público `GET /users/{id}`
`/api/v1/matches/queue|private` (invite `futbolin://join/FUT-XXXX`)
`/api/v1/friends` · ranking de amigos
`/api/v1/tournaments` · copa de 16, octavos → final
`/api/v1/devices` · token FCM
`/api/v1/rankings` · global, semanal, amigos, temporada
`/api/v1/missions`
`/api/v1/store/cosmetics`
`/api/v1/admin/**` (rol ADMIN): preguntas, temporadas, misiones, cosméticos, torneos

## Torneos

Cuadro fijo de 16 jugadores (1v16 … 8v9). Al llenarse el cupo arrancan los octavos. El ganador de cada cruce avanza; el servidor es autoridad del resultado.

## Push y OAuth

- Los tokens de dispositivo se guardan en `device_tokens`. Con `FCM_PROJECT_ID` el backend registra el envío (implementación lista para HTTP v1).
- Google/Apple: verificación JWKS (Nimbus). En desarrollo, `app.oauth.allow-insecure-dev=true` acepta JWT sin firma para tests.

## Motor de juego

- 10 segundos por pregunta
- 4 minutos o primero a 3 goles
- Empate → penales de trivia (5) y muerte súbita
- Abandono si no reconecta en 15 segundos
- El Elo ranked no se aplica a torneos ni partidas privadas
