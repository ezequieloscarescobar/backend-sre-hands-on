# streaming-service — Contexto de Negocio

## ¿Qué hace este servicio?

El `streaming-service` es el microservicio central de MeliPlay. Gestiona el inicio,
la continuación y la finalización de reproducciones de películas, series y episodios
bajo un modelo de suscripción.

## Flujo de una reproducción

Antes de iniciar una reproducción, el servicio:
1. Verifica que el usuario tenga suscripción activa (vía `subscription-service`).
2. Verifica que el contenido esté disponible en la región del usuario (vía `catalog-service`).
3. Registra la sesión.
4. Notifica el inicio al `notification-service` (fire-and-forget).

## Estados de una sesión

```
INITIATED → IN_PROGRESS → COMPLETED
               ↕
            PAUSED
         (desde cualquier estado) → FAILED
```

## Reglas de negocio

- Sin suscripción activa: no se puede reproducir.
- Plan BASIC: máximo 1 pantalla simultánea.
- Plan STANDARD: máximo 2 pantallas simultáneas.
- Plan PREMIUM: máximo 4 pantallas simultáneas.
- El contenido debe estar disponible en la región del usuario.

## Interacciones con otros servicios

- **subscription-service**: consultado en cada intento de reproducción.
- **catalog-service**: consultado para validar disponibilidad del contenido.
- **notification-service**: notificado tras cada inicio exitoso (no bloquea).

## Cómo correr el servicio

```bash
mvn spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.
No requiere ningún backend externo: los servicios dependientes están simulados internamente.

## Usuarios de prueba

| userId | Plan | Estado |
|---|---|---|
| `premium_user1` | PREMIUM | Activo |
| `basic_user1` | BASIC | Activo |
| `expired_user1` | STANDARD | Vencida |
| `slow_user1` | PREMIUM | Activo (latencia alta simulada) |
| `user123` | STANDARD | Activo |

## Contenidos de prueba

| contentId | Disponibilidad | Calidades |
|---|---|---|
| `content_AR_001` | Solo región AR | SD, HD |
| `content_BR_001` | Solo región BR | SD, HD |
| `content_4k_001` | Todas las regiones | SD, HD, 4K |
| `movie_001` | Todas las regiones | SD, HD |
