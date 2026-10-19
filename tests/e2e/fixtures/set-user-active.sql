\set ON_ERROR_STOP on
DO $guard$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'Fixture bloqueada: la base actual debe ser pulsepass_e2e.';
    END IF;
END
$guard$;
\prompt 'Correo sintetico del usuario USER de prueba: ' e2e_target_email
\prompt 'Estado activo (true o false): ' e2e_active
UPDATE users SET active = :'e2e_active'::boolean WHERE email = :'e2e_target_email';
SELECT email, active FROM users WHERE email = :'e2e_target_email';
\unset e2e_target_email
\unset e2e_active
