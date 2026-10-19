# Matriz de cobertura E2E: 22 rutas

Los contratos se verificaron con los controladores, DTOs, seguridad y servicios actuales. La colección agrega escenarios por ruta; esta tabla cuenta cada combinación método/ruta una sola vez. Política de propiedad ajena elegida según AuthorizationService: 403 exacto.

| # | Método y ruta | Caso(s) de colección | Actor y HTTP nominal | Negativa principal |
|---:|---|---|---|---|
| 1 | GET /api/venues/{code} | TC-BIZ-001A | Anónimo 200 | inexistente 404 |
| 2 | GET /api/venues/active | TC-BIZ-001 | Anónimo 200 | Bearer inválido 401 |
| 3 | POST /api/events | TC-BIZ-003 | ADMIN 201 | anónimo 401, USER 403, duplicado 409, enum inválido 400 |
| 4 | GET /api/events/{eventCode} | TC-BIZ-004A / TC-SEC-004A/B/015 | Público publicado 200; ADMIN draft 200 | anónimo/USER draft 403 |
| 5 | GET /api/events/published | TC-BIZ-004B / TC-SEC-005 | Anónimo 200 | borradores excluidos |
| 6 | PATCH /api/events/{eventCode}/publish | TC-BIZ-004 / TC-SEC-002A/B | ADMIN 200 | USER/STAFF 403 |
| 7 | POST /api/events/{eventCode}/artists/{artistId} | TC-BIZ-005 / TC-SEC-003 | ADMIN 200 | USER 403, duplicado 409 |
| 8 | GET /api/events/by-artist?stageName=... | TC-BIZ-005B/C | Público 200 publicados; ADMIN 200 con borradores | borradores ocultos al público |
| 9 | GET /api/artists/{id} | TC-BIZ-002A | Anónimo 200 | ausente 404 |
| 10 | GET /api/artists/by-stage-name?stageName=... | TC-BIZ-002B | Anónimo 200 | valor no existente, confirmar 404 |
| 11 | GET /api/artists/active | TC-BIZ-002 | Anónimo 200 | solo activos |
| 12 | POST /api/users | TC-AUTH-001/001B | Anónimo 201; siempre rol USER | duplicado 409, password corto 400, no escalación |
| 13 | GET /api/users/by-email?email=... | TC-SEC-006/006A/B | propietario o ADMIN 200 | otro USER 403 |
| 14 | GET /api/users/by-username?username=... | TC-SEC-007/007A | propietario o ADMIN 200 | otro USER 403 |
| 15 | POST /api/tickets | TC-BIZ-007A-D / TC-BIZ-014A/D/G/H | USER propio 201, estado PAID | borrador/edad/aforo lleno 409; anónimo 401; correo ajeno 403; enum inválido 400 |
| 16 | GET /api/tickets/{ticketCode} | TC-BIZ-008A/B | propietario o STAFF/ADMIN 200 | otro USER 403 |
| 17 | GET /api/tickets/by-user?email=... | TC-BIZ-008C | propietario o ADMIN 200 | otro USER 403 |
| 18 | GET /api/events/{eventCode}/tickets/paid | TC-BIZ-009 / TC-SEC-013 | STAFF/ADMIN 200 | USER 403; al retirar STAFF, el token anterior pierde el acceso con 403 |
| 19 | PATCH /api/tickets/{ticketCode}/cancel | TC-BIZ-010/011 / TC-SEC-011 | propietario o ADMIN 200 | ajeno 403; repetido 409 |
| 20 | PATCH /api/tickets/{ticketCode}/use | TC-BIZ-012/013 / TC-SEC-012 | STAFF/ADMIN 200 | USER 403; repetido 409 |
| 21 | POST /api/auth/login | TC-AUTH-002-006B | username/email y actores 200 | password/identificador incorrectos 401 |
| 22 | GET /api/auth/me | TC-AUTH-006C-F | autenticado 200 | ausente, inválido, expirado 401 |

## Observaciones de contratos heredados

- UserResponse no contiene password ni roles; la autoridad se confirma con /api/auth/me.
- CreateEventRequest usa LocalDateTime, category y venueCode. EventCategory válidas: MUSIC, SPORTS, TECHNOLOGY, EDUCATION, CULTURE, ENTERTAINMENT.
- PurchaseTicketRequest usa userEmail, eventCode y type. TicketType válidas: GENERAL, VIP, BACKSTAGE, STUDENT.
- Compra produce PAID, cancelación produce CANCELLED y uso produce USED. Precio se determina en el servicio.
- BusinessRuleException y DuplicateResourceException producen 409; validación y JSON inválido 400; recurso ausente 404.
- La aplicación actual devuelve 403 para recursos ajenos; no se acepta 404 como sustituto durante la ejecución.
- STAFF consulta tickets globalmente en este MVP, porque no existe asociación staff-evento.
- Fixtures STAFF/ADMIN e inactividad son externas a HTTP. No hay endpoint de administración de pruebas.

## Escenarios de negocio añadidos

| Caso | Ruta | Actor | Esperado | Comprobación de efecto |
|---|---|---|---:|---|
| TC-BIZ-014A | POST /api/tickets | USER_A, evento DRAFT | 409 | no se persiste ticket |
| TC-BIZ-014B/C | POST /api/events; PATCH /api/events/{eventCode}/publish | ADMIN | 201/200 | evento mínimo 18 queda publicado |
| TC-BIZ-014D | POST /api/tickets | USER_YOUNG | 409 | no se persiste ticket por edad insuficiente |
| TC-BIZ-014E/F | POST /api/events; PATCH /api/events/{eventCode}/publish | ADMIN, venue capacidad 1 | 201/200 | evento se publica en venue aislado |
| TC-BIZ-014G/H | POST /api/tickets | USER_A y USER_B | 201/409 | aforo queda en una única entrada pagada |
| TC-AUTH-001E/F | POST /api/users; POST /api/auth/login | USER_YOUNG sintético | 201/200 | token local disponible para la prueba de edad |
| TC-BIZ-002C | GET /api/artists/by-stage-name?stageName=... | Anónimo | 404 | ErrorResponse de recurso inexistente |
| TC-HTTP-400-03 | POST /api/auth/login con JSON malformado | Anónimo | 400 | ErrorResponse sin traza interna |

El fixture `seed-e2e.sql` también prepara `E2E-VENUE-CAPACITY` con capacidad 1. El caso de edad requiere mantener fecha de evento futura y a más de 18 años de la fecha de nacimiento del usuario joven.

