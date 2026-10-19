\set ON_ERROR_STOP on
DO $guard$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'Fixture bloqueada: la base actual debe ser pulsepass_e2e.';
    END IF;
END
$guard$;
DELETE FROM user_roles WHERE user_id = (SELECT id FROM users WHERE email = 'pulsepass-e2e-staff@example.test') AND role = 'STAFF';
SELECT u.email, r.role FROM users u LEFT JOIN user_roles r ON r.user_id = u.id WHERE u.email = 'pulsepass-e2e-staff@example.test';
