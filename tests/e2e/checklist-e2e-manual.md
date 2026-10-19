# Checklist de ejecución manual E2E

Completar en Postman con envíos individuales Send. Estado inicial de todos los casos: NOT RUN. No guardar tokens, passwords ni capturas con datos sensibles.

## Preflight

- [ ] TC-BIZ-001 — GET /api/venues/active; HTTP 200; venue E2E-VENUE visible.
- [ ] TC-BIZ-001A — GET /api/venues/{code}; HTTP 200.
- [ ] TC-BIZ-002 — GET /api/artists/active; HTTP 200; copiar id de Solar Beat.
- [ ] TC-BIZ-002A — GET /api/artists/{id}; HTTP 200.
- [ ] TC-BIZ-002B — GET /api/artists/by-stage-name; HTTP 200.
- [ ] TC-BIZ-004B inicial — GET /api/events/published; HTTP 200 y no filtra borradores.

## Registro y autenticación

- [ ] TC-AUTH-001 — registrar USER_A con intento role=ADMIN; HTTP 201, sin credenciales.
- [ ] TC-AUTH-001B — registrar USER_B; HTTP 201.
- [ ] TC-AUTH-001C — duplicado; HTTP 409.
- [ ] TC-AUTH-001D — password corto; HTTP 400 y no hay alta.
- [ ] TC-AUTH-002/003 — login A por username y email; HTTP 200.
- [ ] TC-AUTH-004/005 — password incorrecto e identificador inexistente; HTTP 401 genérico equivalente.
- [ ] TC-AUTH-006 — login USER_B; HTTP 200.
- [ ] TC-AUTH-006A — login STAFF; HTTP 200.
- [ ] TC-AUTH-006B — login ADMIN; HTTP 200.
- [ ] TC-AUTH-006C/006D/006E/006F — /me para A, B, STAFF y ADMIN; HTTP 200 y roles correctos.
- [ ] TC-AUTH-007 — /me anónimo; HTTP 401.
- [ ] TC-AUTH-008 — Bearer inventado; HTTP 401.
- [ ] TC-AUTH-009 — firma alterada; HTTP 401.
- [ ] TC-AUTH-010 — token vencido; HTTP 401.
- [ ] TC-AUTH-011/011A — firma distinta e issuer distinto; HTTP 401 o BLOCKED si no hay fixture segura.
- [ ] TC-AUTH-012 — token en query string; HTTP 401.
- [ ] TC-AUTH-013 — usuario inactivo con token previo; HTTP 401; restaurar usuario.
- [ ] TC-AUTH-014 — USER en endpoint ADMIN; HTTP 403.
- [ ] TC-SEC-017 — quitar rol STAFF conservando token; lectura pagada denegada con HTTP 403 y rol restaurado.
- [ ] TC-AUTH-015 — Bearer inválido en endpoint público; HTTP 401 según filtro actual.

## Usuarios

- [ ] TC-SEC-006/007 — A consulta por su email y username; HTTP 200.
- [ ] TC-SEC-006A/007A — B consulta datos de A; HTTP 403 y sin datos de A.
- [ ] TC-SEC-006B — ADMIN consulta A; HTTP 200, sin hash.
- [ ] Confirmar en /me que el registro de A nunca concede ADMIN.

## Eventos

- [ ] TC-SEC-001A — visitante crea evento; HTTP 401.
- [ ] TC-SEC-001B — USER crea evento; HTTP 403.
- [ ] TC-BIZ-003 — ADMIN crea borrador; HTTP 201, status DRAFT.
- [ ] TC-BIZ-003A — código repetido; HTTP 409.
- [ ] TC-SEC-004A/004B — visitante y USER leen borrador; HTTP 403, sin contenido.
- [ ] TC-SEC-015 — ADMIN lee borrador; HTTP 200.
- [ ] TC-SEC-005 — lista pública antes de publicar; HTTP 200, sin borrador.
- [ ] TC-SEC-003 — USER asocia artista; HTTP 403, sin mutación.
- [ ] TC-BIZ-005 — ADMIN asocia artista; HTTP 200.
- [ ] TC-BIZ-005A — asociación repetida; HTTP 409.
- [ ] TC-SEC-002A/002B — USER y STAFF publican; HTTP 403; evento sigue DRAFT.
- [ ] TC-BIZ-004 — ADMIN publica; HTTP 200, estado PUBLISHED.
- [ ] TC-BIZ-004A/004B — lectura por código y listado público; HTTP 200, estado visible.
- [ ] TC-BIZ-005B/005C — búsqueda por artista pública y ADMIN; HTTP 200 con alcance esperado.
- [ ] TC-HTTP-400-01 — categoría inválida; HTTP 400 y sin evento.

