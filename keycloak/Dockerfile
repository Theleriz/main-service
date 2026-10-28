# Keycloak with the lms realm built in (realm settings, token lifetimes, lms-main-service client).
# Build: docker build -t theleriz/lms-keycloak:latest keycloak
FROM quay.io/keycloak/keycloak:26.4

# Imported on the first start, skipped if the realm already exists
COPY realm-lms.json /opt/keycloak/data/import/realm-lms.json

CMD ["start-dev", "--import-realm"]
