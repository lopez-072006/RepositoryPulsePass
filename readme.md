# Integrantes

- Sofia Lopez -- 2024214011
- Cristian Garaviz -- 2023214057

# PulsePass

Sistema de persistencia para una plataforma de gestión de eventos, artistas, lugares, usuarios y entradas.

El proyecto implementa la capa de persistencia de **PulsePass** utilizando Spring Boot, JPA/Hibernate, PostgreSQL y Flyway. También incluye consultas mediante Spring Data JPA, consultas JPQL y pruebas de integración utilizando PostgreSQL mediante Testcontainers.

> **Estado actual:** capa de persistencia implementada y validada mediante pruebas de integración.

---

## 1. Descripción del proyecto

PulsePass es una plataforma orientada a la gestión de eventos.

El sistema permite modelar y persistir información relacionada con:

- Lugares donde se realizan eventos.
- Eventos.
- Artistas.
- Usuarios.
- Perfiles de usuario.
- Entradas.
- Relación entre eventos y artistas.

En esta etapa del proyecto se trabaja principalmente sobre la **capa de persistencia**, por lo que el alcance se concentra en:

- Modelo de dominio.
- Mapeo JPA.
- Relaciones entre entidades.
- Migraciones de base de datos.
- Repositorios.
- Consultas mediante Spring Data JPA.
- Consultas JPQL.
- Pruebas de integración.
- Validación de restricciones de PostgreSQL.

---

# 2. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje de programación |
| Spring Boot 4 | Framework principal |
| Spring Data JPA | Persistencia y repositorios |
| Hibernate | Implementación JPA |
| PostgreSQL | Base de datos |
| Flyway | Migraciones de base de datos |
| Testcontainers | Base de datos PostgreSQL para pruebas |
| JUnit 5 | Pruebas |
| AssertJ | Assertions de las pruebas |
| Maven | Gestión y construcción del proyecto |
| Docker | Ejecución de contenedores |

---

# 3. Arquitectura del proyecto

La estructura principal del proyecto se encuentra organizada de la siguiente manera:

```text
src
├── main
│   ├── java
│   │   └── com
│       └── Unimag
│           └── PulsePass
│               ├── domain
│               │   ├── Artist.java
│               │   ├── Event.java
│               │   ├── Ticket.java
│               │   ├── User.java
│               │   ├── UserProfile.java
│               │   ├── Venue.java
│               │   └── enums
│               │       ├── EventCategory.java
│               │       ├── EventStatus.java
│               │       ├── TicketStatus.java
│               │       └── TicketType.java
│               │
│               ├── repository
│               │   ├── ArtistRepository.java
│               │   ├── EventRepository.java
│               │   ├── TicketRepository.java
│               │   ├── UserProfileRepository.java
│               │   ├── UserRepository.java
│               │   └── VenueRepository.java
│               │
│               └── PulsePassApplication.java
│
├── main
│   └── resources
│       ├── application.yml
│       └── db
│           └── migration
│               ├── V1__create_schema.sql
│               ├── V2__create_base_tables.sql
│               └── V3__add_streaming_url.sql
│
└── test
    └── java
        └── com
            └── Unimag
                └── PulsePass
                    ├── PersistenceIntegrationTest.java
                    ├── PulsePassApplicationTests.java
                    └── TestcontainersConfiguration.java
```

> La estructura puede variar ligeramente dependiendo de la organización actual del proyecto.

---

# 4. Modelo de datos

El modelo de datos está compuesto por las siguientes entidades principales:

```text
Venue
  │
  │ 1:N
  ▼
Event
  │
  ├───────────────┐
  │               │
  │ N:M           │ 1:N
  ▼               ▼
Artist           Ticket
                  │
                  │ N:1
                  ▼
                 User
                  │
                  │ 1:1
                  ▼
             UserProfile
```

Además, la relación N:M entre `Event` y `Artist` se implementa mediante la tabla intermedia:

```text
event_artists
```

---

# 5. Entidades

## 5.1 Venue

Representa el lugar físico donde se realiza un evento.

Campos principales:

- `id`
- `code`
- `name`
- `city`
- `address`
- `capacity`
- `active`

Restricciones importantes:

