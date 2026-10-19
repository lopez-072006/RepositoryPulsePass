# Guía para aprender y repetir las pruebas E2E

Esta guía explica cómo convertir un requisito del PDF en una solicitud manual de Postman. El documento describe el procedimiento esperado; sus ejemplos no son evidencia de que la API ya haya pasado.

## 1. Qué significa E2E aquí

Una prueba end-to-end envía una petición HTTP a la aplicación real. La petición pasa por Spring Security, llega al controlador, atraviesa el servicio y consulta PostgreSQL. Por eso necesitas una API local arrancada y una base exclusiva `pulsepass_e2e`. La colección se ejecuta solicitud por solicitud con **Send**; no uses Runner, scripts ni Newman.

## 2. Cómo leer un requisito del PDF

Para cada escenario identifica cinco cosas:

1. **Actor**: visitante, USER_A, USER_B, STAFF o ADMIN.
2. **Acción**: método y ruta HTTP.
3. **Estado inicial**: por ejemplo, evento DRAFT o ticket PAID.
4. **Resultado esperado**: código HTTP y respuesta JSON.
5. **Efecto**: qué debe o no debe cambiar en el sistema.

Ejemplo: “USER_B no puede consultar el ticket de USER_A”. Actor USER_B; `GET /api/tickets/{ticketCode}`; el ticket pertenece a A y está PAID; HTTP 403; el contenido del ticket no se revela y su estado permanece PAID. Ese razonamiento aparece como `TC-SEC-009`.

## 3. Cómo una solicitud Postman se conecta al código

Al investigar un caso en el proyecto, sigue este camino:

1. Busca el `@GetMapping`, `@PostMapping` o `@PatchMapping` de la ruta en `src/main/java/.../controller`.
2. Lee el DTO de entrada para conocer los nombres y tipos exactos del JSON.
3. Revisa `SecurityConfig` para saber si requiere autenticación y qué roles admite.
4. Revisa `AuthorizationService` cuando el permiso depende de propiedad.
5. Sigue el método del servicio para identificar reglas de negocio y cambios persistidos.
6. En la colección configura el método, URL, token y JSON de esa misma operación.
7. Envía con **Send** y compara el status y el JSON con el requisito. Para reglas de estado, hace una segunda consulta GET y comprueba el estado final.

Así se aprende a probar el contrato existente y no a adivinarlo desde el nombre de la ruta.

## 4. Preparación paso a paso

Sigue la preparación de `README.md` en este directorio. En síntesis: configura PostgreSQL local en `pulsepass_e2e`, prepara la clave JWT solo en la sesión local, arranca la API para Flyway, deténla, ejecuta `fixtures/seed-e2e.sql` y vuelve a arrancar la API. Importa la colección y la plantilla de ambiente; selecciona el ambiente local.

Completa las variables locales de Postman con usuarios nuevos y únicos en `example.test`, contraseñas de prueba y el mismo `runId`. El fixture solicita por consola las contraseñas de ADMIN y STAFF. La cuenta `USER_YOUNG` se crea desde Postman con `youngBirthDate`; es sintética. Mantén `ageEventDate` como fecha futura y al menos 18 años posterior a `youngBirthDate` para que siga siendo futura y la persona siga por debajo de la edad mínima al llegar el evento.

## 5. Primer recorrido recomendado

Ejecuta en este orden para que las variables existan antes de usarlas:

1. Preflight: venues y artistas activos. Copia el id de Solar Beat a `artistId`.
2. Registro: USER_A, USER_B y el usuario sintético joven. Anota cualquier valor único que necesites.
3. Login: inicia sesión con cada actor y copia manualmente cada `accessToken` en la variable local correspondiente. No guardes tokens en el archivo exportado.
4. `/api/auth/me`: confirma que cada token representa el usuario correcto y que USER_A solo tiene USER.
5. Eventos: crea el borrador con ADMIN y guarda su código. Prueba la compra de borrador (409) antes de publicarlo. Luego asocia artista y publica el evento.
6. Edad: crea y publica el evento con edad mínima 18; inicia sesión como USER_YOUNG; intenta comprar y confirma HTTP 409, sin entrada nueva.
7. Aforo: el seed prepara un venue aparte con capacidad 1. Crea y publica allí un evento; USER_A compra la primera entrada (201) y USER_B intenta comprar la segunda (409). La lista pagada debe contener solo una entrada para ese evento.
8. Tickets normales: compra entradas del evento publicado para comprobar propiedad, cancelación y uso. Usa tickets distintos para cancelar y marcar USED.
9. Seguridad: realiza las solicitudes de rol, propiedad, token inválido, firma alterada e inactividad. Restaura las fixtures después de cambiar estado/rol.
10. Cierre: rellena el registro manual y deja cada caso como PASS, FAIL, BLOCKED o NOT RUN. Limpia los valores locales antes de compartir la plantilla.

La colección se ordena por temas; cuando un escenario depende de otro, prioriza la secuencia anterior y vuelve al folder indicado. No marques PASS basándote solo en que Postman recibió un 2xx: comprueba actor, JSON y efecto de negocio.

## 6. Qué revisar en cada respuesta

- **201 al registrar/comprar**: aparecen los campos del DTO esperado; no aparece password/hash. Guarda el código de evento o ticket que el paso siguiente necesita.
- **200 al consultar/transicionar**: comprueba identidad/rol o nuevo estado (`CANCELLED`/`USED`).
- **400**: valida que el error indique entrada inválida; no debe haber registro creado.
- **401**: falta autenticación válida; distingue token ausente, alterado o expirado.
- **403**: el actor inició sesión, pero no tiene permiso; verifica que no se filtren datos ni ocurra mutación.
- **404**: el recurso solicitado no existe.
- **409**: existe un conflicto de negocio: duplicado, transición repetida, evento no comprable, edad insuficiente o aforo agotado.
- **500**: marca FAIL y registra el caso; no lo interpretes como una denegación correcta.

En errores de API revisa `timestamp`, `status`, `error`, `message` y `details`. El `status` JSON debe coincidir con el status HTTP y no deben aparecer secretos ni trazas internas.

## 7. Cómo añadir tú un caso nuevo

Duplica una solicitud parecida de la colección; cambia el ID del caso, actor/token, ruta, body y status esperado. En la descripción anota precondición, resultado y efecto que revisarás. Si requiere datos nuevos, agrégalos a la plantilla y a las fixtures protegidas; actualiza también checklist, matriz y bitácora. Valida que el archivo JSON siga importando en Postman. Deja el resultado en NOT RUN hasta enviarlo manualmente y revisar la respuesta.

## 8. Interpretación de resultados

La colección y los documentos son material preparado para ejecutar. A la fecha de esta guía, las respuestas no se han ejecutado contra tu instancia local: el registro empieza en NOT RUN. Los casos que dependen de una clave JWT distinta o issuer diferente pueden quedar BLOCKED si no tienes un método local seguro para preparar ese token. Esto describe el estado de ejecución, no una aprobación del producto.
