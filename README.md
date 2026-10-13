# BITLAB ACADEMY LMS — Main Service

Основной сервис LMS BITLAB ACADEMY: управление курсами, главами и уроками.
Курс состоит из упорядоченных глав, глава — из упорядоченных уроков; содержимое урока хранится как HTML (под будущий редактор TinyMCE / CKEditor).

## Стек

Java 21 · Spring Boot 4.0.5 (Spring Web MVC, Spring Data JPA, Validation) · PostgreSQL 17 · Liquibase · MapStruct · Lombok · springdoc-openapi (Swagger) · SLF4J/Logback · JUnit 5 + Mockito · Docker / Docker Compose

## Запуск одной командой

```bash
docker compose up -d
```

Поднимаются PostgreSQL и Main Service. Liquibase создаёт таблицы и загружает стартовые данные: курсы Java Developer, Python Developer и SQL and PostgreSQL Developer с главами, лекциями и практиками.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Docker Hub: https://hub.docker.com/r/theleriz/main-service (`theleriz/main-service:1.0`, `latest`)

Образ стягивается из Docker Hub, собирать проект локально не нужно.

Остановить и удалить данные: `docker compose down -v`.

## API

| Метод | Путь | Описание |
|---|---|---|
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

404 — объект не найден, 400 — ошибка валидации (с `fieldErrors`), кривой JSON или неверный тип id, 500 — непредвиденная ошибка.

## Локальная разработка

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

Параметры БД задаются переменными окружения `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (по умолчанию `localhost:5432/lms_main`, `postgres/postgres`).

## Тесты

```bash
./mvnw test
```

Тесты сервисов (Mockito вместо Repository) и контроллеров (`@WebMvcTest`) не требуют запущенной БД.
Полный контекстный тест `MainServiceApplicationTests` включается переменной `INTEGRATION_TESTS=true` при запущенном PostgreSQL.

## Структура

```
src/main/java/kz/bitlab/mainservice/
├── config/        OpenApiConfig
├── controller/    CourseController, ChapterController, LessonController
├── dto/           *Dto, *CreateRequest, *UpdateRequest
├── entity/        Course, Chapter, Lesson
├── exception/     GlobalExceptionHandler, ErrorResponse, NotFoundException
├── mapper/        CourseMapper, ChapterMapper, LessonMapper (MapStruct)
├── repository/    CourseRepository, ChapterRepository, LessonRepository
└── service/       интерфейсы + impl/
src/main/resources/db/changelog/
├── db.changelog-master.yaml
└── changes/       001-create-tables.yaml, 002-insert-data.yaml, data/*.sql
```