## Entradas

- [ ] TC-AUTH-007A — compra anónima; HTTP 401.
- [ ] TC-BIZ-007A/B/C/D — compra A, tickets separados y compra B; cada una HTTP 201.
- [ ] TC-SEC-008 — A compra usando correo de B; HTTP 403 y sin ticket nuevo.
- [ ] TC-HTTP-400-02 — TicketType inválido; HTTP 400 y sin ticket.
- [ ] TC-BIZ-008A/008B — A y STAFF consultan ticket; HTTP 200.
- [ ] TC-SEC-009 — B lee ticket de A; HTTP 403 y sin datos.
- [ ] TC-BIZ-008C — A lista sus tickets; HTTP 200.
- [ ] TC-SEC-010 — B lista tickets de A; HTTP 403 y sin datos.
- [ ] TC-SEC-013 — USER lista tickets pagados; HTTP 403.
- [ ] TC-BIZ-009 — STAFF lista pagados; HTTP 200.
- [ ] TC-SEC-011 — B cancela ticket de A; HTTP 403; A comprueba que sigue PAID.
- [ ] TC-BIZ-010 — A cancela su ticket; HTTP 200/CANCELLED.
- [ ] TC-BIZ-011 — segunda cancelación; HTTP 409; sin nueva mutación.
- [ ] TC-SEC-012 — USER intenta marcar USED; HTTP 403; sigue PAID.
- [ ] TC-BIZ-012 — STAFF marca USED; HTTP 200/USED.
- [ ] TC-BIZ-013 — segundo uso; HTTP 409; sigue USED.
- [ ] TC-BIZ-010A/011A/012A/013A — lecturas finales comprueban estados y propiedad.

## Errores y cierre

- [ ] TC-HTTP-404-01/02/03 — recursos inexistentes; HTTP 404/ErrorResponse.
- [ ] Revisar 400, 401, 403, 404 y 409: cinco campos ErrorResponse; status coincide; sin datos internos.
- [ ] Registrar cada solicitud como PASS, FAIL, BLOCKED o NOT RUN; un 500 inesperado es FAIL.
- [ ] Recuento manual final de rutas únicas: 22/22. Variantes negativas cuentan como escenarios, no como rutas nuevas.
- [ ] Borrar los valores locales de tokens/passwords antes de compartir el ambiente o la evidencia.

## Reglas de negocio extra (TC-BIZ-014)

- [ ] TC-BIZ-014A — comprar antes de publicar; HTTP 409, sin entrada.
- [ ] TC-AUTH-001E/F — registrar y autenticar usuario sintético joven; HTTP 201/200.
- [ ] TC-BIZ-014B/C — ADMIN crea y publica evento con minimumAge=18; HTTP 201/200.
- [ ] TC-BIZ-014D — USER_YOUNG intenta comprar; HTTP 409 y sin nueva entrada.
- [ ] TC-BIZ-014E/F — ADMIN crea y publica evento en venue de capacidad 1; HTTP 201/200.
- [ ] TC-BIZ-014G — A compra única entrada; HTTP 201.
- [ ] TC-BIZ-014H — B intenta exceder aforo; HTTP 409; solo una entrada PAID.
- [ ] Antes de repetir edad: configurar fecha de evento futura y al menos 18 años posterior a la fecha de nacimiento de USER_YOUNG.

- [ ] TC-BIZ-002C — artista por nombre artístico inexistente; HTTP 404/ErrorResponse.
- [ ] TC-HTTP-400-03 — JSON malformado en login; HTTP 400/ErrorResponse y sin traza.