- `code` es único.
- `capacity` debe ser mayor que cero.

---

## 5.2 Event

Representa un evento dentro de PulsePass.

Campos principales:

- `id`
- `eventCode`
- `name`
- `description`
- `category`
- `status`
- `eventDate`
- `minimumAge`
- `streamingUrl`
- `venue`

Un evento pertenece a un `Venue`.

También puede tener múltiples artistas asociados.

---

## 5.3 Artist

Representa un artista que participa en eventos.

Campos principales:

- `id`
- `stageName`
- `country`
- `genre`
- `active`

`stageName` es único dentro de la base de datos.

---

## 5.4 User

Representa un usuario de PulsePass.

Campos principales:

- `id`
- `username`
- `email`
- `active`

Un usuario puede tener un perfil y puede tener múltiples entradas.

---

## 5.5 UserProfile

Contiene información adicional de un usuario.

Campos principales:

- `id`
- `firstName`
- `lastName`
- `phone`
- `city`
- `birthDate`
- `user`

La relación con `User` es de tipo:

```text
User 1 : 1 UserProfile
```

La columna `user_id` es única en la base de datos para garantizar esta relación.

---

## 5.6 Ticket

Representa una entrada adquirida para un evento.

Campos principales:

- `id`
- `ticketCode`
- `type`
- `price`
- `status`
- `purchaseDate`
- `user`
- `event`

Las entradas pertenecen a un usuario y a un evento.

`ticketCode` es único.

---

# 6. Relaciones entre entidades

## Venue → Event

Relación:

```text
Venue 1 : N Event
```

Un lugar puede tener múltiples eventos.

Cada evento pertenece a un único lugar.

En JPA:

```java
@OneToMany(mappedBy = "venue")
private List<Event> events;
```

