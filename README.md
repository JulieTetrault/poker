# Poker API

A Java Spring Boot REST API for managing games, decks, players, and cards.

## Documentation

- [Requirements](docs/requirements.md): the required game behavior and API operations.
- [Domain UML](docs/domain.md): domain classes, properties, public methods, and key design decisions.
- [Implementation plan](docs/implementation-plan.md): architecture and the seven implementation steps.
- [Development log](docs/development-log.md): how Codex helped establish the project, tools, and planning documents.

## Toolchain

- Java 26 — Temurin 26.0.2, pinned in [.sdkmanrc](.sdkmanrc).
- Spring Boot 4.1.1.
- Maven 3.9.16, provided by the Maven Wrapper.
- SDKMAN for selecting the project's JDK.
- Python 3 for serving the Swagger UI documentation.

## Setup

Install SDKMAN if needed, then run these commands from the repository root:

```sh
sdk env install
sdk env
sh scripts/install-hooks.sh
./mvnw verify
```

Run `sdk env` whenever you open a new shell in the project. Maven Wrapper
downloads the pinned Maven version on first use. The hook checks branch names
and staged Java formatting/lint before commits. See [CONTRIBUTING.md](CONTRIBUTING.md)
for branch, signed commit, and PR conventions.

## Run the app

```sh
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080/api/v1`.

Alternatively, build and run the executable JAR:

```sh
./mvnw package
java -jar target/poker-0.0.1-SNAPSHOT.jar
```

## Run Swagger UI

Serve the documentation in a separate terminal:

```sh
python3 -m http.server 8000 --bind 127.0.0.1 --directory docs
```

Open <http://localhost:8000/index.html> to view the [OpenAPI contract](docs/openapi.json).
Swagger UI loads its assets from a CDN, so internet access is required.

## Run the linter and formatter

Spotless applies google-java-format; Checkstyle checks Java conventions.

```sh
./mvnw spotless:apply  # Apply formatting
./mvnw validate        # Check formatting and lint
```

Fix Checkstyle violations manually. These checks cover application and test
sources and also run before Maven tests and builds.

## Run the tests

Tests use JUnit Jupiter, AssertJ, Mockito, and Spring test support.

```sh
./mvnw test                              # Run all tests
./mvnw -Dtest=GameTest test               # Run one test class
./mvnw verify                            # Check style, run tests, and package
```

Test reports are written to `target/surefire-reports/`.
