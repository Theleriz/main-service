-- Initial data for Keycloak's own database (NOT the main service database, NOT Liquibase).
-- Run by the keycloak-init container from docker-compose.yml after Keycloak has created
-- its schema and imported the lms realm (keycloak/realm-lms.json).
--
-- Creates realm roles (Spring Security GrantedAuthority):
--   ROLE_ADMIN, ROLE_TEACHER, ROLE_USER
-- and users of the lms realm:
--   admin   / admin123   -> ROLE_ADMIN
--   teacher / teacher123 -> ROLE_TEACHER
--   user    / user123    -> ROLE_USER
--
-- Passwords are stored the way Keycloak stores them: PBKDF2-SHA512, 210000 iterations,
-- 512-bit key, base64 hash and salt. Keycloak re-hashes them with its current default
-- algorithm on the first successful login.
--
-- The script is idempotent: running it again does not create duplicates.

BEGIN;

-- ---------------------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------------------
INSERT INTO keycloak_role (id, client_realm_constraint, client_role, description, name, realm_id)
SELECT r.id, realm.id, FALSE, r.description, r.name, realm.id
FROM (VALUES ('7a1f0e5c-2d7b-4a6e-9b1c-0a0000000001', 'ROLE_ADMIN', 'Administrator: manages users, roles and all content'),
             ('7a1f0e5c-2d7b-4a6e-9b1c-0a0000000002', 'ROLE_TEACHER', 'Teacher: manages courses, chapters and lessons'),
             ('7a1f0e5c-2d7b-4a6e-9b1c-0a0000000003', 'ROLE_USER', 'Student: reads courses, chapters and lessons'))
         AS r (id, name, description)
         CROSS JOIN realm
WHERE realm.name = 'lms'
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Users (email, first and last name are required by Keycloak's user profile,
-- otherwise login asks to complete the profile)
-- ---------------------------------------------------------------------------
INSERT INTO user_entity (id, email, email_constraint, email_verified, enabled, first_name, last_name,
                         realm_id, username, created_timestamp, not_before)
SELECT u.id, u.email, u.email, TRUE, TRUE, u.first_name, u.last_name,
       realm.id, u.username, (EXTRACT(EPOCH FROM now()) * 1000)::BIGINT, 0
FROM (VALUES ('5c2e8b1a-4f3d-4e7a-8c6b-0b0000000001', 'admin', 'admin@bitlab.kz', 'Admin', 'Bitlab'),
             ('5c2e8b1a-4f3d-4e7a-8c6b-0b0000000002', 'teacher', 'teacher@bitlab.kz', 'Teacher', 'Bitlab'),
             ('5c2e8b1a-4f3d-4e7a-8c6b-0b0000000003', 'user', 'user@bitlab.kz', 'User', 'Bitlab'))
         AS u (id, username, email, first_name, last_name)
         CROSS JOIN realm
WHERE realm.name = 'lms'
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Passwords
-- ---------------------------------------------------------------------------
INSERT INTO credential (id, type, user_id, created_date, secret_data, credential_data, priority)
SELECT c.id, 'password', c.user_id, (EXTRACT(EPOCH FROM now()) * 1000)::BIGINT,
       '{"value":"' || c.hash || '","salt":"' || c.salt || '","additionalParameters":{}}',
       '{"hashIterations":210000,"algorithm":"pbkdf2-sha512","additionalParameters":{}}',
       10
FROM (VALUES ('9d4b7c3e-6a2f-4b8d-9e5a-0c0000000001', '5c2e8b1a-4f3d-4e7a-8c6b-0b0000000001',
              'sCiswwNPBQV8IB968z3V+ookz9AytFyNTwHpggSXoh8nNJOFlqQMnji7IRUZmY32dPbu0OhBBc3Rs3yomJ/QxQ==',
              'eZBuDUNrDI9t3SpUheClLQ=='),
             ('9d4b7c3e-6a2f-4b8d-9e5a-0c0000000002', '5c2e8b1a-4f3d-4e7a-8c6b-0b0000000002',
              'UBDgG4htp7y9d50GY2Z/hEf9bf7H76v5ILPpas14lUrjteB0US7TwFppIEDJsC/x4Nr6OLnuITJszQb9WaJjQA==',
              'KNu3rhrc/6tRQijXQkIaKw=='),
             ('9d4b7c3e-6a2f-4b8d-9e5a-0c0000000003', '5c2e8b1a-4f3d-4e7a-8c6b-0b0000000003',
              'ofD2X84jQ0O+D7HweA36kEVFE30h2ft27ykzS350qHUQu7UptDiJAUjy4OCe9+2H6PaxQct1VvpUAobuty2+4A==',
              'y7N9p3ImJ7gLRRCUk0lH6A=='))
         AS c (id, user_id, hash, salt)
WHERE EXISTS (SELECT 1 FROM user_entity WHERE user_entity.id = c.user_id)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Role mappings: application role + Keycloak's default-roles-lms
-- (the same default roles Keycloak gives to every new user)
-- ---------------------------------------------------------------------------
INSERT INTO user_role_mapping (role_id, user_id)
SELECT m.role_id, m.user_id
FROM (VALUES ('7a1f0e5c-2d7b-4a6e-9b1c-0a0000000001', '5c2e8b1a-4f3d-4e7a-8c6b-0b0000000001'),
             ('7a1f0e5c-2d7b-4a6e-9b1c-0a0000000002', '5c2e8b1a-4f3d-4e7a-8c6b-0b0000000002'),
             ('7a1f0e5c-2d7b-4a6e-9b1c-0a0000000003', '5c2e8b1a-4f3d-4e7a-8c6b-0b0000000003'))
         AS m (role_id, user_id)
WHERE EXISTS (SELECT 1 FROM keycloak_role WHERE keycloak_role.id = m.role_id)
  AND EXISTS (SELECT 1 FROM user_entity WHERE user_entity.id = m.user_id)
ON CONFLICT DO NOTHING;

INSERT INTO user_role_mapping (role_id, user_id)
SELECT default_role.id, u.id
FROM user_entity u
         JOIN realm ON realm.id = u.realm_id
         JOIN keycloak_role default_role
              ON default_role.realm_id = realm.id AND default_role.name = 'default-roles-' || realm.name
WHERE realm.name = 'lms'
  AND u.id IN ('5c2e8b1a-4f3d-4e7a-8c6b-0b0000000001',
               '5c2e8b1a-4f3d-4e7a-8c6b-0b0000000002',
               '5c2e8b1a-4f3d-4e7a-8c6b-0b0000000003')
ON CONFLICT DO NOTHING;

COMMIT;
