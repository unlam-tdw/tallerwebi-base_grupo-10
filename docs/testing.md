# Testing Guide

What to test at each layer, how to write tests, and the testing conventions used in this project.

## Test Pyramid

```
        ┌─────────┐
        │   E2E   │  Playwright, real browser, real database
        │ (slow)  │
        ├─────────┤
        │Integration│  MockMvc + in-memory HSQLDB
        │  (medium) │
        ├─────────┤
        │  Unit   │  Mockito, no Spring context
        │ (fast)  │
        └─────────┘
```

## Unit Tests (`presentation/`)

Pure Mockito tests. No Spring context. Fast.

### Pattern

```java
public class LoginControllerTest {

  private LoginController controller;
  private LoginService loginServiceMock;

  @BeforeEach
  public void init() {
    loginServiceMock = mock(LoginService.class);
    controller = new LoginController(loginServiceMock);
  }

  @Test
  public void shouldReturnLoginView() {
    ModelAndView modelAndView = controller.showLogin();
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("pages/auth/login"));
  }
}
```

### What to test

| Method | Test cases |
| :--- | :--- |
| `showLogin()` | Returns correct view, includes `LoginRequest` in model |
| `validateLogin()` | Success → redirect; failure → re-render with error; invalid input → validation error |
| `register()` | Success → redirect; duplicate email → exception; invalid input → validation error |
| `showHome()` | Has session → home view; no session → redirect to login |
| `logout()` | Invalidates session; handles null session |

### Key patterns

**Mock the service, not the repository:**
```java
loginServiceMock = mock(LoginService.class);
controller = new LoginController(loginServiceMock);
```

**Use `ArgumentCaptor` to verify session storage:**
```java
ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
verify(sessionMock, times(1))
  .setAttribute(eq(SessionInterceptor.USER_SESSION), captor.capture());
assertThat(captor.getValue().getEmail(), equalToIgnoringCase("dami@unlam.com"));
```

**Test that exceptions propagate:**
```java
@Test
public void shouldPropagateExceptionWhenEmailAlreadyExists() {
  doThrow(UserAlreadyExists.class).when(loginServiceMock).register(anyString(), anyString());
  BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(newUserData, "newUserData");
  assertThrows(UserAlreadyExists.class, () -> controller.register(newUserData, bindingResult));
}
```

## Integration Tests (`integration/`)

MockMvc tests with the full Spring context. Uses in-memory HSQLDB.

### Pattern

```java
@WebIntegrationTest
public class LoginControllerTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private LoginService loginService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void shouldRedirectToLoginPageFromRoot() throws Exception {
    this.mockMvc.perform(get("/"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }
}
```

### What to test

| Endpoint | Test cases |
| :--- | :--- |
| `GET /` | Redirects to `/login` |
| `GET /login` | Returns login view with `loginData` model attribute |
| `POST /validate-login` | Valid credentials → redirect `/home`; invalid → re-render with error; missing fields → validation error |
| `GET /new-user` | Returns registration view |
| `POST /register` | New email → redirect `/login`; duplicate → error; invalid → validation error |
| `GET /home` | No session → redirect `/login`; with session → home view |
| `POST /logout` | Invalidates session, redirects to `/login` |

### Key patterns

**Use composed annotations:**
```java
@WebIntegrationTest    // MockMvc + in-memory HSQLDB
public class LoginControllerTest { ... }
```

**Create test data in `@BeforeEach`:**
```java
@BeforeEach
public void setUp() {
  this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  if (userRepository.findByEmail(LOGIN_EMAIL).isEmpty()) {
    loginService.register(LOGIN_EMAIL, LOGIN_PASSWORD);
  }
}
```

## E2E Tests (`e2e/`)

Playwright tests with a real browser. Requires MySQL + app running.

### Pattern

```java
public class LoginViewE2E {

  @Test
  public void shouldNavigateToHomeWhenUserExists() {
    page.navigate(baseUrl + "/login");
    page.locator("#email").fill("test@unlam.edu.ar");
    page.locator("#password").fill("test");
    page.locator("#btn-login").click();
    waitForPath("/spring/home");
  }
}
```

### What to test

| Flow | Test cases |
| :--- | :--- |
| Login | Fill form → submit → navigate to home |
| Login error | Wrong password → error message appears |
| Register | Fill form → submit → navigate to login |
| Logout | Click logout → navigate to login |

### Key patterns

**Wait for navigation (async):**
```java
private void waitForPath(String expectedPath) {
  await().atMost(Duration.ofSeconds(5))
    .until(() -> page.url().contains(expectedPath));
}
```

**UI contract:** E2E tests depend on element IDs and names:
- `#email`, `#password` — input fields
- `#btn-login`, `#btn-register` — buttons
- Error message text: "Invalid email or password"

## Running Tests

```shell
# All Java tests (unit + integration)
mvn test

# Specific test class
mvn test -Dtest="LoginControllerTest"

# Specific test method
mvn test -Dtest="LoginControllerTest#shouldReturnToLoginWhenCredentialsAreWrong"

# E2E tests (requires Docker stack running)
docker compose --profile dev up -d
mvn test -Dtest="LoginViewE2E"
```

## Coverage

Coverage is measured by JaCoCo. See [code-quality.md](code-quality.md) for details.

**Requirements:**
- `domain/` and `presentation/` must reach **100%** line coverage
- `infrastructure/` must reach **80%**
- Global floor: **80%**
