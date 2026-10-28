# One-shot job: runs insert.sql (roles ROLE_ADMIN, ROLE_TEACHER, ROLE_USER and initial users)
# against Keycloak's database. Connection is set with PGHOST, PGDATABASE, PGUSER, PGPASSWORD.
# Build: docker build -t theleriz/lms-keycloak-init:latest keycloak/init
FROM postgres:17

COPY insert.sql /scripts/insert.sql

CMD ["psql", "-v", "ON_ERROR_STOP=1", "-f", "/scripts/insert.sql"]
