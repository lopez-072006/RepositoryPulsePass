# PulsePass - pruebas E2E manuales con Postman

## Objetivo y alcance

Esta fase comprueba la API completa por HTTP real: Postman -> filtros Spring Security -> controladores -> servicios -> repositorios -> PostgreSQL. La ejecución es manual, solicitud por solicitud, con el botón **Send**. La colección no contiene scripts, y no se usa Collection Runner, Newman, Postman CLI, cURL ni mocks.

La colección cubre las 22 rutas heredadas (20 de negocio y 2 de autenticación) y agrega solicitudes negativas para roles, propiedad, JWT, validación y transiciones. Incluir una solicitud no significa que ya se haya ejecutado ni aprobado: la evidencia se registra en registro-ejecucion-manual.md.

## Entregables

- postman/PulsePass-Manual-E2E.postman_collection.json
- postman/PulsePass-local.postman_environment.json (plantilla sin secretos)
- fixtures/seed-e2e.sql y herramientas de fixtures locales
- checklist-e2e-manual.md
- registro-ejecucion-manual.md
- matriz-cobertura-e2e.md

## Preparar un ambiente aislado

1. Instala Java 21, PostgreSQL y Postman Desktop. Usa solo una base local exclusiva llamada **pulsepass_e2e**; no apuntes a desarrollo compartido, QA ni producción.
2. Crea la base pulsepass_e2e y configura el runtime de PulsePass para conectarse allí. En PowerShell, antes de iniciar la aplicación:
   
   ~~~powershell
   $env:DB_URL = 'jdbc:postgresql://localhost:5432/pulsepass_e2e'
   $env:DB_USER = 'postgres'
   $env:DB_PASSWORD = '<contraseña local de PostgreSQL>'
   $jwtBytes = New-Object byte[] 32
   [System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
   $env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($jwtBytes)
   $env:PULSEPASS_JWT_ISSUER = 'pulsepass-api'
   $env:PULSEPASS_JWT_ACCESS_TOKEN_TTL = 'PT15M'
   ~~~

   Mantén la clave JWT solo en esa sesión local. Para TC-AUTH-010, usa PT1M durante el arranque E2E y vuelve a PT15M al terminar. No uses una clave de otro ambiente.
3. Arranca PulsePass una vez para que Flyway aplique las migraciones. Detén la aplicación y provisiona las fixtures:
   
   ~~~powershell
   psql -h localhost -U postgres -d pulsepass_e2e -X -v ON_ERROR_STOP=1 -f tests/e2e/fixtures/seed-e2e.sql
   ~~~

   El fixture verifica el nombre exacto de la base antes de escribir. Pide contraseñas STAFF/ADMIN en modo oculto y usa BCrypt por pgcrypto. Si pgcrypto no está disponible, instala/habilita la extensión solo en la base pulsepass_e2e con una cuenta local autorizada. No pegues hashes ni contraseñas en el repositorio.
4. Inicia la aplicación en el mismo proceso/terminal con las variables ya configuradas. Confirma que el inicio termina sin errores de PostgreSQL, Flyway, JWT o validación JPA y que escucha en el puerto 8080.
5. En Postman importa la colección y el ambiente. Selecciona **PulsePass - Local E2E (plantilla)**. Ajusta baseUrl si tu aplicación escucha en otra URL.
6. Define manualmente runId (por ejemplo, un identificador de sesión no sensible), userAUsername, userAEmail, userBUsername y userBEmail con valores únicos bajo example.test. Define userAPassword, userBPassword, staffPassword y adminPassword en los valores locales/secretos del ambiente. Usa contraseñas de prueba fuertes que no reutilices en otros servicios.
7. Escribe las mismas contraseñas STAFF/ADMIN cuando las pida el fixture SQL y cuando completes Postman. Los valores por defecto de identificadores privilegiados son pulsepass-e2e-staff y pulsepass-e2e-admin.
8. El ambiente deja en blanco tokens, códigos e IDs que se copian manualmente. No sincronices ni exportes valores actuales que contengan tokens o contraseñas. Si necesitas compartir el ambiente, exporta únicamente esta plantilla limpia.
9. Comprueba que el venue E2E-VENUE aparece en GET /api/venues/active y que Solar Beat aparece en GET /api/artists/active. Flyway V2 inserta el artista base; la fixture SQL prepara el venue.

### Sembrado y datos seguros

Las cuentas privilegiadas existen solo en esta base de pruebas. No existe un endpoint de registro con rol; no intentes agregarlo. USER_A y USER_B se crean por POST /api/users. Las direcciones usan example.test y no deben pertenecer a personas reales. La colección no contiene contraseñas reales, hashes ni JWT.

La fixture crea un venue de capacidad 500 y las cuentas STAFF/ADMIN. Guarda solo hashes BCrypt. La fecha de evento inicial es futura y amplia; revisa eventDate al ejecutar, pues la persona que prueba debe confirmar que siga siendo futura.

## Orden manual obligatorio

Cada solicitud se abre y se envía individualmente. Antes de pulsar Send, confirma el ambiente seleccionado, el actor, el token activo y los valores de variables visibles.

### A. Preflight y catálogos

1. Envía GET venues activos; verifica E2E-VENUE. Envía GET venue por código.
2. Envía GET artistas activos; copia el id de Solar Beat a artistId.
3. Envía GET artista por id y por nombre artístico.
4. Envía GET eventos publicados; esta lectura no debe mostrar datos de borradores preexistentes.

### B. Registro y autenticación

1. Registra USER_A y USER_B. La solicitud de A añade un campo role=ADMIN como intento de escalación: la respuesta puede mostrar los campos del UserResponse, pero la cuenta nunca puede obtener ADMIN. Confírmalo con GET /api/auth/me después del login.
2. Prueba registro duplicado y contraseña demasiado corta; compara 409 y 400.
3. Haz login de A por username, B por username, STAFF y ADMIN. Copia manualmente solo accessToken a la variable local del actor correspondiente.
4. Envía /api/auth/me para los cuatro actores y contrasta id, username, email y roles con el actor.
5. Comprueba password erróneo, identificador inexistente, /me sin token y token inválido. Los dos errores de login deben tener una respuesta genérica equivalente.

### C. Eventos y borradores

1. Con ADMIN crea un evento. Copia el eventCode retornado a draftEventCode y verifica status DRAFT.
2. Antes de publicar, intenta leerlo como visitante y USER; verifica 403 y que no se filtren nombre/descripción. ADMIN sí puede leerlo.
3. GET /api/events/published no debe incluir el borrador. USER no puede crear, asociar artistas ni publicar.
4. ADMIN asocia Solar Beat, comprueba que repetir la asociación da 409 y publica el evento.
5. Copia el mismo código a publishedEventCode. Lee el evento como visitante y comprueba PUBLISHED, el artista asociado, el listado publicado y la búsqueda por artista.
6. Revisa que el público no vea borradores por código, lista ni búsqueda de artista; STAFF/ADMIN sí pueden consultar según el contrato de seguridad.

### D. Entradas, propiedad y transiciones

1. Con USER_A compra tres entradas distintas del evento publicado. Copia los códigos a ticketCancelableCode, ticketCancelableCode2 y ticketUsableCode. Con USER_B compra otra y copia ticketBCode. No reutilices una entrada para cancelar y usar.
2. USER_A debe poder consultar/listar sus entradas; STAFF puede hacer consultas operativas. USER_B no puede leer/listar/cancelar una entrada de A.
3. Antes de provocar transiciones, prueba las denegaciones por propiedad y vuelve a leer cada ticket con su propietario para comprobar que no cambió.
4. Confirma que USER_A no puede comprar una entrada usando el correo de B y que no se creó una entrada para B.
5. STAFF lista las entradas pagadas del evento. USER recibe 403.
6. USER_A cancela ticketCancelableCode (200 y CANCELLED); repetir la cancelación debe dar 409.
7. STAFF marca ticketUsableCode como USED (200); repetir el uso debe dar 409. Confirma cada transición con una consulta GET posterior.
8. Usa los casos de error con datos válidos salvo el campo específicamente inválido. Un 500 inesperado es FAIL.

### E. JWT y regresión

- Token alterado: copia un token local vigente, cambia un carácter de su firma y envíalo como Bearer. No uses un decodificador web ni compartas el token.
- Token expirado: inicia la aplicación E2E con PULSEPASS_JWT_ACCESS_TOKEN_TTL=PT1M, captura un token, espera más de un minuto y vuelve a usarlo.
- Firma con otra clave e issuer distinto: la colección deja las variables para esas solicitudes. Se requiere un token preparado offline con el secreto de prueba exclusivamente E2E. Nunca uses claves de producción. Si no se dispone de un mecanismo local aprobado, marca esos casos BLOCKED; no simules el resultado.
- Cuenta inactiva/rol retirado: usa los fixtures SQL de este directorio en pulsepass_e2e, conserva el token emitido antes del cambio, verifica el rechazo y restaura la fixture antes de seguir.
- Para errores, verifica status exacto y ErrorResponse: timestamp, status, error, message, details. El status del body debe coincidir con HTTP; no debe haber stack trace, contraseña, hash, token ni secreto.

## Contratos de respuesta de este proyecto

- Registro: HTTP 201 y UserResponse: id, username, email, active, firstName, lastName, phone, city, birthDate.
- Login: HTTP 200 y AuthResponse: accessToken, tokenType=Bearer, expiresIn (900 segundos por defecto).
- /me: HTTP 200 y AuthenticatedUserResponse: id, username, email, roles.
- EventResponse incluye id, eventCode, name, description, category, status, eventDate, minimumAge, venueCode, venueName, artists.
- TicketResponse incluye id, ticketCode, type, price, status, purchaseDate, userEmail, eventCode, eventName.
- Tipos de evento: MUSIC, SPORTS, TECHNOLOGY, EDUCATION, CULTURE, ENTERTAINMENT. Tipos de entrada: GENERAL, VIP, BACKSTAGE, STUDENT.
- ErrorResponse: timestamp, status, error, message, details. En esta implementación, recurso ajeno se deniega con 403; regla de negocio/duplicado se expresa como 409; validación y JSON inválido como 400; no autenticado como 401; recurso ausente como 404.

El precio pertenece al servicio, no al cuerpo de compra. La compra válida produce PAID; cancelación válida produce CANCELLED; uso válido produce USED.

## Repetición segura y limitaciones

La opción más reproducible es borrar/restaurar únicamente la base pulsepass_e2e, volver a aplicar Flyway y ejecutar seed-e2e.sql. Antes de cualquier restauración verifica el nombre de base y confirma que no contenga datos de desarrollo compartidos. Alternativamente, usa runId nuevo y usuarios/códigos nuevos; venue y artista permanecen como fixtures compartidas dentro de la base E2E.

STAFF puede ver entradas de manera global en el MVP; el dominio no asocia personal con eventos. Esto es una limitación documentada, no un control por evento.

Solo una persona puede marcar PASS/FAIL/BLOCKED después de revisar el HTTP, el JSON y el estado persistido. Casos omitidos son NOT RUN; prerrequisitos ausentes son BLOCKED. Nunca conviertas un bloqueo en PASS.

## Para aprender el proceso

Empieza por [guia-aprendizaje-e2e.md](guia-aprendizaje-e2e.md): explica cómo leer un caso del PDF, encontrar su contrato en el código y reproducirlo en Postman. La lista de ejecución está en [checklist-e2e-manual.md](checklist-e2e-manual.md); el estado real de cada envío se anota en [registro-ejecucion-manual.md](registro-ejecucion-manual.md).

La colección incluye escenarios explícitos de compra de evento borrador, edad mínima y aforo agotado. Las pruebas siguen siendo manuales; NOT RUN significa que todavía debes enviarlas y revisar su resultado.
