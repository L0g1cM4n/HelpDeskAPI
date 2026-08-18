# API de Mesa de Ayuda (Helpdesk) con SLA — Spring Boot + JWT

API REST para la gestión de tickets de soporte técnico con autenticación
JWT (access token + refresh token), autorización por rol (RBAC) y cálculo
automático de SLA según la prioridad del ticket.

## Estado del proyecto

✅ **Completado.** Implementa todos los endpoints y requisitos del taller:

- [x] Modelo de datos: `Usuario`, `Ticket`, `RefreshToken` + enums (`Rol`, `Prioridad`, `EstadoTicket`)
- [x] Repositorios Spring Data JPA
- [x] Base de datos H2 en memoria
- [x] Autenticación JWT: registro, login, refresh y logout (refresh token persistido en BD, Opción A)
- [x] Protección de rutas por autenticación y autorización por rol (RBAC)
- [x] Endpoints de tickets con reglas de negocio (cálculo de SLA, tickets vencidos)
- [x] Endpoint de administración (`POST /api/admin/soporte`)
- [x] Manejo global de excepciones (400/401/403/404/409)
- [ ] Colección Postman / Swagger (pendiente)
- [ ] Video de evidencia (pendiente)

## Stack tecnológico

| Componente     | Tecnología                  |
|----------------|------------------------------|
| Lenguaje       | Java 17                      |
| Framework      | Spring Boot 3.3.2            |
| Seguridad      | Spring Security + JWT (jjwt) |
| Persistencia   | Spring Data JPA              |
| Base de datos  | H2 (en memoria)              |

## Cómo ejecutar el proyecto

Requisitos: JDK 17+ y Maven 3.9+.

```bash
mvn spring-boot:run
```

La API quedará disponible en `http://localhost:8080`.
La consola de H2 estará disponible en `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:helpdeskdb`, usuario `sa`, sin contraseña).

## Usuarios iniciales

Al arrancar, la aplicación siembra un usuario administrador, ya que el
registro público solo crea usuarios con rol `USUARIO`:

| Email            | Password   | Rol   |
|------------------|------------|-------|
| `admin@correo.com` | `admin123` | ADMIN |

Los demás roles se obtienen así:
- `USUARIO`: cualquier `POST /api/auth/registro`.
- `SOPORTE`: un `ADMIN` asciende a un usuario existente con `POST /api/admin/soporte`.

## Estrategia de refresh token elegida

Se eligió la **Opción A: refresh token persistido en base de datos**
(entidad `RefreshToken`), porque permite revocar sesiones de forma
explícita en el logout (marcando `revocado = true`) en lugar de depender
únicamente de la expiración del token. Es más didáctico porque obliga a
razonar sobre el ciclo de vida completo del token.

## Endpoints

### 7.1 Rutas públicas (sin autenticación)

| Método | Ruta                | Descripción                                                  |
|--------|---------------------|--------------------------------------------------------------|
| POST   | `/api/auth/registro` | Registra un usuario con rol `USUARIO`                        |
| POST   | `/api/auth/login`    | Autentica y devuelve `accessToken` + `refreshToken`          |
| POST   | `/api/auth/refresh`  | Recibe un `refreshToken` válido y devuelve un nuevo `accessToken` |
| GET    | `/api/ping`          | Verifica que la API está viva (responde `{"mensaje":"pong"}`) |

### 7.2 Rutas protegidas por autenticación (cualquier usuario logueado)

| Método | Ruta                | Descripción                                            |
|--------|---------------------|--------------------------------------------------------|
| POST   | `/api/auth/logout`  | Revoca el `refreshToken` del usuario autenticado       |
| POST   | `/api/tickets`      | Crea un ticket (el creador es el usuario autenticado)  |
| GET    | `/api/tickets/mios` | Lista los tickets creados por el usuario autenticado   |
| GET    | `/api/tickets/{id}` | Consulta un ticket (solo el dueño o `SOPORTE`/`ADMIN`) |

