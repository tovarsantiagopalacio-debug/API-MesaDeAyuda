# Helpdesk API - Mesa de Ayuda con SLA

API REST de Mesa de Ayuda (Helpdesk) con SLA y autenticación JWT, desarrollada con Spring Boot 3.

## Stack tecnológico

- Java 17+
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- H2 (base de datos en memoria)
- JWT (jjwt 0.12.3)
- Lombok

## Modelo de datos

- **Usuario** - `id`, `nombre`, `email` (único), `password`, `rol` (USUARIO, SOPORTE, ADMIN)
- **Ticket** - `id`, `titulo`, `descripcion`, `prioridad` (BAJA, MEDIA, ALTA), `estado` (ABIERTO, EN_PROCESO, RESUELTO), `creadoEn`, `slaVenceEn`, `creadoPor`
- **RefreshToken** - `id`, `token`, `usuario`, `expiraEn`, `revocado`

## Regla de negocio: SLA

Al crear un ticket, el servidor calcula automáticamente `slaVenceEn`:

| Prioridad | SLA |
|-----------|-----|
| ALTA      | 4h  |
| MEDIA     | 24h |
| BAJA      | 72h |

## Estrategia de Refresh Token

Se eligió la **Opción A (persistido en base de datos)** porque:
- Permite revocación real de tokens (logout efectivo)
- Es más didáctica para comprender el ciclo de vida del token
- Permite invalidar sesiones específicas

## Seguridad

- Contraseñas cifradas con BCrypt
- Access token JWT (15 min) con claims: email, rol
- Refresh token persistido en BD (7 días) con rotación automática
- Rutas protegidas por autenticación y rol (RBAC)
- CSRF deshabilitado, sesión stateless

## Endpoints

### Públicos

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/ping | Verifica API viva (pong) |
| POST | /api/auth/registro | Registra usuario (rol USUARIO) |
| POST | /api/auth/login | Login, devuelve access + refresh token |
| POST | /api/auth/refresh | Renueva access token con refresh token |

### Autenticados

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/auth/logout | Revoca refresh token |
| POST | /api/tickets | Crea ticket (creador = autenticado) |
| GET | /api/tickets/mios | Tickets del usuario autenticado |
| GET | /api/tickets/{id} | Ver ticket (dueño o SOPORTE/ADMIN) |

### Roles SOPORTE/ADMIN

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/tickets | Lista todos los tickets |
| GET | /api/tickets/vencidos | Tickets con SLA vencido |
| PATCH | /api/tickets/{id}/estado | Cambia estado del ticket |

### Rol ADMIN

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/admin/soporte | Asciende un usuario a rol SOPORTE |

## Requisitos

- JDK 17+
- Maven 3.8+

## Ejecución

```bash
mvn clean package -DskipTests
java -jar target/api-1.0.0.jar
```

La API queda disponible en `http://localhost:8080`.

Consola H2: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:helpdesk`
- Usuario: `sa`
- Password: (vacío)

## Datos de prueba

| Rol | Email | Password |
|-----|-------|----------|
| ADMIN | admin@helpdesk.com | admin123 |
| SOPORTE | soporte@helpdesk.com | soporte123 |
| USUARIO | usuario@helpdesk.com | usuario123 |

## Verificación rápida

```bash
curl http://localhost:8080/api/ping
# {"mensaje":"pong"}
```

## Flujo de autenticación

1. Login: `POST /api/auth/login` con email y password
2. Usar `accessToken` en header `Authorization: Bearer <accessToken>`
3. Al expirar (401), llamar `POST /api/auth/refresh` con `refreshToken`
4. Logout: `POST /api/auth/logout` (revoca refresh token)
