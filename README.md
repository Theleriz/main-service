# BITLAB ACADEMY LMS — Main Service

Основной сервис LMS BITLAB ACADEMY: управление курсами, главами и уроками.
Курс состоит из упорядоченных глав, глава — из упорядоченных уроков; содержимое урока хранится как HTML (под будущий редактор TinyMCE / CKEditor).

## Стек

Java 21 · Spring Boot 4.0.5 (Spring Web MVC, Spring Data JPA, Validation, Spring Security + OAuth2 Resource Server) · Keycloak 26 · PostgreSQL 17 · Liquibase · MapStruct · Lombok · springdoc-openapi (Swagger) · SLF4J/Logback · JUnit 5 + Mockito · Docker / Docker Compose

## Запуск одной командой

```bash
docker compose up -d
```

Поднимаются:

| Контейнер | Что это | Порт |
|---|---|---|
| `lms-postgres` | БД основного сервиса | 5432 |
| `lms-keycloak-postgres` | Собственная БД Keycloak (только во внутренней сети `keycloak-db`, основной сервис её не видит) | — |
| `lms-keycloak` | Keycloak 26, realm `lms` импортируется из `keycloak/realm-lms.json` | 8180 |
| `lms-keycloak-init` | Одноразовый контейнер: выполняет `keycloak/insert.sql` в БД Keycloak и завершается | — |
| `lms-main-service` | Основной сервис, стартует после `lms-keycloak-init` | 8080 |

Liquibase основного сервиса создаёт таблицы и загружает стартовые данные: курсы Java Developer, Python Developer и SQL and PostgreSQL Developer с главами, лекциями и практиками.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Docker Hub: https://hub.docker.com/r/theleriz/main-service (`theleriz/main-service:2.0` — с Keycloak и Spring Security, `latest`; `1.0` — без безопасности)

Образ стягивается из Docker Hub, собирать проект локально не нужно.

Остановить и удалить данные: `docker compose down -v`.

## Keycloak и безопасность

- Админка Keycloak: http://localhost:8180 (`admin` / `admin`, realm `master`), рабочий realm — `lms`.
- Realm `lms`: JWT access token живёт 5 минут, refresh token — 168 часов (SSO Session Idle / Max = 604800 с).
- Клиент `lms-main-service` (confidential, secret `lms-main-service-secret`): password и refresh_token grant для логина, service account с ролями `manage-users`, `view-users`, `query-users`, `view-realm` для регистрации и обновления пользователей через Admin REST API.

Роли и пользователи создаются SQL-скриптом [`keycloak/insert.sql`](keycloak/insert.sql) прямо в БД Keycloak (Liquibase основного сервиса к ней доступа не имеет). Скрипт идемпотентный.

