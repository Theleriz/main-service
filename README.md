# BITLAB ACADEMY LMS — Main Service

Основной сервис LMS BITLAB ACADEMY: управление курсами, главами, уроками и файлами уроков.
Курс состоит из упорядоченных глав, глава — из упорядоченных уроков; содержимое урока хранится как HTML (под будущий редактор TinyMCE / CKEditor).

## Стек

Java 21 · Spring Boot 4.0.5 (Spring Web MVC, Spring Data JPA, Validation, Spring Security + OAuth2 Resource Server) · Keycloak 26 · MinIO (S3) · PostgreSQL 17 · Liquibase · MapStruct · Lombok · springdoc-openapi (Swagger) · SLF4J/Logback · JUnit 5 + Mockito · Docker / Docker Compose

## Запуск одной командой

```bash
docker compose up -d
```

Поднимаются:

| Контейнер | Что это | Порт |
|---|---|---|
| `lms-postgres` | БД основного сервиса | 5432 |
| `lms-keycloak-postgres` | Собственная БД Keycloak (только во внутренней сети `keycloak-db`, основной сервис её не видит) | — |
| `lms-keycloak` | Образ `theleriz/lms-keycloak`: Keycloak 26 со встроенным realm `lms` (`keycloak/realm-lms.json`) | 8180 |
| `lms-keycloak-init` | Образ `theleriz/lms-keycloak-init`: одноразово выполняет `keycloak/init/insert.sql` в БД Keycloak и завершается | — |
| `lms-minio` | MinIO (S3) — хранилище файлов уроков, консоль http://localhost:9001 (`minioadmin` / `minioadmin`) | 9000, 9001 |
| `lms-minio-init` | Одноразово создаёт bucket `lms-files` и завершается | — |
| `lms-main-service` | Основной сервис, стартует после `lms-keycloak-init` и `lms-minio-init` | 8080 |

Liquibase основного сервиса создаёт таблицы (`courses`, `chapters`, `lessons`, `attachments`) и загружает стартовые данные: курсы Java Developer, Python Developer и SQL and PostgreSQL Developer с главами, лекциями и практиками.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Docker Hub:
  - https://hub.docker.com/r/theleriz/main-service (`3.0` — с MinIO и вложениями, `latest`; `2.0` — Keycloak и Spring Security; `1.0` — без безопасности)
  - https://hub.docker.com/r/theleriz/lms-keycloak (`1.0`, `latest`)
  - https://hub.docker.com/r/theleriz/lms-keycloak-init (`1.0`, `latest`)

Все образы стягиваются из Docker Hub: для запуска достаточно одного файла `docker-compose.yml`, собирать проект локально не нужно.
Официальные образы `minio/minio` больше не публикуются, поэтому используется поддерживаемая сообществом сборка `pgsty/minio` и `pgsty/mc`.

Остановить и удалить данные: `docker compose down -v`.

## Keycloak и безопасность

- Админка Keycloak: http://localhost:8180 (`admin` / `admin`, realm `master`), рабочий realm — `lms`.
- Realm `lms`: JWT access token живёт 5 минут, refresh token — 168 часов (SSO Session Idle / Max = 604800 с).
- Клиент `lms-main-service` (confidential, secret `lms-main-service-secret`): password и refresh_token grant для логина, service account с ролями `manage-users`, `view-users`, `query-users`, `view-realm` для регистрации и обновления пользователей через Admin REST API.

