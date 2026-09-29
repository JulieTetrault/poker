# Repository guidance

## Project context

This is a Java Spring Boot poker REST API. Business requirements have not yet
been defined; do not invent poker rules or add infrastructure without a task
requiring it. Preserve existing user changes.

## Toolchain and checks

- Use the Java version pinned in `.sdkmanrc` (`sdk env`) and Maven Wrapper.
- Run `./mvnw spotless:apply` after editing Java; Spotless owns Java formatting.
- Run `./mvnw validate` for formatting and Checkstyle checks.
- Run `./mvnw verify` for Java/build changes. Use focused tests during development.
- For documentation-only changes, check links and `git diff --check`.
- Use JUnit Jupiter and AssertJ for unit tests, Mockito where needed, and
  Spring test contexts only when the behavior needs them. Name tests `*Test`
  or `*Tests`; test behavior rather than implementation details.

## Collaboration

- Follow `CONTRIBUTING.md` for branch names and hook setup. Do not commit directly
  to `main`, bypass checks, force-push `main`, or merge without authorization.
- Keep changes focused and use the simple PR template when preparing a PR.
- Record each development step, its goal, decisions, AI contribution, and actual
  verification in `docs/development-log.md`. Update README commands when workflows change.
- Report checks honestly, including anything not run or blocked by access.
- Use Renovate for dependency updates; do not introduce Dependabot.
- Keep Maven and GitHub Actions versions explicit. Recheck Spring compatibility
  when updating the JDK, and keep `.sdkmanrc` and `pom.xml` aligned.