y en `Event`:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "venue_id", nullable = false)
private Venue venue;
```

---

## Event ↔ Artist

Relación:

```text
Event N : M Artist
```

Un evento puede tener múltiples artistas y un artista puede participar en múltiples eventos.

La relación se implementa mediante:

```text
event_artists
```

con una clave primaria compuesta:

```text
(event_id, artist_id)
```

En `Event`:

```java
@ManyToMany
@JoinTable(
    name = "event_artists",
    joinColumns = @JoinColumn(name = "event_id"),
    inverseJoinColumns = @JoinColumn(name = "artist_id")
)
private Set<Artist> artists;
```

---

## User → UserProfile

Relación:

```text
User 1 : 1 UserProfile
```

La tabla `user_profiles` contiene:

```text
user_id UNIQUE
```

Esto evita que un mismo usuario tenga más de un perfil.

---

## User → Ticket

Relación:

```text
User 1 : N Ticket
```

Un usuario puede tener múltiples entradas.

---

## Event → Ticket

Relación:

```text
Event 1 : N Ticket
```

Un evento puede tener múltiples entradas.

---

# 7. Base de datos

La aplicación utiliza PostgreSQL como sistema gestor de base de datos.

Las tablas principales son:

```text
venues
events
artists
event_artists
users
user_profiles
tickets
```

---

# 8. Migraciones Flyway

Las migraciones de la base de datos se encuentran en:

```text
src/main/resources/db/migration/
```

## V1 - Creación del esquema

Archivo:

```text
V1__create_schema.sql
```

Esta migración crea las tablas principales:

- `venues`
- `artists`
- `users`
- `events`
- `user_profiles`
- `tickets`
- `event_artists`

También crea:

- Primary Keys.
- Foreign Keys.
- Restricciones `UNIQUE`.
- Restricciones `CHECK`.
- Índices.

Entre las restricciones importantes se encuentran:

```sql
UNIQUE
```

para códigos y datos que no deben repetirse.

También:

```sql
CHECK (capacity > 0)
```

para validar la capacidad de los lugares.

---

## V2 - Datos iniciales de artistas

Archivo:

```text
V2__create_base_tables.sql
```

Esta migración agrega artistas iniciales:

```text
Solar Beat
Neon Waves
Caribbean Sound
Ocean Drive
Digital Pulse
```

Estos registros son utilizados también durante las pruebas de integración.

---

## V3 - Streaming URL

Archivo:

```text
V3__add_streaming_url.sql
```

Agrega la columna:

```text
streaming_url
```

a la tabla `events`.

La columna permite almacenar una URL relacionada con la transmisión del evento.

---

# 9. Configuración de Hibernate

El proyecto utiliza:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Esto significa que Hibernate **no crea ni modifica automáticamente las tablas**.

En cambio:

1. Flyway ejecuta las migraciones.
2. PostgreSQL contiene el esquema.
3. Hibernate valida que las entidades JPA sean compatibles con ese esquema.

Esta estrategia evita que Hibernate modifique accidentalmente la estructura de la base de datos.

---

# 10. Repositorios

Los repositorios utilizan:

```java
JpaRepository
```

Esto proporciona operaciones básicas como:

```text
save()
findById()
findAll()
delete()
count()
```

Además, se implementaron consultas específicas utilizando:

- Spring Data Query Methods.
- JPQL mediante `@Query`.

---

# 11. VenueRepository

El repositorio permite buscar lugares mediante su código.

Consulta:

```java
Optional<Venue> findByCode(String code);
```

Spring Data JPA genera automáticamente la consulta correspondiente.

---

# 12. ArtistRepository

El repositorio permite buscar artistas por nombre artístico.

```java
Optional<Artist> findByStageName(String stageName);
```

Este método también se utiliza durante las pruebas para recuperar artistas creados mediante Flyway.

---

# 13. UserRepository

Permite buscar usuarios mediante su correo electrónico ignorando mayúsculas y minúsculas:

```java
Optional<User> findByEmailIgnoreCase(String email);
```

Por ejemplo:

```text
user@pulsepass.com
USER@PULSEPASS.COM
User@PulsePass.com
```

se consideran equivalentes para la búsqueda.

---

# 14. EventRepository

`EventRepository` contiene consultas mediante Query Methods y JPQL.

## Buscar evento por código

```java
Optional<Event> findByEventCode(String eventCode);
```

---

## Buscar eventos por estado ordenados por fecha

```java
List<Event> findByStatusOrderByEventDateAsc(EventStatus status);
```

Permite obtener eventos de un estado determinado y ordenarlos cronológicamente.

Ejemplo:

```java
eventRepository.findByStatusOrderByEventDateAsc(
    EventStatus.PUBLISHED
);
```

---

## Buscar eventos por venue

```java
List<Event> findByVenueCode(String venueCode);
```

Spring Data navega desde:

```text
Event → Venue → code
```

---

# 15. Consultas JPQL

Para consultas más complejas se utiliza:

```java
@Query
```

con JPQL.

---

## Eventos por artista

```java
@Query("""
    SELECT DISTINCT e
    FROM Event e
    JOIN e.artists a
    WHERE LOWER(a.stageName) = LOWER(:stageName)
""")
List<Event> findEventsByArtistStageName(
        @Param("stageName") String stageName
);
```

Esta consulta:

1. Parte de `Event`.
2. Hace `JOIN` con los artistas.
3. Compara el nombre artístico.
4. Utiliza `LOWER()` para ignorar mayúsculas/minúsculas.
5. Utiliza `DISTINCT` para evitar eventos duplicados.

---

# 16. Eventos por ciudad y artista

```java
@Query("""
    SELECT DISTINCT e
    FROM Event e
    JOIN e.venue v
    JOIN e.artists a
    WHERE v.city = :city
      AND LOWER(a.stageName) = LOWER(:stageName)
""")
List<Event> findByCityAndArtist(
        @Param("city") String city,
        @Param("stageName") String stageName
);
```

La consulta combina información de:

```text
Event
   ↓
Venue
   ↓
