# API de Mesa de Ayuda (Helpdesk) con SLA — Spring Boot + JWT

API REST para la gestión de tickets de soporte técnico con autenticación
JWT (access token + refresh token), autorización por rol (RBAC) y cálculo
automático de SLA según la prioridad del ticket.

## Estado actual del proyecto

🚧 **En construcción.** Este es un avance del taller, no la entrega final.

Completado hasta ahora:
- [x] Estructura del proyecto Maven (Spring Boot 3.3.2, Java 17)
- [x] Modelo de datos: `Usuario`, `Ticket`, `RefreshToken` + enums (`Rol`, `Prioridad`, `EstadoTicket`)
- [x] Repositorios Spring Data JPA
- [x] Configuración de base de datos H2 en memoria
- [x] Seguridad con Spring Security + filtro JWT (access token + refresh token)
- [x] Endpoints de autenticación (`/api/auth/registro`, `/login`, `/refresh`, `/logout`, `/api/ping`)
- [x] Manejo global de excepciones (400/401/403/404/409)
- [ ] Endpoints de tickets con reglas de negocio (cálculo de SLA, vencidos)
- [ ] Autorización por rol en los endpoints de tickets
- [ ] Colección Postman / Swagger
- [ ] Video de evidencia

## Endpoints de autenticación disponibles

| Método | Ruta                | Auth requerida | Descripción                                  |
|--------|---------------------|-----------------|-----------------------------------------------|
| GET    | `/api/ping`          | No              | Responde `{"mensaje":"pong"}`                 |
| POST   | `/api/auth/registro` | No              | Registra un usuario con rol `USUARIO`         |
| POST   | `/api/auth/login`    | No              | Devuelve `accessToken` + `refreshToken`       |
| POST   | `/api/auth/refresh`  | No (usa refreshToken en el body) | Devuelve un nuevo `accessToken` |
| POST   | `/api/auth/logout`   | Sí (Bearer accessToken) | Revoca el `refreshToken` enviado en el body |

Body de `/api/auth/registro`:
```json
{ "nombre": "Ana", "email": "ana@correo.com", "password": "123456" }
```
Body de `/api/auth/login`:
```json
{ "email": "ana@correo.com", "password": "123456" }
```
Body de `/api/auth/refresh` y `/api/auth/logout`:
```json
{ "refreshToken": "eyJhbGciOi..." }
```

## Stack tecnológico

| Componente     | Tecnología                  |
|----------------|------------------------------|
| Lenguaje       | Java 17                      |
| Framework      | Spring Boot 3.3.2            |
| Seguridad      | Spring Security + JWT (jjwt) |
| Persistencia   | Spring Data JPA              |
| Base de datos  | H2 (en memoria)              |

## Estrategia de refresh token elegida

Se eligió la **Opción A: refresh token persistido en base de datos**
(entidad `RefreshToken`), porque permite revocar sesiones de forma
explícita en el logout (marcando `revocado = true`) en lugar de depender
únicamente de la expiración del token. Es más didáctico porque obliga a
razonar sobre el ciclo de vida completo del token.

## Cómo ejecutar el proyecto

Requisitos: JDK 17+ y Maven 3.9+ (o usar el wrapper `./mvnw` una vez se
agregue).

```bash
mvn spring-boot:run
```

La API quedará disponible en `http://localhost:8080`.
La consola de H2 estará disponible en `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:helpdeskdb`, usuario `sa`, sin contraseña).

## Modelo de datos

- **Usuario**: `id`, `nombre`, `email` (único), `password` (BCrypt), `rol` (`USUARIO`, `SOPORTE`, `ADMIN`).
- **Ticket**: `id`, `titulo`, `descripcion`, `prioridad` (`BAJA`, `MEDIA`, `ALTA`), `estado` (`ABIERTO`, `EN_PROCESO`, `RESUELTO`), `creadoEn`, `slaVenceEn` (calculado por el servidor), `creadoPor`.
- **RefreshToken**: `id`, `token`, `usuario`, `expiraEn`, `revocado`.

### Regla de SLA

Al crear un ticket, `slaVenceEn` se calcula sumando a `creadoEn`:

| Prioridad | Horas de SLA |
|-----------|--------------|
| ALTA      | 4            |
| MEDIA     | 24           |
| BAJA      | 72           |

Un ticket está **vencido** si la fecha actual superó `slaVenceEn` y su
estado no es `RESUELTO`.

## Próximos pasos

Controlador y servicio de `Ticket` (creación con cálculo de SLA, "mis
tickets", listado general por rol, cambio de estado, listado de vencidos),
y la colección de Postman/Swagger con la evidencia en video.
