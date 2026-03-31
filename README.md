# Система мониторинга пожарной безопасности (учебный корпус)

REST API на Spring Boot: учёт датчиков, инциденты (alerts), пользователи и роли, JWT в cookie, загрузка фото (Thymeleaf), PDF-отчёт при закрытии инцидента, уведомления в Telegram при создании инцидента.

## Технологии

- Java **17**
- Spring Boot **4.0.2** (Web, Data JPA, Security, Validation, Thymeleaf)
- **PostgreSQL**
- **JWT** (хранение в cookie `jwtToken`, дополнительно поддерживается заголовок `Authorization: Bearer …`)
- **Swagger / OpenAPI**
- **iText** (PDF)
- **Telegram Bot API** (исходящие HTTPS-запросы через `RestClient`, без библиотеки `telegrambots`)

## Требования

- JDK **17+**
- **Maven** (в проекте есть `mvnw` / `mvnw.cmd`)
- **PostgreSQL** с созданной БД (по умолчанию имя базы **`app`**)

## Настройка PostgreSQL

1. Создайте базу, например:

   ```sql
   CREATE DATABASE app;
   ```

2. Учётные данные по умолчанию в профиле **`dev`** (`application.yaml`): пользователь **`postgres`**, пароль **`admin`**, URL **`jdbc:postgresql://localhost:5432/app`**. При необходимости измените под свою среду.

3. При старте приложения Hibernate обновит схему (`ddl-auto: update`), скрипт **`src/main/resources/data.sql`** подставит роли, права и тестового администратора.

## Переменные окружения (опционально)

| Переменная | Назначение |
|------------|------------|
| `TELEGRAM_BOT_TOKEN` | Токен бота от @BotFather |
| `TELEGRAM_CHAT_ID` | ID чата для уведомлений |

Если не заданы, приложение запускается, но уведомления в Telegram пропускаются (в логе будет предупреждение).

## Запуск

Из корня проекта:

```bash
./mvnw spring-boot-run
```

Windows:

```cmd
mvnw.cmd spring-boot-run
```

Профиль по умолчанию: **`dev`**, порт: **`http://localhost:9000`**.

## Тестовый вход

После применения `data.sql`:

- **Логин:** `admin`  
- **Пароль:** `admin`  
- **Роль:** `ADMIN` (полный набор прав)

**Регистрация нового пользователя:** `POST /api/auth/register` с телом **`RegisterUserDTO`** (поля `username`, `password`, опционально `roleTitles` — массив строк, например `["OPERATOR"]`). Дубликат логина отсекается валидацией **`@UniqueUsername`**.

## Роли и права (RBAC)

В БД заведены роли (см. `data.sql`). У каждой роли — набор **permissions**; в JWT попадают именно они, а Spring Security проверяет **authority** вида `alert.read`, `sensor.write` и т.д.

| Роль | Суть | Права (упрощённо) |
|------|------|-------------------|
| **ADMIN** | Полный доступ | Все: `alert.*`, `sensor.*`, `user.*` (чтение/запись инцидентов и датчиков, пользователи, роли/права на чтение) |
| **OPERATOR** | Работа с инцидентами | `alert.read`, `alert.write` — видеть и создавать/менять инциденты, **нет** `alert.delete`, **нет** прав на датчики и пользователей |
| **VIEWER** | Только просмотр | Все **read**: `alert.read`, `sensor.read`, `user.read` — списки и детали, без создания/изменения |

Точное соответствие «метод HTTP → нужная authority» задано в `SecurityConfig` (например, `DELETE /api/incidents/**` требует `alert.delete`, поэтому у **OPERATOR** удаления инцидента не будет).

## Модель «датчик → инцидент»

В предметной области **инцидент не существует «сам по себе»**: у него обязательна связь с **датчиком** (`sensor_id` в БД). Поэтому:

