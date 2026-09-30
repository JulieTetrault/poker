# poker
Simple Java Spring Boot REST API simulating a game of poker 🎴🃏♥️♦️♠️♣️

The [development log](docs/development-log.md) records each step taken with Codex,
including its goal, implementation decisions, and verification.

## API planning and preview

The [requirements](docs/requirements.md), [implementation plan](docs/implementation-plan.md),
and [OpenAPI draft](docs/openapi.json) describe the planned card-shoe API.
Business endpoints are not implemented yet. Preview the contract in Swagger UI:

```sh
python3 -m http.server 8000 --bind 127.0.0.1 --directory docs
```

Open <http://localhost:8000/index.html>. See the
[manual testing guide](docs/manual-api-testing.md) for the optional stateless mock,
example errors, and acceptance scenarios for the real API.

## Toolchain

- Spring Boot 4.1.1
- Java 26 (Temurin 26.0.2), pinned in `.sdkmanrc`
- Maven 3.9.16 through the included Maven Wrapper

Java 26 is the newest Java version supported by the current
[Spring Boot compatibility documentation](https://docs.spring.io/spring-boot/system-requirements.html).

## Setup

Install [SDKMAN!](https://sdkman.io/install/) if needed, then run from the repository root:

```sh
sdk env install
sdk env
sh scripts/install-hooks.sh
./mvnw verify
```

Run `sdk env` when opening a new shell in this project to select the pinned JDK.
Maven Wrapper downloads the pinned Maven distribution automatically on first use.

## Contribution workflow

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch names, hook installation,
and pull requests. Use `feat/`, `fix/`, `chore/`, or `docs/` followed by a
lowercase kebab-case description. Start work with, for example:

```sh
git checkout -b feat/hand-ranking
```

The installed pre-commit hook checks branch names and runs formatting/lint
checks against staged files. It rejects direct commits to `main`. Install it
in every new clone with `sh scripts/install-hooks.sh`.

Signed commits are required by the repository's GitHub protection rules.
Configure your GPG key before committing; see [CONTRIBUTING.md](CONTRIBUTING.md).

[EditorConfig](.editorconfig) keeps basic editor settings consistent, the
[PR template](.github/pull_request_template.md) captures purpose, verification,
and AI assistance, and [AGENTS.md](AGENTS.md) guides Codex's repository work.

[Renovate](renovate.json) is configured for Maven and GitHub Actions updates,
including Maven Wrapper. Enable its GitHub App after the configuration is
merged to activate updates; automatic merging is disabled.

## Formatting and linting

Use [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven) with
google-java-format in AOSP style for four-space Java indentation, and
[Checkstyle](https://checkstyle.org/checks.html) for naming, imports, required
braces, and other code conventions. Both check production and test sources.
This Maven-based setup fits the Java-only project and needs no Node/npm installation.
Checkstyle rules live in [config/checkstyle/checkstyle.xml](config/checkstyle/checkstyle.xml);
formatting and plugin versions are configured in [pom.xml](pom.xml).

```sh
./mvnw spotless:apply  # Fix Java formatting
./mvnw validate        # Check formatting and lint (does not modify source)
```

Fix lint violations reported by Checkstyle manually. The style checks run in
Maven's `validate` phase, so `test`, `package`, and `verify` also enforce them.

## Tests

The Spring Web MVC test starter supplies JUnit Jupiter, AssertJ, Mockito, and
Spring's testing support. Spring Boot manages compatible dependency and test
runner versions. Mockito is configured as a JVM agent for the test process,
and Maven Surefire fails if no tests are found.

```sh
./mvnw test                         # Run tests, including the context smoke test
./mvnw -Dtest=PokerApplicationTests test  # Run one test class
./mvnw verify                       # Check style, run tests, and package the app
```

Place tests under `src/test/java`, mirroring the production package, and name
classes `*Test` or `*Tests` for automatic discovery. Use plain JUnit Jupiter
tests for business logic, `@ExtendWith(MockitoExtension.class)` when mocks are
needed, and AssertJ for assertions. Use `@WebMvcTest` for controller slices and
`@SpringBootTest` for checks requiring the application context.

Test reports are written to `target/surefire-reports/`.

## Continuous integration

The [GitHub Actions workflow](.github/workflows/ci.yml) runs on pushes, pull
requests, and manual dispatch. It selects Temurin from `.sdkmanrc`, caches Maven
dependencies, and runs these steps in order:

1. **Check branch name:** shared with the pre-commit hook; `main` is allowed for
   push/manual runs, while PR source branches must follow the convention.
2. **Check style and lint:** `./mvnw --batch-mode --no-transfer-progress validate`.
3. **Run tests:** `./mvnw --batch-mode --no-transfer-progress test`.

A failed style check prevents the test step from running; any failed check
fails the workflow. The workflow uses read-only repository permissions and a
15-minute timeout. This is the CI portion of the pipeline; deployment awaits
a hosting target and release requirements.

## Run

```sh
./mvnw spring-boot:run
```

The server starts on port 8080. The boilerplate includes Spring Web MVC and a
context startup test; poker endpoints will be added with the project requirements.

To build and run the executable JAR:

```sh
./mvnw package
java -jar target/poker-0.0.1-SNAPSHOT.jar
```