### 7.3 Rutas protegidas por rol

| Método | Ruta                          | Rol requerido  | Descripción                                      |
|--------|-------------------------------|----------------|--------------------------------------------------|
| GET    | `/api/tickets`                | SOPORTE, ADMIN | Lista todos los tickets                          |
| PATCH  | `/api/tickets/{id}/estado`    | SOPORTE, ADMIN | Cambia el estado de un ticket                    |
| GET    | `/api/tickets/vencidos`       | SOPORTE, ADMIN | Lista los tickets que superaron su SLA           |
| POST   | `/api/admin/soporte`          | ADMIN          | Asciende a un usuario existente al rol `SOPORTE` |

> Nota: las rutas `/api/tickets/{id}` y `/api/tickets/vencidos` conviven
> sin conflicto: Spring elige siempre el patrón literal (`vencidos`) sobre
> el de variable (`{id}`).

## Ejemplos de uso

### Registro

```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{ "nombre": "Ana", "email": "ana@correo.com", "password": "123456" }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "ana@correo.com", "password": "123456" }'
```

Respuesta (201 en registro, 200 en login):

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi..."
}
```

### Crear un ticket (con Bearer accessToken)

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{ "titulo": "No enciende", "descripcion": "El equipo no prende", "prioridad": "ALTA" }'
```

Respuesta (201): el servidor define `estado`, `creadoEn`, `slaVenceEn` y `vencido`.

### Renovar el access token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{ "refreshToken": "<refreshToken>" }'
```

### Logout (revoca el refresh token)

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{ "refreshToken": "<refreshToken>" }'
```

Un `/api/auth/refresh` posterior con ese mismo token responde `401 Unauthorized`.

### Ascender a un usuario a SOPORTE (solo ADMIN)

```bash
curl -X POST http://localhost:8080/api/admin/soporte \
  -H "Authorization: Bearer <accessTokenAdmin>" \
  -H "Content-Type: application/json" \
  -d '{ "email": "ana@correo.com" }'
```

## Modelo de datos

- **Usuario**: `id`, `nombre`, `email` (único), `password` (BCrypt), `rol` (`USUARIO`, `SOPORTE`, `ADMIN`).
- **Ticket**: `id`, `titulo`, `descripcion`, `prioridad` (`BAJA`, `MEDIA`, `ALTA`), `estado` (`ABIERTO`, `EN_PROCESO`, `RESUELTO`, por defecto `ABIERTO`), `creadoEn`, `slaVenceEn` (calculado por el servidor), `creadoPor`.
- **RefreshToken**: `id`, `token`, `usuario`, `expiraEn`, `revocado`.

### Regla de SLA

Al crear un ticket, `slaVenceEn` se calcula sumando a `creadoEn`:

| Prioridad | Horas de SLA |
|-----------|--------------|
| ALTA      | 4            |
| MEDIA     | 24           |
| BAJA      | 72           |

El cliente nunca envía `slaVenceEn` ni `estado`; ambos los define el servidor.
Un ticket está **vencido** si la fecha actual superó `slaVenceEn` y su estado
no es `RESUELTO`. Cada respuesta de ticket incluye el campo `vencido`.

## Validaciones y códigos de respuesta

- `email` con formato válido y único (duplicado → `409`).
- `password` con mínimo 6 caracteres.
- `titulo` y `descripcion` obligatorios.
- `prioridad` y `estado` solo aceptan valores del enum (inválido → `400`).

| Situación                                   | Código |
|---------------------------------------------|--------|
| Creación exitosa                            | 201    |
| Consulta exitosa                            | 200    |
| Datos inválidos                             | 400    |
| Sin token / token inválido / refresh inválido | 401  |
| Rol insuficiente                            | 403    |
| Recurso no encontrado                       | 404    |
| Email ya registrado                         | 409    |

## Pendiente

- Colección Postman / Swagger para probar todos los endpoints.
- Video de evidencia (registro/login, refresh, logout, 401/403, SLA y vencidos).