1. Сначала должен быть **датчик** — создайте его через `POST /api/sensors` (нужно право `sensor.write`, оно есть у **ADMIN**) и возьмите из ответа **`id`**.
2. Затем **инцидент** — `POST /api/incidents` с полем **`sensorId`**, указывающим на этот датчик (нужно `alert.write`).

Если передать несуществующий `sensorId`, API вернёт **404** (`ResourceNotFoundException`: датчик не найден). Это не баг, а правило модели данных.

**Типичный сценарий после входа под ADMIN:** логин → `POST /api/sensors` → `POST /api/incidents` с `sensorId` из ответа датчика → при настроенном Telegram уйдёт уведомление в бот.

## Документация API

- Swagger UI: **http://localhost:9000/swagger-ui.html**  
- OpenAPI JSON: **http://localhost:9000/v3/api-docs**

Для защищённых методов: сначала `POST /api/auth/login`, затем cookie `jwtToken` подставится автоматически в браузере при запросах на тот же origin; для SPA с другого порта используйте поле **`token`** из ответа и заголовок **`Authorization: Bearer`** с этим токеном (фильтр читает и cookie, и Bearer). Профиль и права для UI: **`GET /api/auth/me`**.

## Важные REST-эндпоинты (кратко)

| Метод | Путь | Назначение |
|--------|------|------------|
| POST | `/api/auth/register` | Регистрация |
| POST | `/api/auth/login` | Вход: JWT в cookie `jwtToken` и в JSON `{ "message", "token" }` (удобно для SPA + `Authorization: Bearer`) |
| GET | `/api/auth/me` | Текущий пользователь: id, username, enabled, roleTitles, **permissions** (требуется JWT) |
| CRUD | `/api/incidents` | Инциденты (alerts) |
| CRUD | `/api/sensors` | Датчики |
| CRUD | `/api/users` | Пользователи (`POST` — только с правом `user.write`, для создания учёток из админки) |
| GET | `/api/roles`, `/api/permissions` | Справочники |
| GET | `/incidents/{id}/upload` | Форма загрузки фото (Thymeleaf) |

Поле **`type`** в теле инцидента — **строка**, допустимые значения: `ACCIDENT`, `HARD_BRAKING`, `BUTTON` (регистр не важен). Подробнее про порядок «сначала датчик» — в разделе **«Модель датчик → инцидент»** выше.

## Файлы и отчёты

- Загрузки: каталог из **`app.upload.dir`** (по умолчанию `uploads`).
- PDF при переводе инцидента в статус **RESOLVED**: каталог `uploads/reports`, URL сохраняется в поле **`reportUrl`** сущности, если PDF создан успешно.
- Фото и отчёты отдаются по путям **`/uploads/**`** (`WebConfig`). Для SPA без передачи JWT в `<img>` / прямой ссылке **`GET /uploads/**` разрешён без авторизации**; загрузка по-прежнему только через API с правом **`alert.write`** (`POST /api/incidents/{id}/photos`).

## Дамп базы для репозитория / отчёта

```bash
pg_dump -U postgres -d app -f dump.sql
```

(Укажите свой пользователь и имя БД при отличии от настроек.)

## Структура исходников

- `model/` — JPA-сущности  
- `repository/` — Spring Data  
- `service/` — бизнес-логика  
- `controller/` — REST и Thymeleaf  
- `dto/` — объекты запросов/ответов  
- `config/` — Security, Swagger, CORS, веб-настройки  
- `exception/` — кастомные исключения и `GlobalExceptionHandler`  
- `validation/` — кастомные Bean Validation (`@ValidEventType`, `@UniqueUsername`)

## Безопасность

Секрет JWT и пароли в `application.yaml` приведены для **локальной разработки**. Для продакшена задайте сильный **`jwt.secret`**, отключите вывод SQL, настройте **HTTPS** и cookie **`Secure`**, не коммитьте реальные токены Telegram и пароли БД.
