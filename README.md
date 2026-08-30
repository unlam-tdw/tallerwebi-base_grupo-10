# Valhalla — Taller Web I

Spring MVC + Thymeleaf + Tailwind project for Taller Web I (UNLaM).

## Quick Start

```shell
git clone <repo-url>
cd valhalla
mvn clean package
docker compose up --build
```

The app runs at [http://localhost:8080/spring](http://localhost:8080/spring).

**Default credentials:** `test@unlam.edu.ar` / `test`

### Local development (recommended)

```shell
docker compose --profile dev up
```

This starts MySQL + the app in Docker with hot-reload. The app is available at http://localhost:8080/spring.

## Project Structure

```
src/main/java/com/valhalla/
├── config/           # Spring configuration
├── domain/           # Business logic (services, models, exceptions)
├── infrastructure/   # Persistence (Spring Data JPA repositories)
└── presentation/     # MVC controllers, DTOs, session interceptor, exception handling

src/main/webapp/
├── WEB-INF/templates/
│   ├── layouts/      # base page chrome (head + layout decorator)
│   ├── components/   # reusable fragments (navbar, alerts)
│   └── pages/        # full pages, grouped by feature (auth/)
└── resources/core/js/
    ├── tailwind-browser.js  # Tailwind CSS compiled in the browser
    └── vue.global.prod.js   # Vue.js for client-side interactivity
```

## Documentation

| Document | Description |
| :--- | :--- |
| [Architecture](docs/architecture.md) | Layered architecture, dependency rules, request flow |
| [Adding a Feature](docs/adding-a-feature.md) | Step-by-step: domain → repository → service → controller → template |
| [Frontend Stack](docs/frontend-stack.md) | Why each tool exists and team conventions |
| [Vue + Tailwind Guide](docs/guide-vue.md) | How to create views with Vue |
| [CSS + JS Guide](docs/guide-css-js.md) | How to create views with plain CSS + JS |
| [Error Handling](docs/error-handling.md) | Validation, custom exceptions, GlobalExceptionHandler |
| [Testing](docs/testing.md) | Unit tests, integration tests, E2E tests |
| [Environment Setup](docs/setup.md) | Install Java, Maven, Docker, IDE config |
| [Commands Reference](docs/commands.md) | Maven, Docker, and testing commands |
| [Code Quality](docs/code-quality.md) | Checkstyle, PMD, CPD, JaCoCo, Prettier |

## Authentication

Session-based. On successful login the controller maps the domain `User` to a `UserSession` DTO (email + role) and stores it in the HTTP session. `SessionInterceptor` guards `/home` (redirects to `/login` if no session), and `POST /logout` invalidates the session. New registrations get `role = USER` and `active = true`.

## Technologies

- Docker
- Java 25 (LTS)
- Spring 6.2.19 + Spring Data JPA 3.5.13
- Hibernate Validator 8.0.5.Final
- Thymeleaf 3.1.5.RELEASE
- Embedded Jetty Server EE10 12.0.37
- Tailwind CSS 4.3.3 (compiled in the browser)
- Vue.js 3.5.13 (client-side interactivity)
- Spring Test 6.2.19 / Hamcrest 2.2 / JUnit 6.1.2
- Mockito 5.23.0 / Playwright 1.61.0
- PMD 7.26.0 / Checkstyle 13.8.0 / Prettier 0.22 / JaCoCo 0.8.15