Artist
```

y filtra por:

- ciudad;
- artista.

---

# 17. Eventos recomendados

La consulta de eventos recomendados utiliza:

- estado del evento;
- fecha posterior a una fecha determinada;
- ciudad;
- coincidencia parcial con el nombre del artista;
- orden cronológico;
- `DISTINCT`.

```java
@Query("""
    SELECT DISTINCT e
    FROM Event e
    JOIN e.venue v
    JOIN e.artists a
    WHERE e.status = :status
      AND e.eventDate > :afterDate
      AND v.city = :city
      AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistName, '%'))
    ORDER BY e.eventDate ASC
""")
List<Event> findRecommendedEvents(
        @Param("status") EventStatus status,
        @Param("afterDate") LocalDateTime afterDate,
        @Param("city") String city,
        @Param("artistName") String artistName
);
```

Por ejemplo, una búsqueda de:

```text
artistName = "ocean"
```

puede encontrar:

```text
Ocean Drive
```

porque se utiliza:

```sql
LIKE
```

con coincidencia parcial.

---

# 18. TicketRepository

El repositorio contiene las siguientes consultas.

## Buscar ticket por código

```java
Optional<Ticket> findByTicketCode(String ticketCode);
```

---

## Buscar tickets de un usuario por estado

```java
List<Ticket> findByUserEmailIgnoreCaseAndStatus(
        String email,
        TicketStatus status
);
```

Permite buscar, por ejemplo, los tickets pagados de un usuario.

---

## Buscar tickets de un evento por estado

```java
List<Ticket> findByEventEventCodeAndStatus(
        String eventCode,
        TicketStatus status
);
```

Permite obtener los tickets de un evento filtrados por estado.

---

## Contar tickets pagados

```java
@Query("""
    SELECT COUNT(t) FROM Ticket t
    WHERE t.event.eventCode = :eventCode
      AND t.status = :status
""")
long countPaidTicketsByEventCode(
        @Param("eventCode") String eventCode,
        @Param("status") TicketStatus status
);
```

Esta consulta utiliza:

```text
COUNT
```

para obtener la cantidad de tickets que cumplen las condiciones.

---

# 19. Query Methods vs JPQL

En el proyecto se utiliza una estrategia combinada.

## Query Methods

Se utilizan cuando la consulta puede expresarse fácilmente mediante el nombre del método.

Ejemplo:

```java
findByEventCode(String eventCode)
```

o:

```java
findByStatusOrderByEventDateAsc(EventStatus status)
```

Ventajas:

- Menos código.
- Fácil de leer.
- Spring Data genera la consulta automáticamente.

---

## JPQL

Se utiliza cuando la consulta necesita relaciones o lógica más compleja.

Ejemplo:

```text
Event
  ↓
Venue
  ↓
Artist
```

o cuando se requiere:

- `JOIN`;
- `DISTINCT`;
- `COUNT`;
- `LIKE`;
- `LOWER`;
- múltiples filtros;
- ordenamiento complejo.

---

# 20. Pruebas de integración

Las pruebas principales se encuentran en:

```text
PersistenceIntegrationTest.java
```

Las pruebas utilizan:

```java
@SpringBootTest
```

y:

```java
@Testcontainers
```

La base de datos utilizada durante las pruebas es PostgreSQL real ejecutado dentro de un contenedor.

---

# 21. Testcontainers

La configuración utiliza:

```java
PostgreSQLContainer
```

con PostgreSQL:

```text
postgres:18-alpine
```

Esto permite que las pruebas se ejecuten sobre una base de datos PostgreSQL real sin depender de una instalación local específica.

El contenedor se crea durante la ejecución de las pruebas.

---

# 22. Pruebas realizadas

Actualmente el proyecto cuenta con:

```text
17 pruebas
```

Resultado actual:

```text
Tests run: 17
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Las pruebas cubren diferentes aspectos de la capa de persistencia.

### Flyway

Se verifica que las migraciones se ejecuten correctamente.

### Venue

Se verifica:

- creación de un venue;
- búsqueda por código;
- restricción `UNIQUE`;
- restricción `CHECK` de capacidad.

### Event

Se verifica:

- búsqueda por `eventCode`;
- eventos publicados;
- orden por fecha;
- búsqueda por venue;
- búsqueda por artista;
- búsqueda por ciudad y artista;
- búsqueda de eventos recomendados.

### User

Se verifica:

- búsqueda por email ignorando mayúsculas/minúsculas;
- relación con `UserProfile`.

### Ticket

Se verifica:

- búsqueda por usuario y estado;
- búsqueda por evento y estado;
- conteo de tickets pagados;
- persistencia dentro del escenario completo.

---

# 23. Restricciones de base de datos

La base de datos contiene restricciones importantes.