| Пользователь | Пароль | Роль |
|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` |
| `teacher` | `teacher123` | `ROLE_TEACHER` |
| `user` | `user123` | `ROLE_USER` |

Spring Security проверяет подпись JWT по ключам Keycloak, а `KeycloakJwtAuthenticationConverter` превращает токен в `AuthUser` (наследник `org.springframework.security.core.userdetails.User`): роли из `realm_access.roles` становятся `GrantedAuthority`.

Права доступа:

| Что | Кто |
|---|---|
| `/api/v1/auth/**`, Swagger | все, без токена |
| `GET` курсов, глав, уроков | любой аутентифицированный |
| `POST` / `PUT` / `DELETE` курсов, глав, уроков | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST /api/v1/users`, `PUT /api/v1/users/{id}` | только `ROLE_ADMIN` |
| `GET` / `PUT /api/v1/users/me` | любой аутентифицированный; менять роли может только `ROLE_ADMIN` |

Без токена — 401, без нужной роли — 403, в том же формате `ErrorResponse`.

Пример:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
```

В Swagger UI нажмите **Authorize** и вставьте `accessToken` из ответа.

## API

| Метод | Путь | Описание |
|---|---|---|
| POST | `/api/v1/auth/login` | Логин → `accessToken`, `refreshToken` |
| POST | `/api/v1/auth/refresh` | Новый `accessToken` по `refreshToken` |
| POST | `/api/v1/users` | Регистрация пользователя с ролями (`ROLE_ADMIN`) → 201 |
| GET | `/api/v1/users/me` | Текущий пользователь |
| PUT | `/api/v1/users/me` | Обновить свои данные и пароль (роли — только админ) |
| PUT | `/api/v1/users/{id}` | Обновить любого пользователя, включая роли (`ROLE_ADMIN`) |
| GET | `/api/v1/courses` | Все курсы |
| GET | `/api/v1/courses/{id}` | Курс по id |
| GET | `/api/v1/courses/{id}/chapters` | Главы курса (по `order`) |
| POST | `/api/v1/courses` | Создать курс → 201 |
| PUT | `/api/v1/courses/{id}` | Обновить курс → 200 |
| DELETE | `/api/v1/courses/{id}` | Удалить курс с главами и уроками → 204 |
| GET | `/api/v1/chapters` | Все главы |
| GET | `/api/v1/chapters/{id}` | Глава по id |
| GET | `/api/v1/chapters/{id}/lessons` | Уроки главы (по `order`) |
| POST | `/api/v1/chapters` | Создать главу → 201 |
| PUT | `/api/v1/chapters/{id}` | Обновить главу → 200 |
| DELETE | `/api/v1/chapters/{id}` | Удалить главу с уроками → 204 |
| GET | `/api/v1/lessons` | Все уроки |
| GET | `/api/v1/lessons/{id}` | Урок по id |
| POST | `/api/v1/lessons` | Создать урок → 201 |
| PUT | `/api/v1/lessons/{id}` | Обновить урок → 200 |
| DELETE | `/api/v1/lessons/{id}` | Удалить урок → 204 |

Все ошибки возвращаются в едином формате `ErrorResponse` без stack trace:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Course with id 999 not found",
  "path": "/api/v1/courses/999",
  "timestamp": "2026-10-13T10:15:30"
}
```

404 — объект не найден, 400 — ошибка валидации (с `fieldErrors`), кривой JSON или неверный тип id, 401 — нет/неверный токен или неверный логин/пароль, 403 — нет прав, 409 — username или email уже заняты, 500 — непредвиденная ошибка.

## Локальная разработка

```bash
docker compose up -d postgres keycloak-init
./mvnw spring-boot:run
```

Параметры БД задаются переменными окружения `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (по умолчанию `localhost:5432/lms_main`, `postgres/postgres`).
Keycloak — `KEYCLOAK_URL` (по умолчанию `http://localhost:8180`), `KEYCLOAK_ISSUER_URL`, `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET`.

## Тесты

```bash
./mvnw test
```

Тесты сервисов (Mockito вместо Repository и KeycloakClient) и контроллеров (`@WebMvcTest` с настоящим `SecurityConfig`) не требуют запущенной БД и Keycloak.
Полный контекстный тест `MainServiceApplicationTests` включается переменной `INTEGRATION_TESTS=true` при запущенном PostgreSQL.

## Структура

```
src/main/java/kz/bitlab/mainservice/
├── config/        OpenApiConfig, SecurityConfig
├── controller/    Course/Chapter/LessonController, AuthController, UserController
├── dto/           *Dto, *CreateRequest, *UpdateRequest, LoginRequest, RefreshTokenRequest, TokenResponse
├── entity/        Course, Chapter, Lesson
├── exception/     GlobalExceptionHandler, ErrorResponse, NotFoundException, UnauthorizedException, ConflictException
├── keycloak/      KeycloakClient (token endpoint + Admin REST API), KeycloakProperties, dto/
├── mapper/        Course/Chapter/LessonMapper, AuthMapper, UserMapper (MapStruct)
├── repository/    CourseRepository, ChapterRepository, LessonRepository
├── security/      Role, AuthUser, KeycloakJwtAuthenticationConverter
└── service/       интерфейсы + impl/
src/main/resources/db/changelog/
├── db.changelog-master.yaml
└── changes/       001-create-tables.yaml, 002-insert-data.yaml, data/*.sql
keycloak/
├── realm-lms.json  realm lms, время жизни токенов, клиент lms-main-service
└── insert.sql      роли ROLE_ADMIN / ROLE_TEACHER / ROLE_USER и пользователи в БД Keycloak
```
