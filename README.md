# Helpdesk API - Avance

API REST de Mesa de Ayuda (Helpdesk) con SLA y autenticación JWT, desarrollada con Spring Boot 3.

## Stack tecnológico

- Java 17+
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- H2 (base de datos en memoria)
- JWT (jjwt 0.12.3)
- Lombok

## Avance actual

### Modelo de datos implementado

- **Usuario** - `id`, `nombre`, `email` (único), `password`, `rol` (USUARIO, SOPORTE, ADMIN)
- **Ticket** - `id`, `titulo`, `descripcion`, `prioridad` (BAJA, MEDIA, ALTA), `estado` (ABIERTO, EN_PROCESO, RESUELTO), `creadoEn`, `slaVenceEn` (calculado automáticamente), `creadoPor` (relación con Usuario)
- **RefreshToken** - `id`, `token`, `usuario`, `expiraEn`, `revocado` (Opción A: persistido en BD para permitir revocación real)

### Regla de negocio: SLA

Al crear un ticket, el servidor calcula automáticamente `slaVenceEn`:
| Prioridad | SLA |
|-----------|-----|
| ALTA      | 4h  |
| MEDIA     | 24h |
| BAJA      | 72h |

El ticket expone un campo `vencido` que es `true` si la fecha actual superó el SLA y el estado no es `RESUELTO`.

### Seguridad

- Configuración base con Spring Security (sin CSRF, stateless)
- Endpoint público `GET /api/ping` → `{"mensaje":"pong"}`
- Contraseñas cifradas con BCrypt
- Pendiente: implementar JWT, refresh token y roles

### Endpoints implementados

| Método | Ruta       | Auth | Descripción                    |
|--------|------------|------|--------------------------------|
| GET    | /api/ping  | No   | Verifica que la API responde   |

### Pendiente para completar

- Registro y login con JWT (access + refresh token)
- Renovación de access token vía `/api/auth/refresh`
- Logout con revocación de refresh token
- CRUD de tickets con protección por rol
- Asignación de rol SOPORTE (solo ADMIN)
- Listado de tickets vencidos
- Validaciones y manejo de errores
- Colección de Postman/Thunder Client

## Requisitos

- JDK 17+
- Maven 3.8+

## Ejecución

```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/api-1.0.0.jar

# La API queda disponible en http://localhost:8080
# Consola H2: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:helpdesk)
```

Verificar que funciona:

```bash
curl http://localhost:8080/api/ping
# Respuesta: {"mensaje":"pong"}
```

## Estrategia de Refresh Token

Se eligió la **Opción A (persistido en base de datos)** porque:
- Permite revocación real de tokens (logout efectivo)
- Es más didáctica para comprender el ciclo de vida del token
- Permite invalidar sesiones específicas
