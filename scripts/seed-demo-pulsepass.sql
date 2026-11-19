
\set ON_ERROR_STOP on

-- PulsePass: datos locales de demostracion.
-- Exclusivo para la base pulsepass.
-- No elimina ni sobrescribe registros existentes.

\prompt 'Contrasena para las cuentas USER: ' demo_user_password
\prompt 'Contrasena para la cuenta STAFF: ' demo_staff_password
\prompt 'Contrasena para la cuenta ADMIN: ' demo_admin_password
BEGIN;

DO $guard$
BEGIN
    IF current_database() <> 'pulsepass' THEN
        RAISE EXCEPTION 'Este script solo se puede ejecutar en pulsepass. Base actual: %', current_database();
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM flyway_schema_history
        WHERE version = '4' AND success = TRUE
    ) THEN
        RAISE EXCEPTION 'Flyway V4 no esta aplicada.';
    END IF;
END
$guard$;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. Sede
INSERT INTO venues (code, name, city, address, capacity, active)
VALUES (
    'VEN-SMR-01',
    'Marina Convention Center',
    'Santa Marta',
    'Santa Marta, Magdalena',
    5000,
    TRUE
)
ON CONFLICT (code) DO NOTHING;

-- 2. Comprobar que la sede existente coincide con el escenario
DO $venue_guard$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM venues
        WHERE code = 'VEN-SMR-01'
          AND name = 'Marina Convention Center'
          AND active = TRUE
    ) THEN
        RAISE EXCEPTION 'El codigo VEN-SMR-01 ya existe con otros datos.';
    END IF;
END
$venue_guard$;

-- 3. Evento de demostracion
INSERT INTO events (
    event_code, name, description, category, status,
    event_date, minimum_age, venue_id, streaming_url
)
SELECT
    'CMF-2026',
    'Caribbean Music Fest 2026',
    'Festival de musica caribena de demostracion para PulsePass.',
    'MUSIC',
    'PUBLISHED',
    TIMESTAMP '2026-12-19 18:00:00',
    18,
    v.id,
    NULL
FROM venues v
WHERE v.code = 'VEN-SMR-01'
ON CONFLICT (event_code) DO NOTHING;

DO $event_guard$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM events
        WHERE event_code = 'CMF-2026'
          AND name = 'Caribbean Music Fest 2026'
          AND status = 'PUBLISHED'
          AND venue_id = (SELECT id FROM venues WHERE code = 'VEN-SMR-01')
    ) THEN
        RAISE EXCEPTION 'CMF-2026 ya existe con datos incompatibles.';
    END IF;
END
$event_guard$;

-- 4. Asociar artistas iniciales
DO $artist_guard$
DECLARE
    artist_name TEXT;
BEGIN
    FOREACH artist_name IN ARRAY ARRAY[
        'Solar Beat',
        'Neon Waves',
        'Caribbean Sound'
    ]
    LOOP
        IF NOT EXISTS (
            SELECT 1 FROM artists
            WHERE stage_name = artist_name AND active = TRUE
        ) THEN
            RAISE EXCEPTION 'No se encontro el artista activo: %', artist_name;
        END IF;
    END LOOP;
END
$artist_guard$;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
CROSS JOIN artists a
WHERE e.event_code = 'CMF-2026'
  AND a.stage_name IN ('Solar Beat', 'Neon Waves', 'Caribbean Sound')
  AND a.active = TRUE
ON CONFLICT DO NOTHING;

-- 5. Cuentas de demostracion
INSERT INTO users (username, email, active)
VALUES
    ('demo-andrea', 'demo-andrea@example.test', TRUE),
    ('demo-carlos', 'demo-carlos@example.test', TRUE),
    ('demo-laura', 'demo-laura@example.test', TRUE),
    ('demo-miguel', 'demo-miguel@example.test', FALSE),
    ('demo-staff', 'demo-staff@example.test', TRUE),
    ('demo-admin', 'demo-admin@example.test', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Comprobar que no se reutilizaron correos de otras cuentas
DO $users_guard$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM (VALUES
            ('demo-andrea', 'demo-andrea@example.test'),
            ('demo-carlos', 'demo-carlos@example.test'),
            ('demo-laura', 'demo-laura@example.test'),
            ('demo-miguel', 'demo-miguel@example.test'),
            ('demo-staff', 'demo-staff@example.test'),
            ('demo-admin', 'demo-admin@example.test')
        ) AS expected(username, email)
        JOIN users u ON u.username = expected.username
        WHERE u.email <> expected.email
    ) THEN
        RAISE EXCEPTION 'Una cuenta demo ya existe con un correo diferente.';
    END IF;
