# Commands Reference

## Maven

To run Maven commands, either in the IDE's integrated terminal or in another terminal, use the main `mvn` command followed by the command or lifecycle phase to execute.

> Maven runs all the phases prior to the lifecycle phase you specify.

### Lifecycle phases

```shell
# Cleans the target directory from the previous build
mvn clean

# Validates that the project is correct
mvn validate

# Compiles the project source code
mvn compile

# Runs the Java test suites (unit + MockMvc integration)
mvn test

# Packages the compiled code into a JAR or WAR file
mvn package

# Verifies that the package is valid
mvn verify

# Installs the package into the local Maven repository
mvn install
```

### Common combinations

```shell
# Most common — downloads dependencies, compiles, and runs tests
mvn clean install

# Clean build with tests
mvn clean package

# Run tests only
mvn test
```

## Docker

### Local development (recommended)

```shell
# Start MySQL + app with a single command
mvn clean jetty:run -Pdev

# Stop (MySQL keeps running)
Ctrl+C
```

### Docker Compose (full stack in Docker)

```shell
# Start everything in Docker
mvn clean package
docker compose up --build

# Stop and remove containers + volumes
docker compose down --rmi local
```

### Docker manual

```shell
# Create an image named "mysql"
docker build -f DockerfileSQL -t mysql .

# Run MySQL container
docker run --env-file .env --name valhalla-mysql -d -p 3306:3306 mysql

# Create an image named "valhalla"
docker build -f DockerfileJetty -t valhalla .

# Run the app
docker run -p 8080:8080 valhalla
```

### Common commands

```shell
# Show running containers
docker ps

# Show all containers
docker ps -a

# Show all images
docker images

# Show container logs
docker logs <containerId>

# Remove a container
docker rm <containerId>

# Remove an image
docker rmi <imageId>

# Run a container with bash
docker run -it --entrypoint /bin/bash valhalla
```

## Testing

```shell
# Run all Java tests (uses in-memory HSQLDB, no MySQL needed)
mvn test

# Run E2E tests (requires MySQL + app running, or use -Pdev)
mvn test -Dtest="LoginViewE2E"

# Run a specific E2E test
mvn test -Dtest="LoginViewE2E#shouldNavigateToHomeWhenUserExists"
```
