# Registro manual de ejecución E2E

Rellena una fila después de pulsar Send y revisar HTTP, headers, JSON y estado posterior cuando aplique. Estado permitido: PASS, FAIL, BLOCKED o NOT RUN. No incluyas JWT, contraseñas, datos personales reales ni secretos en esta bitácora o en capturas.

Ambiente/base E2E: ____________________  
runId: ____________________  
Fecha/hora local: ____________________  
Versión/commit probado: ____________________  
Operador: ____________________  

| Caso | Actor | Método/ruta | HTTP esperado | HTTP observado | JSON/efecto comprobado | Resultado | Evidencia/observación no sensible |
|---|---|---|---:|---:|---|---|---|
| TC-BIZ-001 | ANONYMOUS | GET /api/venues/active | 200 | pendiente | pendiente | NOT RUN | |
| TC-BIZ-002 | ANONYMOUS | GET /api/artists/active | 200 | pendiente | pendiente | NOT RUN | |
| TC-AUTH-001 | ANONYMOUS | POST /api/users | 201 | pendiente | pendiente | NOT RUN | |
| TC-AUTH-002 | ANONYMOUS | POST /api/auth/login | 200 | pendiente | pendiente | NOT RUN | |
| TC-AUTH-006C | USER_A | GET /api/auth/me | 200 | pendiente | pendiente | NOT RUN | |
| TC-AUTH-007 | ANONYMOUS | GET /api/auth/me | 401 | pendiente | pendiente | NOT RUN | |
| TC-SEC-001A | ANONYMOUS | POST /api/events | 401 | pendiente | pendiente | NOT RUN | |
| TC-BIZ-003 | ADMIN | POST /api/events | 201 | pendiente | pendiente | NOT RUN | |
| TC-SEC-004A | ANONYMOUS | GET /api/events/{eventCode} borrador | 403 | pendiente | pendiente | NOT RUN | |
| TC-BIZ-004 | ADMIN | PATCH /api/events/{eventCode}/publish | 200 | pendiente | pendiente | NOT RUN | |
| TC-BIZ-005B | ANONYMOUS | GET /api/events/by-artist | 200 | pendiente | pendiente | NOT RUN | |
| TC-SEC-006A | USER_B | GET /api/users/by-email | 403 | pendiente | pendiente | NOT RUN | |
| TC-SEC-007A | USER_B | GET /api/users/by-username | 403 | pendiente | pendiente | NOT RUN | |
| TC-BIZ-007A | USER_A | POST /api/tickets | 201 | pendiente | pendiente | NOT RUN | |
| TC-SEC-008 | USER_A | POST /api/tickets con email B | 403 | pendiente | pendiente | NOT RUN | |
| TC-SEC-009 | USER_B | GET /api/tickets/{ticketCode} A | 403 | pendiente | pendiente | NOT RUN | |
| TC-SEC-010 | USER_B | GET /api/tickets/by-user A | 403 | pendiente | pendiente | NOT RUN | |
| TC-BIZ-009 | STAFF | GET /api/events/{eventCode}/tickets/paid | 200 | pendiente | pendiente | NOT RUN | |
| TC-BIZ-010 | USER_A | PATCH /api/tickets/{ticketCode}/cancel | 200 | pendiente | pendiente | NOT RUN | |
| TC-BIZ-012 | STAFF | PATCH /api/tickets/{ticketCode}/use | 200 | pendiente | pendiente | NOT RUN | |
| TC-AUTH-008 | ANONYMOUS + Bearer inválido | GET /api/auth/me | 401 | pendiente | pendiente | NOT RUN | |
| TC-HTTP-* | según escenario | validación/ausente/duplicado | 400/404/409 | pendiente | pendiente | NOT RUN | |
| TC-AUTH-001E | ANONYMOUS | POST /api/users | 201 | pendiente | usuario joven creado | NOT RUN | |
| TC-AUTH-001F | ANONYMOUS | POST /api/auth/login | 200 | pendiente | token copiado localmente | NOT RUN | |
| TC-BIZ-014A | USER_A | POST /api/tickets, evento borrador | 409 | pendiente | no existe ticket nuevo | NOT RUN | |
| TC-BIZ-014B | ADMIN | POST /api/events, edad mínima | 201 | pendiente | evento DRAFT con minimumAge 18 | NOT RUN | |
| TC-BIZ-014C | ADMIN | PATCH /api/events/{eventCode}/publish | 200 | pendiente | evento PUBLISHED | NOT RUN | |
| TC-BIZ-014D | USER_YOUNG | POST /api/tickets, edad insuficiente | 409 | pendiente | no existe ticket nuevo | NOT RUN | |
| TC-BIZ-014E | ADMIN | POST /api/events, venue capacidad 1 | 201 | pendiente | evento DRAFT en fixture aislada | NOT RUN | |
| TC-BIZ-014F | ADMIN | PATCH /api/events/{eventCode}/publish | 200 | pendiente | evento PUBLISHED | NOT RUN | |
| TC-BIZ-014G | USER_A | POST /api/tickets, primera compra aforo 1 | 201 | pendiente | una entrada PAID | NOT RUN | |
| TC-BIZ-014H | USER_B | POST /api/tickets, aforo agotado | 409 | pendiente | no hay segunda entrada PAID | NOT RUN | |
| TC-BIZ-002C | ANONYMOUS | GET /api/artists/by-stage-name | 404 | pendiente | ErrorResponse de recurso inexistente | NOT RUN | |
| TC-HTTP-400-03 | ANONYMOUS | POST /api/auth/login JSON inválido | 400 | pendiente | ErrorResponse sin traza | NOT RUN | |
Resumen manual: PASS ___ | FAIL ___ | BLOCKED ___ | NOT RUN ___  
Rutas únicas nominales ejecutadas: ___ / 22  
Defectos/regresiones: ______________________________________________  
Bloqueos y precondición que falta: ___________________________________  

Reglas: 500 inesperado = FAIL; prerrequisito faltante = BLOCKED; no ejecutado = NOT RUN. Una respuesta 2xx no basta: hay que validar DTO, actor y efecto persistido. No marques como aprobados casos omitidos.

