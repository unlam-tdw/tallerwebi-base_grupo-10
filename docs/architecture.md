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
| `LoginController.java` | Handles `/login`, `/validate-login`, `/register`, `/home`, `/logout` |
| `LoginRequest.java` | DTO for login form data (with validation annotations) |
| `NewUserRequest.java` | DTO for registration form data (with validation annotations) |
| `UserSession.java` | DTO stored in HTTP session (email + role) |
| `SessionInterceptor.java` | Guards `/home` — redirects to `/login` if no session |
| `GlobalExceptionHandler.java` | `@ControllerAdvice` — handles exceptions globally |

**Rules:**
- Controllers return `ModelAndView` (view name + model data)
- Use `@Valid` + `BindingResult` for form validation
- Never put business logic in controllers — delegate to services
- Use DTOs for form data, never pass entities to templates

### `infrastructure/` — Data Access

The persistence layer handles database access via Spring Data JPA.

| File | Purpose |
| :--- | :--- |
| `UserRepository.java` | Spring Data interface — `findByEmail()`, etc. |

**Rules:**
- Repositories extend `JpaRepository<Entity, IdType>`
- No business logic in repositories — only queries
- Domain exceptions propagate up, repository exceptions are caught by `GlobalExceptionHandler`

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