## UNIQUE

Ejemplos:

```text
venues.code
artists.stage_name
users.username
users.email
events.event_code
user_profiles.user_id
tickets.ticket_code
```

Estas restricciones impiden registros duplicados.

---

## CHECK

La capacidad de un venue debe ser mayor que cero:

```sql
CHECK (capacity > 0)
```

También existen restricciones `CHECK` para los valores permitidos de:

```text
EventCategory
EventStatus
TicketStatus
TicketType
```

---

# 24. Configuración

La aplicación utiliza `application.yml`.

La configuración principal incluye:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/deepblue}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
    locations: classpath:db/migration
```

Las variables de entorno permiten cambiar la configuración de la base de datos sin modificar el código fuente.

Variables disponibles:

```text
DB_URL
DB_USER
DB_PASSWORD
```

---

# 25. Requisitos previos

Para ejecutar el proyecto se necesita:

- Java 21.
- Docker Desktop.
- Maven Wrapper incluido en el proyecto.

Se recomienda utilizar Docker Desktop para permitir que Testcontainers ejecute PostgreSQL durante las pruebas.

---

# 26. Ejecutar el proyecto

Clonar el repositorio:

```bash
git clone <URL_DEL_REPOSITORIO>
```

Entrar al proyecto:

```bash
cd RepositoryPulsePass
```

---

# 27. Ejecutar las pruebas

En Windows:

```cmd
.\mvnw.cmd clean test
```

En Linux/macOS:

```bash
./mvnw clean test
```

El resultado esperado es:

```text
BUILD SUCCESS
```

---

# 28. Ejecutar la aplicación

Con Maven Wrapper:

### Windows

```cmd
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

---

# 29. Migraciones

Flyway ejecuta automáticamente las migraciones ubicadas en:

```text
src/main/resources/db/migration
```

Las migraciones se ejecutan en orden:

```text
V1
 ↓
V2
 ↓
V3
```

Flyway mantiene el historial de las migraciones en:

```text
flyway_schema_history
```

---

# 30. Alcance actual

El alcance actual del proyecto está centrado en la persistencia.

Incluye:

- Modelo de dominio.
- Entidades JPA.
- Relaciones entre entidades.
- PostgreSQL.
- Flyway.
- Spring Data JPA.
- Query Methods.
- JPQL.
- Testcontainers.
- Pruebas de integración.

---

# 31. Fuera de alcance

De acuerdo con el alcance definido para esta etapa, no se implementan:

- API REST.
- Controladores.
- Servicios de negocio.
- Autenticación.
- Autorización.
- Procesamiento de pagos.
- Frontend.
- Aplicación móvil.
- Integraciones externas.

---

# 32. Definition of Done

La capa de persistencia se considera completada cuando se cumplen los siguientes puntos:

- [x] Modelo de entidades implementado.
- [x] Relaciones JPA implementadas.
- [x] Migraciones Flyway implementadas.
- [x] PostgreSQL configurado.
- [x] Hibernate configurado con `ddl-auto: validate`.
- [x] Repositorios basados en `JpaRepository`.
- [x] Query Methods implementados.
- [x] Consultas JPQL implementadas.
- [x] Consultas N:M implementadas.
- [x] `COUNT` implementado.
- [x] Restricciones de base de datos implementadas.
- [x] Restricciones probadas mediante integración.
- [x] Testcontainers configurado.
- [x] Pruebas de integración implementadas.
- [x] 17 pruebas ejecutadas correctamente.
- [x] `mvn clean test` finaliza con `BUILD SUCCESS`.
- [x] README documenta el modelo, migraciones, consultas y pruebas.

---

# 33. Resultado

Actualmente la capa de persistencia de PulsePass se encuentra implementada y validada mediante pruebas de integración sobre PostgreSQL utilizando Testcontainers.

Resultado de la ejecución:

```text
Tests run: 17
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Esto confirma que las funcionalidades de persistencia cubiertas por las pruebas se ejecutan correctamente sobre PostgreSQL.

---

# 34. Autores

Proyecto desarrollado por Cristian Garaviz y Sofia Lopez como parte del proceso académico de Ingeniería de Sistemas.

**PulsePass**