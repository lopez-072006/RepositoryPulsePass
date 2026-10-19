\set ON_ERROR_STOP on

DO $guard$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'Fixture bloqueada: la base actual debe ser pulsepass_e2e, no %', current_database();
    END IF;
END
$guard$;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

\prompt -s 'Contrasena local de prueba para STAFF (no la guardes en archivos): ' e2e_staff_password
\prompt -s 'Contrasena local de prueba para ADMIN (no la guardes en archivos): ' e2e_admin_password

INSERT INTO venues (code, name, city, address, capacity, active)
VALUES ('E2E-VENUE', 'PulsePass E2E Venue', 'Santa Marta', 'Direccion de fixture', 500, TRUE)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name, city = EXCLUDED.city, address = EXCLUDED.address,
    capacity = EXCLUDED.capacity, active = TRUE;

DO $artist_guard$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM artists WHERE stage_name = 'Solar Beat' AND active = TRUE) THEN
        RAISE EXCEPTION 'No se encontro Solar Beat activo; verifica Flyway V2 antes del seed.';
    END IF;
END
$artist_guard$;

INSERT INTO users (username, email, active)
VALUES ('pulsepass-e2e-staff', 'pulsepass-e2e-staff@example.test', TRUE)
ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email, active = TRUE;
SELECT id AS e2e_staff_id FROM users WHERE username = 'pulsepass-e2e-staff' \gset

INSERT INTO user_profiles (first_name, last_name, phone, city, birth_date, user_id)
VALUES ('Staff', 'E2E', NULL, 'Santa Marta', DATE '1990-01-01', :e2e_staff_id)
ON CONFLICT (user_id) DO UPDATE SET first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name,
    city = EXCLUDED.city, birth_date = EXCLUDED.birth_date;

INSERT INTO user_credentials (user_id, password_hash, password_changed_at)
VALUES (:e2e_staff_id, crypt(:'e2e_staff_password', gen_salt('bf', 12)), CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO UPDATE SET password_hash = EXCLUDED.password_hash,
    password_changed_at = CURRENT_TIMESTAMP;
DELETE FROM user_roles WHERE user_id = :e2e_staff_id;
INSERT INTO user_roles (user_id, role) VALUES (:e2e_staff_id, 'STAFF');

INSERT INTO users (username, email, active)
VALUES ('pulsepass-e2e-admin', 'pulsepass-e2e-admin@example.test', TRUE)
ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email, active = TRUE;
SELECT id AS e2e_admin_id FROM users WHERE username = 'pulsepass-e2e-admin' \gset

INSERT INTO user_profiles (first_name, last_name, phone, city, birth_date, user_id)
VALUES ('Admin', 'E2E', NULL, 'Santa Marta', DATE '1990-01-01', :e2e_admin_id)
ON CONFLICT (user_id) DO UPDATE SET first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name,
    city = EXCLUDED.city, birth_date = EXCLUDED.birth_date;

INSERT INTO user_credentials (user_id, password_hash, password_changed_at)
VALUES (:e2e_admin_id, crypt(:'e2e_admin_password', gen_salt('bf', 12)), CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO UPDATE SET password_hash = EXCLUDED.password_hash,
    password_changed_at = CURRENT_TIMESTAMP;
DELETE FROM user_roles WHERE user_id = :e2e_admin_id;
INSERT INTO user_roles (user_id, role) VALUES (:e2e_admin_id, 'ADMIN');

\unset e2e_staff_password
\unset e2e_admin_password
\echo 'Fixtures E2E aplicadas solo en pulsepass_e2e.'

INSERT INTO venues (code, name, city, address, capacity, active)
VALUES ('E2E-VENUE-CAPACITY', 'PulsePass E2E Venue Capacity One', 'Santa Marta', 'Direccion de fixture E2E', 1, TRUE)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name, city = EXCLUDED.city, address = EXCLUDED.address,
    capacity = EXCLUDED.capacity, active = TRUE;
