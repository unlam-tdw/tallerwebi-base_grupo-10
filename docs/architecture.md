# Architecture Overview

This document explains the layered architecture of the project, why each layer exists, and the rules that govern dependencies.

## Layers

The project follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│            presentation/                │
│   Controllers, DTOs, Interceptors       │
│   (HTTP in, HTTP out)                   │
├─────────────────────────────────────────┤
│              domain/                    │
│   Services, Entities, Exceptions        │
│   (Business logic)                      │
├─────────────────────────────────────────┤
│           infrastructure/               │
│   Repositories, JPA config              │
│   (Data access)                         │
└─────────────────────────────────────────┘
```

### Rule: dependencies point inward

- `presentation` depends on `domain` (calls services)
- `domain` depends on nothing (pure business logic)
- `infrastructure` depends on `domain` (implements repository interfaces)

**Never** let `domain` import from `presentation` or `infrastructure`. The domain layer must be independent.

## Package Details

### `domain/` — Business Logic

The domain layer contains the core business rules. It has no knowledge of HTTP, databases, or frameworks.

| File | Purpose |
| :--- | :--- |
| `User.java` | JPA entity — maps to the `users` table |
| `LoginService.java` | Interface — defines what the service can do |
| `LoginServiceImpl.java` | Implementation — contains the actual business logic |
| `exception/` | Custom exceptions for domain errors |

**Rules:**
- Services are defined as interfaces, implemented as `@Service` classes
- Business exceptions extend `RuntimeException`
- Entities use JPA annotations but no Spring MVC annotations

### `presentation/` — HTTP Layer

The presentation layer handles HTTP requests and responses. It translates between HTTP and the domain.

| File | Purpose |
| :--- | :--- |
| `login/LoginController.java` | Handles `/login`, `/validate-login`, `/register`, `/home`, `/logout` |
| `login/LoginRequest.java` | DTO for login form data (with validation annotations) |
| `shared/NewUserRequest.java` | DTO for registration form data (with validation annotations) |
| `shared/UserSession.java` | DTO stored in HTTP session (email + role) |
| `shared/SessionInterceptor.java` | Guards `/home`, `/users` — redirects to `/login` if no session |
| `shared/GlobalExceptionHandler.java` | `@ControllerAdvice` — handles exceptions globally |

**Rules:**
- Controllers return `ModelAndView` (view name + model data)
- Use `@Valid` + `BindingResult` for form validation
- Never put business logic in controllers — delegate to services
- Use DTOs for form data, never pass entities to templates
- Cross-cutting concerns (interceptors, exception handling, shared DTOs) go in `shared/`

### `infrastructure/` — Data Access

The persistence layer handles database access via Spring Data JPA.

| File | Purpose |
| :--- | :--- |
| `UserRepository.java` | Spring Data interface — `findByEmail()`, etc. |

**Rules:**
- Repositories extend `JpaRepository<Entity, IdType>`
- No business logic in repositories — only queries
- Domain exceptions propagate up, repository exceptions are caught by `GlobalExceptionHandler`

### `config/` — Spring Configuration

Wires all the framework pieces together. These classes are loaded by `MyServletInitializer` at startup.

| File | Purpose |
| :--- | :--- |
| `SpringWebConfig.java` | MVC config — extends `BaseWebConfig` for production |
| `BaseWebConfig.java` | Shared MVC base — Thymeleaf view resolver, interceptors, resource handlers |
| `JpaConfig.java` | JPA / DataSource config — connects to PostgreSQL |
| `DatabaseInitializationConfig.java` | Schema initialization (DDL) |
| `EnvironmentConfig.java` | Reads `DB_HOST`, `DB_PORT`, etc. from env vars with defaults |
| `SecurityConfig.java` | `PasswordEncoder` bean (BCrypt) |
| `ValidationConfig.java` | Bean Validation (`LocalValidatorFactoryBean`) |
| `DevReloadController.java` | Dev-only endpoint — returns a token that changes when templates change |
| `DevReloadInterceptor.java` | Dev-only interceptor — adds reload headers to every response |

### `MyServletInitializer.java` — Bootstrap

Extends `AbstractAnnotationConfigDispatcherServletInitializer` — this is the entry point for deploying the WAR to an external servlet container (Jetty via `mvn jetty:run`, Tomcat, etc.). It registers `SpringWebConfig`, `JpaConfig`, and `DatabaseInitializationConfig` as the servlet context.

## Request Flow

Here's what happens when a user submits the login form:

```
1. POST /validate-login
       ↓
2. LoginController.validateLogin()
       ↓
3. @Valid + BindingResult checks DTO validation
       ↓ (if valid)
4. LoginService.findUser(email, password)
       ↓
5. UserRepository.findByEmail() — database query
       ↓
6. If user found → create UserSession → store in HttpSession → redirect /home
   If not found → return login view with error
```

## Key Patterns

### Dependency Injection

Controllers receive services via constructor injection:

```java
@Controller
public class LoginController {

  private final LoginService loginService;

  @Autowired
  public LoginController(LoginService loginService) {
    this.loginService = loginService;
  }
}
```

### DTOs for Form Data

Form data is mapped to DTOs with validation annotations:

```java
public class LoginRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email is not valid")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;
}
```

### Session-based Authentication

Auth state is stored in the HTTP session:

```java
// Login — store session
UserSession userSession = new UserSession(user.getEmail(), user.getRole());
request.getSession().setAttribute(SessionInterceptor.USER_SESSION, userSession);

// Guard — check session
UserSession user = (UserSession) session.getAttribute(SessionInterceptor.USER_SESSION);
if (user == null) {
  return "redirect:/login";
}

// Logout — invalidate session
session.invalidate();
```

### Global Exception Handling

`@ControllerAdvice` catches exceptions and returns appropriate views:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyExists.class)
  public ModelAndView handleUserAlreadyExists() {
    // Return registration form with error
  }

  @ExceptionHandler(Exception.class)
  public ModelAndView handleUnexpectedError(Exception ex) {
    // Log and return generic error page
  }
}
```

### Password Hashing (BCrypt)

Passwords are never stored in plain text. `SecurityConfig` registers a `BCryptPasswordEncoder` bean:

```java
@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
```

Services inject `PasswordEncoder` and use it to hash on registration and verify on login:

```java
// Registration — hash before saving
String hashed = passwordEncoder.encode(rawPassword);
user.setPassword(hashed);

// Login — compare raw input against stored hash
if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
  throw new InvalidCredentials();
}
```

**Rule:** never store plain passwords. Always go through the `PasswordEncoder` bean.
