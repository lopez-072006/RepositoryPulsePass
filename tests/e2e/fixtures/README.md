# Fixtures locales para PulsePass E2E

Todo lo que aparece aquí solo puede ejecutarse contra una base local llamada pulsepass_e2e. Los scripts verifican ese nombre antes de modificar filas. No cambies esa protección ni los uses contra otro ambiente.

## Requisitos

- Base PostgreSQL pulsepass_e2e creada.
- Aplicación iniciada una vez para que Flyway aplique V1-V4; luego detenida para ejecutar el seed.
- Usuario PostgreSQL local con permiso para crear pgcrypto.
- Las contraseñas STAFF/ADMIN se ingresan ocultas cuando el seed las solicita. Usa las mismas en los valores locales secretos de Postman; no las guardes en archivos.

## Seed

Desde la raíz del proyecto:

~~~powershell
psql -h localhost -U postgres -d pulsepass_e2e -X -v ON_ERROR_STOP=1 -f tests/e2e/fixtures/seed-e2e.sql
~~~

Crea/actualiza E2E-VENUE (capacidad 500), STAFF/ADMIN con BCrypt, sus roles y perfiles. Requiere que Solar Beat esté presente por la migración V2. No crea usuarios USER; regístralos por la API para probar el flujo real.

## Inactivar/re-activar usuario

Con la app detenida o cuando comprendas el efecto inmediato del siguiente request, ejecuta set-user-active.sql. El script pide el email y el booleano true/false; úsalo solo con el correo sintético de USER_A en la base E2E. Para probar un JWT anterior: login, copia el token local, inactiva A, consulta /api/auth/me con el token copiado (se espera 401), y luego reactiva A.

~~~powershell
psql -h localhost -U postgres -d pulsepass_e2e -X -v ON_ERROR_STOP=1 -f tests/e2e/fixtures/set-user-active.sql
~~~

## Retirar/restaurar rol STAFF

Para TC-SEC-017, ejecuta revoke-staff-role.sql, prueba el token STAFF previo y después ejecuta grant-staff-role.sql. Ambos scripts se limitan al correo fijo de la fixture STAFF y protegen la base por nombre. No dejes el fixture sin rol al cerrar.

~~~powershell
psql -h localhost -U postgres -d pulsepass_e2e -X -v ON_ERROR_STOP=1 -f tests/e2e/fixtures/revoke-staff-role.sql
psql -h localhost -U postgres -d pulsepass_e2e -X -v ON_ERROR_STOP=1 -f tests/e2e/fixtures/grant-staff-role.sql
~~~

Todos los scripts validan la base objetivo antes de modificar. Revisa salida de psql y confirma filas afectadas. Si pgcrypto no puede habilitarse localmente, solicita provisionar hashes BCrypt por un medio administrativo aprobado; no uses contraseñas en texto claro en SQL versionado.
