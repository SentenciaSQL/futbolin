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
flutter create . --platforms=android,ios
flutter pub get
flutter run --dart-define=API_URL=http://10.0.2.2:8080 --dart-define=WS_URL=ws://10.0.2.2:8080/ws/match?token=
```

Idiomas: español e inglés (`assets/i18n`). Preparado para PT/FR/IT/DE.

## WebSocket

`/ws/match?token=<access JWT>`

Eventos: `QUEUE`, `ANSWER`, `REMATCH`, `EMOJI` y del servidor `MATCH_FOUND`, `QUESTION`, `ANSWER_RESULT`, `BALL_MOVED`, `GOAL`, `MATCH_FINISHED`, `PLAYER_DISCONNECTED`, `PLAYER_RECONNECTED`.

El cliente **no** envía el tiempo de respuesta. El servidor calcula `responseMs` al recibir el mensaje y rechaza respuestas imposiblemente rápidas.

## Importación masiva

CSV / Excel / JSON con columnas:

`question, option_a, option_b, option_c, option_d, correct_answer, category, difficulty, explanation`

Ejemplo: `docs/sample-questions.csv`. Los duplicados se detectan por el texto en español.

## API versionada

`/api/v1/auth/register|login|refresh`
`/api/v1/users/me`
`/api/v1/matches/queue|private`
`/api/v1/rankings`
`/api/v1/missions`
`/api/v1/store/cosmetics`
`/api/v1/admin/**` (rol ADMIN)

## Motor de juego

- 10 segundos por pregunta
- 4 minutos o primero a 3 goles
- Empate → penales de trivia (5) y muerte súbita
- Abandono si no reconecta en 15 segundos