END
$users_guard$;

-- 6. Perfiles
INSERT INTO user_profiles (
    first_name, last_name, phone, city, birth_date, user_id
)
SELECT
    p.first_name,
    p.last_name,
    NULL,
    'Santa Marta',
    p.birth_date,
    u.id
FROM (VALUES
    ('demo-andrea', 'Andrea', 'Demo', DATE '1995-05-10'),
    ('demo-carlos', 'Carlos', 'Demo', DATE '1990-03-15'),
    ('demo-laura', 'Laura', 'Demo', DATE '2009-06-10'),
    ('demo-miguel', 'Miguel', 'Demo', DATE '1990-08-20'),
    ('demo-staff', 'Staff', 'PulsePass', DATE '1990-01-01'),
    ('demo-admin', 'Admin', 'PulsePass', DATE '1990-01-01')
) AS p(username, first_name, last_name, birth_date)
JOIN users u ON u.username = p.username
ON CONFLICT (user_id) DO NOTHING;

-- 7. Credenciales BCrypt
INSERT INTO user_credentials (user_id, password_hash)
SELECT u.id, crypt(
    CASE
        WHEN u.username IN ('demo-andrea', 'demo-carlos', 'demo-laura', 'demo-miguel')
            THEN :'demo_user_password'
        WHEN u.username = 'demo-staff'
            THEN :'demo_staff_password'
        WHEN u.username = 'demo-admin'
            THEN :'demo_admin_password'
    END,
    gen_salt('bf', 12)
)
FROM users u
WHERE u.username IN (
    'demo-andrea', 'demo-carlos', 'demo-laura',
    'demo-miguel', 'demo-staff', 'demo-admin'
)
ON CONFLICT (user_id) DO NOTHING;

-- 8. Roles
INSERT INTO user_roles (user_id, role)
SELECT u.id, 'USER'
FROM users u
WHERE u.username IN ('demo-andrea', 'demo-carlos', 'demo-laura', 'demo-miguel')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'STAFF'
FROM users
WHERE username = 'demo-staff'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE username = 'demo-admin'
ON CONFLICT DO NOTHING;

-- 9. Tickets de demostracion
-- Precios tomados de TicketPricingPolicy.java
INSERT INTO tickets (
    ticket_code, type, price, status, purchase_date, user_id, event_id
)
SELECT
    'TKT-DEMO-ANDREA-VIP',
    'VIP',
    150000.00,
    'PAID',
    CURRENT_TIMESTAMP,
    u.id,
    e.id
FROM users u
CROSS JOIN events e
WHERE u.username = 'demo-andrea'
  AND e.event_code = 'CMF-2026'
ON CONFLICT (ticket_code) DO NOTHING;

INSERT INTO tickets (
    ticket_code, type, price, status, purchase_date, user_id, event_id
)
SELECT
    'TKT-DEMO-CARLOS-GENERAL',
    'GENERAL',
    100000.00,
    'PAID',
    CURRENT_TIMESTAMP,
    u.id,
    e.id
FROM users u
CROSS JOIN events e
WHERE u.username = 'demo-carlos'
  AND e.event_code = 'CMF-2026'
ON CONFLICT (ticket_code) DO NOTHING;

-- 10. Verificaciones finales antes del COMMIT
DO $final_guard$
BEGIN
    IF (SELECT COUNT(*) FROM event_artists ea
        JOIN events e ON e.id = ea.event_id
        WHERE e.event_code = 'CMF-2026') <> 3 THEN
        RAISE EXCEPTION 'El evento no tiene exactamente los tres artistas esperados.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM tickets
        WHERE ticket_code = 'TKT-DEMO-ANDREA-VIP'
          AND type = 'VIP' AND price = 150000
          AND status = 'PAID'
    ) THEN
        RAISE EXCEPTION 'Ticket VIP de Andrea no valido.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM tickets
        WHERE ticket_code = 'TKT-DEMO-CARLOS-GENERAL'
          AND type = 'GENERAL' AND price = 100000
          AND status = 'PAID'
    ) THEN
        RAISE EXCEPTION 'Ticket GENERAL de Carlos no valido.';
    END IF;
END
$final_guard$;

COMMIT;

\unset demo_user_password
\unset demo_staff_password
\unset demo_admin_password

\echo 'Datos de demostracion PulsePass aplicados correctamente.'
