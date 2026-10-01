# Development log

This log summarizes the main steps I took with Codex to establish the project
and plan its implementation. I directed the work, reviewed the proposed choices,
and refined the results. Codex helped create the files, configure tools, and
verify the setup.

## 1. Set up the project boilerplate

I asked Codex to create a Java Spring Boot foundation with a reproducible local
toolchain before implementing the game.

Codex generated the starter application, Maven build, wrapper scripts, and
initial configuration. We selected Spring Boot 4.1.1 and Java 26.0.2 after
checking compatibility. SDKMAN pins the JDK in [`.sdkmanrc`](../.sdkmanrc), and
the Maven Wrapper pins Maven so contributors can use the same build tools
without separate Maven installation.

The setup also included a context startup test, Git ignore rules, and
[README](../README.md) instructions for selecting Java, building, and running
the application. The initial Maven verification passed and produced an
executable JAR, establishing a working baseline for domain development.

## 2. Set up development tools and contribution conventions

I used Codex to establish consistent code quality checks and a review workflow.
We chose Spotless with google-java-format for formatting and Checkstyle for
source conventions. Both run during Maven validation; formatting is applied
explicitly with `./mvnw spotless:apply`.

Codex configured [GitHub Actions](../.github/workflows/ci.yml) to check branch
names, formatting, lint, and tests. Local verification uses the pinned JDK and
Maven Wrapper, keeping the developer workflow aligned with CI.

We documented branch naming, signed commits, and PR expectations in
[CONTRIBUTING.md](../CONTRIBUTING.md). A small PR template records the purpose,
verification, and AI assistance. The native pre-commit hook checks the branch
and validates an export of the staged files, so partially staged changes are
checked as they will be committed. EditorConfig and repository guidance keep
editor settings and subsequent Codex work consistent.

Codex tested the checks with temporary formatting, lint, and test failures,
then removed those examples. Branch validation and hook behavior were checked
locally. The setup's clean Maven verification passed; these historical results
do not establish the status of later application changes.

## 3. Create the domain and API implementation documents

I asked Codex to translate the supplied game requirements into three documents,
then reviewed and simplified them as the design evolved:

- [domain.md](domain.md) describes the models, their public operations, and
  relationships. It explains Shoe's undealt state and delegation to dealing,
  counting, and shuffling services, including Fisher–Yates shuffle.
- [openapi.json](openapi.json) defines routes, request and response shapes,
  status codes, and happy-path examples, providing a shared contract for implementation
  and endpoint testing.
- [implementation-plan.md](implementation-plan.md) explains the architecture
  and seven implementation stages, from domain models to request validation.

The main architectural decision was to separate domain behavior from HTTP and
persistence concerns, using repository interfaces and explicit entity/response
mappers. I guided revisions to keep the documents focused on relevant decisions
rather than exhaustive implementation instructions.