Роли и пользователи создаются SQL-скриптом [`keycloak/init/insert.sql`](keycloak/init/insert.sql) прямо в БД Keycloak (Liquibase основного сервиса к ней доступа не имеет). Скрипт идемпотентный.

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
| `GET` курсов, глав, уроков | `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_USER` |
| `/course/**`, `/chapter/**` — создание, изменение, удаление курсов и глав | только `ROLE_ADMIN` |
| `POST` / `PUT` / `DELETE` уроков | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST /attachment/upload` | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `GET /attachment/download/{id}` | у кого есть доступ к курсу урока (проверяется перед скачиванием) |
| `POST /api/v1/users`, `PUT /api/v1/users/{id}` | только `ROLE_ADMIN` |
| `GET` / `PUT /api/v1/users/me` | любой аутентифицированный; менять роли может только `ROLE_ADMIN` |

Без токена — 401, без нужной роли — 403, в том же формате `ErrorResponse`.

Доступ к курсу проверяет `CourseAccessChecker`. Записи на курсы пока нет, поэтому доступ ко всем курсам есть у любого пользователя с ролью `ROLE_ADMIN`, `ROLE_TEACHER` или `ROLE_USER`; пользователь Keycloak без этих ролей скачать файл не может.

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
| POST | `/course` | Создать курс (`ROLE_ADMIN`) → 201 |
| PUT | `/course/{id}` | Обновить курс (`ROLE_ADMIN`) → 200 |
| DELETE | `/course/{id}` | Удалить курс с главами, уроками и вложениями (`ROLE_ADMIN`) → 204 |
| GET | `/api/v1/chapters` | Все главы |
| GET | `/api/v1/chapters/{id}` | Глава по id |
| GET | `/api/v1/chapters/{id}/lessons` | Уроки главы (по `order`) |
| POST | `/chapter` | Создать главу (`ROLE_ADMIN`) → 201 |
| PUT | `/chapter/{id}` | Обновить главу (`ROLE_ADMIN`) → 200 |
| DELETE | `/chapter/{id}` | Удалить главу с уроками (`ROLE_ADMIN`) → 204 |
| GET | `/api/v1/lessons` | Все уроки |
| GET | `/api/v1/lessons/{id}` | Урок по id |
| GET | `/api/v1/lessons/{id}/attachments` | Вложения урока |
| POST | `/api/v1/lessons` | Создать урок → 201 |
| PUT | `/api/v1/lessons/{id}` | Обновить урок → 200 |
| DELETE | `/api/v1/lessons/{id}` | Удалить урок → 204 |
| POST | `/attachment/upload` | Загрузить файл к уроку: `multipart/form-data`, поля `lessonId` и `file` (`ROLE_ADMIN`, `ROLE_TEACHER`) → 201 |
| GET | `/attachment/download/{id}` | Скачать вложение (с проверкой доступа к курсу) |

Вложения: файл хранится в MinIO, bucket `lms-files`, под именем `lessons/{lessonId}/{uuid}.{расширение}` — это и есть поле `url` сущности `Attachment`. Исходное имя файла хранится в `name` и отдаётся при скачивании. У урока может быть несколько вложений, максимальный размер файла — 50 MB.

```bash
curl -X POST http://localhost:8080/attachment/upload -H "Authorization: Bearer $TOKEN" -F lessonId=1 -F file=@notes.pdf
curl -OJ http://localhost:8080/attachment/download/1 -H "Authorization: Bearer $TOKEN"
```

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

404 — объект не найден, 400 — ошибка валидации (с `fieldErrors`), кривой JSON или неверный тип id, 401 — нет/неверный токен или неверный логин/пароль, 403 — нет прав, 409 — username или email уже заняты, 413 — файл больше 50 MB, 500 — непредвиденная ошибка.

## Сборка образов

```bash
docker build -t theleriz/main-service:latest .
docker build -t theleriz/lms-keycloak:latest keycloak
docker build -t theleriz/lms-keycloak-init:latest keycloak/init
```

## Локальная разработка

```bash
docker compose up -d postgres keycloak-init minio-init
./mvnw spring-boot:run
```

Параметры БД задаются переменными окружения `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (по умолчанию `localhost:5432/lms_main`, `postgres/postgres`).
Keycloak — `KEYCLOAK_URL` (по умолчанию `http://localhost:8180`), `KEYCLOAK_ISSUER_URL`, `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET`.
MinIO — `MINIO_URL` (по умолчанию `http://localhost:9000`), `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET` (по умолчанию `lms-files`).

## Тесты

```bash
./mvnw test
```

Тесты сервисов (Mockito вместо Repository, KeycloakClient и FileService) и контроллеров (`@WebMvcTest` с настоящим `SecurityConfig`) не требуют запущенных БД, Keycloak и MinIO.
Полный контекстный тест `MainServiceApplicationTests` включается переменной `INTEGRATION_TESTS=true` при запущенном PostgreSQL.

## Структура

```
src/main/java/kz/bitlab/mainservice/
├── config/        OpenApiConfig, SecurityConfig, MinioConfig, MinioProperties
├── controller/    Course/Chapter/LessonController (чтение), CourseAdminController (/course),
│                  ChapterAdminController (/chapter), AttachmentController, AuthController, UserController
├── dto/           *Dto, *CreateRequest, *UpdateRequest, AttachmentDto, FileDownload, LoginRequest, ...
├── entity/        Course, Chapter, Lesson, Attachment
├── exception/     GlobalExceptionHandler, ErrorResponse, NotFoundException, BadRequestException,
│                  UnauthorizedException, ConflictException, FileStorageException
├── keycloak/      KeycloakClient (token endpoint + Admin REST API), KeycloakProperties, dto/
├── mapper/        Course/Chapter/Lesson/AttachmentMapper, AuthMapper, UserMapper (MapStruct)
├── repository/    CourseRepository, ChapterRepository, LessonRepository, AttachmentRepository
├── security/      Role, AuthUser, KeycloakJwtAuthenticationConverter, CourseAccessChecker
└── service/       интерфейсы + impl/ (FileService → MinioFileService, AttachmentService, ...)
src/main/resources/db/changelog/
├── db.changelog-master.yaml
└── changes/       001-create-tables.yaml, 002-insert-data.yaml, 003-create-attachments.yaml, data/*.sql
keycloak/                 образ theleriz/lms-keycloak
├── Dockerfile
├── realm-lms.json        realm lms, время жизни токенов, клиент lms-main-service
└── init/                 образ theleriz/lms-keycloak-init
    ├── Dockerfile
    └── insert.sql        роли ROLE_ADMIN / ROLE_TEACHER / ROLE_USER и пользователи в БД Keycloak
```
