Taller Web I base project (Maven and Thymeleaf)
===============================

## Development environment setup
Before working with the project, we must install and configure some tools:

### Java
Java is the programming language we will work with. The project is configured to run with Java 25 (LTS).
* Download the JDK for your operating system: [JDK 25 Temurin for Linux/Windows](https://adoptium.net/temurin/releases/?version=25).
* Unzip the downloaded file into a folder.
    * Example: `/home/java` (Linux) or `C:\java` (Windows).
* Set an environment variable named `JAVA_HOME` pointing to the folder where the downloaded file was unzipped.
    * Example: `C:\java\jdk-25` (Windows) or `/home/java/jdk-25` (Linux).
* Add `JAVA_HOME` to the `PATH` environment variable:
    * Add `%JAVA_HOME%\bin` to the existing list.
* After saving the environment variable configuration, run `java -version` and then `javac -version` in CMD or Terminal; you should see the installed Java version and the compiler version as output, respectively.
* [Guide to installing Java on Windows](https://www.java.com/es/download/help/windows_manual_download.html)
* [Guide to installing Java on Linux](https://www.java.com/es/download/help/linux_x64_install.html)

### Maven
Maven is a project management tool (mainly for Java projects). It simplifies and standardizes the software build process.
* It is required to have Java 25 installed and the environment variables configured (`JAVA_HOME` and `PATH`).
* Download Maven from the [official site](https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip) and unzip it into a folder (it can live next to the Java installation or elsewhere).
* Set an environment variable named `MAVEN_HOME` pointing to the folder where the downloaded file was unzipped.
    * Example: `/home/maven/apache-maven-3.9.16` (Linux) or `C:\maven\apache-maven-3.9.16` (Windows).
* Add `MAVEN_HOME` to the `PATH` environment variable:
    * Add `%MAVEN_HOME%\bin` to the existing list.
* After saving the environment variable configuration, run `mvn -version` in CMD or Terminal; you should see the downloaded Maven version as output. If the Terminal or CMD was open during the configuration, close it and open it again.
* **.m2 folder**: Maven's local repository where it stores artifacts (like JAR files), downloaded as dependencies or generated locally. By default this folder is created in the following paths:
    * Linux: `/home/<my user>/.m2`.
    * Windows: `C:\Users\<my user>\.m2`.
* [Guide to installing Maven on Windows or Linux](https://maven.apache.org/install.html).

#### IDE configuration
* IntelliJ: Maven comes installed and the plugin is available in the panel on the right (shown with the letter **M**). It lets you run commands and manage plugins and dependencies.
* VS Code (recommended plugins):
    * Maven for Java: the official plugin. From the explorer, a Maven section appears below the project, making it easy to run commands and manage dependencies and plugins.
    * XML: improves autocompletion and syntax validation in XML files. For Maven the `pom.xml` file is crucial.

### Docker
Docker is a container platform that packages applications together with all their dependencies into lightweight, portable containers. Containers run isolated and consistently in any environment with Docker installed.
* To install on Windows, the simplest path is `Docker Desktop`. Follow this [guide](https://docs.docker.com/desktop/setup/install/windows-install/).
* To install on Linux, just install `Docker Engine` by following this [guide](https://docs.docker.com/engine/install/ubuntu/). You can also install `Docker Desktop` (it includes Docker Engine).

## 1. How to start the project?
> We need a mysql database on port 3306 beforehand.

### Prerequisites (first time on your machine)
You need to have installed:
- **JDK 25**
- **Maven**
- **Docker Desktop** (with Docker Compose v2)

No Node.js is needed: Tailwind is compiled in the browser by a vendored script
(see section 8).

### First time cloning the repo
```powershell
git clone <repo-url>
cd valhalla
docker compose up --build
```

The clone needs no frontend setup: the Tailwind runtime is already vendored in
the repo, so there is nothing to install or compile for styles. `docker compose
up --build` starts MySQL and builds + launches the app on
http://localhost:8080/spring (sign in with `test@unlam.edu.ar` / `test`).

### Local development (recommended)
```shell
docker compose up -d mysql   # database only (the app seeds the schema on boot)
mvn clean jetty:run          # app on http://localhost:8080/spring
```

- **Styles (Tailwind)**: compiled **in the browser** by the vendored
  `@tailwindcss/browser` script — there is no build step, edit any `.html` and
  the utility classes are picked up on refresh.
- **Thymeleaf templates**: update live (cache off).
- **Java changes** require a restart: `Ctrl+C` and run `mvn clean jetty:run`
  again.

### Step by step (without the orchestrator)
```shell
# Start a database with docker
docker build -f DockerfileSQL -t mysql .
docker run --env-file .env --name valhalla-mysql -d -p 3306:3306 mysql

# Start the project
$ mvn clean jetty:run
# http://localhost:8080/spring
```
## 2. Thymeleaf
* [Documentation](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)

### Templates organization
Templates live under `WEB-INF/templates/` and are organized by role:
`layouts/` (shared chrome: `head` fragment plus the `layout(content)`
decorator), `components/` (reusable fragments such as the navbar and alerts)
and `pages/` (complete pages, grouped by feature, e.g. `pages/auth/`).
Views are resolved by path: a controller returns `"pages/auth/login"` and the
`ThymeleafViewResolver` maps it to `WEB-INF/templates/pages/auth/login.html`.
Pages opt into the layout with `th:replace="~{layouts/base :: layout(~{::main})}"`
and fragments are referenced from the template root, e.g.
`th:replace="~{components/navbar}"`.

## 3. Hamcrest
* [Documentation](https://hamcrest.org/JavaHamcrest/javadoc/2.2/)

## 4. GitHub Actions
* [Documentation](https://docs.github.com/en/actions/quickstart)

## 5. Playwright
* [Documentation](https://playwright.dev/java/docs/intro)

## 6. Jetty
* [Documentation](https://eclipse.dev/jetty/documentation/jetty-12/)

## 7. How to run the end-to-end tests?

### Start the server
```shell
# Option 1
$ mvn clean jetty:run

# Option 2 -- see section 10 docker-compose
$ docker-compose up --build
```
### Run the tests in another terminal
```shell
$ mvn test -Dtest="LoginViewE2E"
$ mvn test -Dtest="LoginViewE2E#shouldNavigateToHomeWhenUserExists"
```

> **Note (UI contract):** the E2E tests depend on the rendered views keeping the `id`/`name` values their selectors use (`#email`, `#password`, `#btn-login`, `#btn-register`, `nav a.navbar-brand` and the error message "Invalid email or password"). The frontend only provides styles (Tailwind, compiled in the browser) to those views without changing that contract.

## 8. How are the styles compiled?
There is **no build step**: Tailwind is compiled in the browser. The layout
(`layouts/base.html`) loads the vendored `@tailwindcss/browser` script from
`src/main/webapp/resources/core/js/tailwind-browser.js`, which scans the
rendered DOM and generates the CSS on the fly. A small inline script hides the
page until the styles are ready (no unstyled flash). Thymeleaf renders all HTML
and all business logic lives in Java — the client runs no custom JavaScript.

To **upgrade** Tailwind, replace the vendored file with a newer build:
```shell
$ curl -o src/main/webapp/resources/core/js/tailwind-browser.js \
  https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4.3.3/dist/index.global.js
```

> **Note (test pyramid):** `mvn test` runs the Java test suites (unit + MockMvc
> integration with Spring Test) and does not require Node — the whole app needs
> no frontend toolchain at all. The E2E tests (Playwright) are invoked on demand
> with the section 7 commands and require MySQL on port 3306 and the application
> up and running.

## 9. Docker:
The docker files in this project are set up to deploy a WAR file using the Jetty or Tomcat server.
The docker files for Jetty and Tomcat expect the WAR file to be named "valhalla-1.0-SNAPSHOT"; for that, modify the <artifactId> and <version> attributes in the pom.xml file.

To generate a WAR file, run maven.
```shell
mvn clean package
```

Once we have the WAR file, generate the docker image.
```shell
docker build -f DockerfileJetty -t valhalla .
docker build -f DockerfileTomcat -t valhalla .
```

Once the image is generated, we can instantiate a container and run it.
```shell
docker run -p 8080:8080 valhalla
```

### 9.1 Basic commands
```shell
# Create an image named "valhalla".
docker build -f DockerfileJetty -t valhalla .

# Instantiate and run a container from the "valhalla" image.
docker run -p 8080:8080 valhalla 

# Run an already instantiated container.
docker start <containerId> 

# Instantiate a container from the valhalla image to run bash.
docker run -it --entrypoint /bin/bash valhalla

# Show the logs.
docker logs <containerId>

# Show all running containers.
docker ps

# Show all created (or existing) containers.
docker ps -a 

# Show all created images.
docker images

# Remove a container.
docker rm <containerId>

# Remove an image.
docker rmi <imageId>

# Create an image named "mysql".
docker build -f DockerfileSQL -t mysql .

# Instantiate a container from the mysql image.
docker run --env-file .env --name valhalla-mysql -d -p 3306:3306 mysql # sudo apt install mysql-client
```

## 10. docker-compose
Docker Compose is a tool that lets you define and run multi-container applications using YAML files. It simplifies managing multiple services and their dependencies, letting you orchestrate the whole application stack with a single command.

```shell
mvn clean package
# Invoke docker-compose to create containers for all the specified services
docker-compose up --build

# Invoke docker to remove the created containers
# --rmi local means it must also remove the volumes
docker-compose down --rmi local
```
## 11. Maven commands
To run Maven commands, either in the IDE's integrated terminal or in another terminal like Linux or Windows (CMD), use the main `mvn` command followed by the command or lifecycle phase to execute. Example: `mvn clean`.

> Maven runs all the phases prior to the lifecycle phase you specify.

### Commands:
```shell
# It is a command. Cleans the target directory (containing generated JARs or WARs) from the previous build
mvn clean

# -> Phases in execution order <-

# Validates that the project is correct and that all the required information is available
mvn validate

# Compiles the project source code (also downloads dependencies)
mvn compile

# Runs the Java test suites (unit + MockMvc integration)
mvn test

# Packages the compiled code into a JAR or WAR file (downloads dependencies)
mvn package

# Verifies that the package is valid and meets the quality criteria
mvn verify

# Installs the package into the local Maven repository
mvn install

# Uploads the artifact to a remote repository (which must be defined in the pom.xml file) so it can be distributed to other teams or developers, or deployed (end of the build stage)
mvn deploy

# The 'clean' command can be combined with the phases
mvn clean package

# Possibly the most run command. Dependencies are downloaded when running a build command like 'compile' or 'package' ('install' includes them, plus validations and test runs).
mvn clean install

```

## 12. Code Quality Tools
The project integrates several tools to ensure the code is clean, maintainable and free of common mistakes. These tools run automatically during the Maven lifecycle.

### Tools Overview

| Tool | Main Function | Role in the Project | Report Location |
| :--- | :--- | :--- | :--- |
| **Prettier** | **Automatic Formatting** | Defines the aesthetics (spaces, braces, indentation). It handles the "look". | N/A (Applies changes) |
| **Checkstyle** | **Conventions and Structure** | Validates variable names, Javadoc presence and imports. | `.code-quality/checkstyle/` |
| **PMD** | **Logic and Best Practices** | Detects potential errors, unused variables and optimizations. | `.code-quality/pmd/` |
| **CPD** | **Duplicate Detection** | Finds copied and pasted code blocks (Copy-Paste). | `.code-quality/cpd/` |
| **JaCoCo** | **Test Coverage** | Measures what percentage of the code is covered by tests (>80%). | `.code-quality/jacoco/` |

---

### PMD (Static Code Analyzer)
Analyzes Java code for design issues, unused variables, missing optimizations and bad practices.
* **Runs in:** `validate` phase (check) and `test` phase (report regeneration).
* **Configuration:** Uses `pmd-code-rules.xml`.
* **Commands:**
  * `mvn pmd:check`: Validates the rules and fails on violations.
  * `mvn pmd:pmd`: Generates the visual report at `.code-quality/pmd/pmd.html`.
* **Reports:** All results (XML and HTML) are saved in the `.code-quality/pmd/` folder at the project root.
* **Failure example:** Creating a variable with a very short name (e.g. `int x = 0;`) or having a method with cyclomatic complexity above 10.
* **Documentation:** [PMD Official Site](https://pmd.github.io/)

### CPD (Copy-Paste Detector)
An extension of PMD that detects duplicated (copy-paste) code blocks in the project.
* **Runs in:** `validate` phase (check) and `test` phase (report generation).
* **Commands:**
  * `mvn pmd:cpd-check`: Validates duplicates and fails if it finds any.
  * `mvn pmd:cpd`: Generates the duplicates report at `.code-quality/cpd/cpd.html`.
* **Reports:** All results (XML and HTML) are saved in the `.code-quality/cpd/` folder at the project root.
* **Failure example:** Copying and pasting an identical logic block into two different controllers instead of abstracting it into a service.
* **Documentation:** [CPD Documentation](https://pmd.github.io/latest/pmd_userdocs_cpd.html)

### Checkstyle
Verifies that the code follows formatting and style standards (based on the Google guide). It focuses on the aesthetics and structure of the code.
* **Runs in:** `validate` phase.
* **Configuration:** Uses `checkstyle-base.xml`. This file is a custom configuration that inherits from Google Style but **disables the formatting rules** (indentation, spaces, braces) to avoid conflicts with Prettier, focusing only on naming conventions, Javadocs and imports.
* **Commands:**
  * `mvn checkstyle:check`: Validates the style and fails on violations.
  * `mvn checkstyle:checkstyle`: Generates the visual report at `.code-quality/checkstyle/checkstyle.html`.
* **Reports:** All results (XML and HTML) are saved in the `.code-quality/checkstyle/` folder at the project root.
* **Failure example:** Using incorrect variable names (e.g. `int MiVariable`), missing Javadoc on public classes, or star imports (`import java.util.*`).
* **Documentation:** [Checkstyle Google Style](https://checkstyle.sourceforge.io/google_style.html)

### Prettier (Maven Plugin)
Automatically formats Java code so it complies with the style rules.
* **Runs in:** `process-sources` phase (before compiling).
* **Commands:**
  * `mvn prettier:write`: Formats and overwrites the files with the correct style.
  * `mvn prettier:check`: Only verifies that the code complies with the format without modifying files.
* **Function:** Rewrites your `.java` files to fix indentation and formatting automatically when running `mvn test` or `mvn compile`.
* **Documentation:** [Prettier Java](https://github.com/jhipster/prettier-java)

### Styles (Tailwind in the browser)
There is no frontend build or linter: `@tailwindcss/browser` is vendored at
`src/main/webapp/resources/core/js/tailwind-browser.js` and compiles the
utility classes in the browser, so the app needs no Node, npm, Vite or build
step — the client runs no logic of its own.

### JaCoCo (Code Coverage)
Measures what percentage of the source code is covered by the tests.
* **Runs in:** `test` phase.
* **Configuration:** per-package gates — `domain` and `presentation` must reach
  **100%** line coverage, `infrastructure` **80%**, plus an **80%** global floor
  so future packages can't silently go untested. `config`, the JPA entity and
  the DTOs are excluded (pure boilerplate: constructors/getters/setters).
* **Commands:**
  * `mvn jacoco:report`: Generates the visual report at `.code-quality/jacoco/index.html`.
* **Reports:** All results (binary and HTML) are saved in the `.code-quality/jacoco/` folder at the project root.
* **Failure example:** If the tests cover less than the required line coverage per package, the build fails during verification.
* **Documentation:** [JaCoCo Official Site](https://www.jacoco.org/jacoco/)

#### Command to generate a fresh report:
Since the results are saved in a custom folder outside `target`, it is recommended to delete the previous results to make sure the metric is current:

**On Linux / macOS / Git Bash:**
```shell
rm -rf .code-quality/jacoco && mvn clean test
```

**On Windows (PowerShell):**
```powershell
Remove-Item -Recurse -Force .code-quality/jacoco; mvn clean test
```

**On Windows (CMD):**
```cmd
rd /s /q .code-quality\jacoco & mvn clean test
```

## Project structure

```
src/main/java/com/valhalla/
├── config/           # Spring configuration (web, JPA, security, validation, environment)
├── domain/           # Business logic (services, models, exceptions)
├── infrastructure/   # Persistence (Spring Data JPA repositories)
└── presentation/     # MVC controllers, DTOs, session interceptor and global exception handling

src/main/webapp/
├── WEB-INF/templates/        # Thymeleaf templates
│   ├── layouts/              # base page chrome (head + layout decorator)
│   ├── components/           # reusable fragments (navbar, alerts)
│   └── pages/                # full pages, grouped by feature (auth/)
└── resources/core/js/        # Vendored @tailwindcss/browser (Tailwind compiled in the browser)

src/test/java/               # Unit and integration tests (JUnit, MockMvc, JPA, Playwright)
docker-compose.yml    # Local stack: mysql (dev) + jetty-app (dockerized app)
DockerfileJetty       # Jetty multi-stage image (compiles the WAR inside the image)
DockerfileSQL         # MySQL image (schema + seed initialized by the app at boot)
```

**Authentication:** session-based. On successful login the controller maps the
domain `User` to a `UserSession` DTO (email + role) and stores it in the HTTP
session. `SessionInterceptor` guards `/home` (redirects to `/login` if no
session), and `POST /logout` invalidates the session. New registrations get
`role = USER` and `active = true`; inactive users cannot log in.

**Test conventions:** integration tests use the composed annotations
`@WebIntegrationTest` (MockMvc web + in-memory HSQLDB) and `@JpaIntegrationTest`
(persistence only) instead of repeating the Spring `@ContextConfiguration`
setup. Repository JPA tests use HSQLDB; only the `DataSource` and dialect
differ from production.

**Golden rule:** the frontend ONLY renders with Thymeleaf + Tailwind; ALL business
logic lives in Java (domain/services).

## Technologies:
* Docker
* Java 25 (LTS)
* Spring 6.2.19
* Spring Data JPA 3.5.13
* spring-security-crypto 6.5.11 (BCrypt PasswordEncoder)
* Hibernate Validator 8.0.5.Final + jakarta.el 4.0.2
* Thymeleaf 3.1.5.RELEASE (spring6)
* Embedded Jetty Server EE10 12.0.37
* Servlet API (Jakarta) 6.0.0
* Tailwind CSS 4.3.3 compiled in the browser (vendored `@tailwindcss/browser`; no build step, no Node)
* IntelliJ IDEA | VS Code
* Maven 3.9+ (on JDK 25)
* Spring Test 6.2.19
* Hamcrest 2.2
* JUnit 6.1.2
* Hibernate 6.6.54.Final (org.hibernate.orm)
* MySQL Connector/J 9.7.0
* Mockito 5.23.0
* Playwright 1.61.0
* PMD 7.26.0 (plugin 3.28.0) & CPD
* Checkstyle 13.8.0 (plugin 3.6.0, Google Style)
* Prettier Maven Plugin 0.22 (Prettier-Java 2.5.0)
* JaCoCo 0.8.15

*_Project modified based on: [Spring MVC hello world example (Maven and Thymeleaf)](https://mkyong.com/spring-mvc/spring-mvc-hello-world-example/) _*