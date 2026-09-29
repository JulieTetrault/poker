# Development log

This document records how the project is built with assistance from Codex.
Each entry explains the requested work, its goal, the decisions made, and how
the result was checked. New entries will be added as development progresses.

## Step 1: Setting up the boilerplate

Date: September 29, 2026

### Goal

Create a working, reproducible Java Spring Boot foundation before implementing
the poker requirements. The repository initially contained a README and a
Java `.gitignore`.

### Request and Codex's role

The developer requested Java Spring Boot, the latest suitable SDK version, and
SDKMAN to pin that version. Codex inspected the repository and installed tools,
checked Spring's compatibility documentation and SDKMAN's available releases,
generated the starter project, and verified the build.

Codex selected Maven, the initial `com.example.poker` package, and the minimal
Spring Web MVC dependencies as implementation defaults. These choices can be
revisited when the project's requirements are defined.

### Actions and their purpose

1. **Select compatible versions.** Use Spring Boot 4.1.1, the latest stable
   release found during setup, with Temurin Java 26.0.2. Java 27 was available,
   but Spring Boot's documentation listed support only through Java 26 at the
   time. This kept the project within the documented compatibility range.
2. **Pin Java with SDKMAN.** Add `java=26.0.2-tem` to [`.sdkmanrc`](../.sdkmanrc)
   so contributors can install the required JDK with `sdk env install` and
   select it with `sdk env`. Install that JDK locally and restore the existing
   global Java 21 default after SDKMAN changed it during installation.
3. **Generate the Spring Boot starter.** Use Spring Initializr to create the
   application entry point, application configuration, and [Maven build](../pom.xml).
   Include Spring Web MVC to provide the foundation for the planned REST API.
4. **Include Maven Wrapper.** Pin Maven 3.9.16 so contributors can build with
   `./mvnw` without installing Maven separately.
5. **Add a startup test.** Include `PokerApplicationTests.contextLoads()` to
   check that the Spring application context initializes successfully.
6. **Configure repository hygiene.** Ignore Maven build output, IDE files, and
   operating system metadata. Add line-ending rules for the wrapper scripts
   and make the Unix wrapper executable.
7. **Document local usage.** Add toolchain versions, SDKMAN setup commands,
   build commands, and application launch instructions to the [README](../README.md).

### Verification and outcome

Codex ran `./mvnw -B verify` using the pinned Java 26.0.2 SDK. The build succeeded:

- Application and test sources compiled for Java 26.
- The context startup test passed: one test, zero failures or errors.
- Maven produced the executable `target/poker-0.0.1-SNAPSHOT.jar`.
- `git diff --check` reported no whitespace errors.
- The global SDKMAN Java default was confirmed as the original Java 21.0.6.

The result is a buildable Spring Boot foundation. The startup test verifies
application initialization; poker behavior and API endpoints remain to be
implemented once the requirements are provided.

### References consulted during setup

- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Initializr](https://start.spring.io/)
- [SDKMAN usage and project environments](https://sdkman.io/usage/)

Version decisions above describe the setup date; the linked documentation may
change as new versions are released.

## Step 2: Configuring development tools and continuous integration

Date: September 29, 2026

### Goal

Keep Java code consistently formatted, catch convention violations early,
establish the test tooling, and automatically check contributions on GitHub.

### Request and Codex's role

The developer asked Codex to choose a Java formatter and linter, mentioning
prettier-java as an option, verify the unit test setup, and create a GitHub
Actions pipeline checking style followed by tests. Codex researched the tools,
selected the configuration, updated the build and workflow, and documented the
local commands in the README.

### Decisions and their purpose

1. **Formatting: Spotless 3.10.3 with google-java-format 1.36.1.** This provides
   automatic formatting and a check-only command through Maven. Prettier Java
   was considered; its Node/npm tooling adds another ecosystem to this
   Java-only repository. Spotless with a Java formatter fits the existing build.
2. **Linting: Checkstyle 14.3.0 through Maven Checkstyle Plugin 3.6.0.** A small
   [rule set](../config/checkstyle/checkstyle.xml) checks imports, naming,
   required braces, empty statements, and paired `equals`/`hashCode` methods.
   Layout is delegated to the formatter to avoid conflicting formatting rules.
   These are source-level convention checks, not comprehensive bug detection.
3. **Enforce checks locally.** Both tools run during Maven's `validate` phase
   and cover application and test sources. Normal builds fail on violations.
   `spotless:apply` is the explicit formatting command; checks never rewrite code.
4. **Use the existing Spring test dependencies.** The Web MVC test starter
   already supplies JUnit Jupiter, AssertJ, Mockito, and Spring test support.
   Spring Boot continues to manage their versions. Surefire now fails when
   no tests are discovered, and Mockito starts as an explicit Java agent to
   avoid relying on dynamic attachment. Maven Dependency Plugin resolves the
   agent JAR from the active dependency set and local cache location.
5. **Define test conventions.** Document `*Test`/`*Tests` discovery, plain unit
   tests, Mockito-based tests, controller slices, context tests, and reports.
   The existing context smoke test remains the initial test; poker unit tests
   will be written alongside business behavior.
6. **Add GitHub Actions.** The [CI workflow](../.github/workflows/ci.yml) runs
   on pushes, pull requests, and manual dispatch. It reads the Java version
   from `.sdkmanrc`, caches Maven dependencies, checks style, then runs tests.
   It uses read-only repository permissions, a timeout, and cancellation of
   superseded runs. Deployment configuration awaits release and hosting details.

### Verification and outcome

- Applied the formatter to the existing application and test sources.
- Ran a clean Maven `verify` with Java 26.0.2: formatting and lint passed,
  the context test passed, and the executable JAR was built.
- Temporarily introduced an unformatted test source, a method-name lint
  violation, and a failing JUnit test. Each corresponding check failed as
  expected. Removed the temporary source and ran the clean build afterward.
- Confirmed that the workflow YAML parses and its steps are ordered correctly,
  all local documentation links resolve, and `git diff --check` passes.

The workflow has been validated locally but has not yet run on GitHub. Its
first hosted run will occur after these files are pushed to the repository.

### References consulted

- [Spotless Maven integration](https://github.com/diffplug/spotless/tree/main/plugin-maven)
- [Prettier Java](https://github.com/jhipster/prettier-java)
- [Checkstyle checks](https://checkstyle.org/checks.html)
- [Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/usage.html)
- [Spring Boot testing](https://docs.spring.io/spring-boot/reference/testing/)
- [Mockito documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html)
- [GitHub Actions Java setup](https://github.com/actions/setup-java)

## Step 3: Establishing the contribution workflow

Date: September 29, 2026

### Goal

Catch branch-name and lint errors before commits, keep editor settings
consistent, document expectations for contributors and Codex, and prepare
dependency updates and protected development on `main`.

### Request and Codex's role

The developer requested local and CI branch checks, a pre-commit lint check,
EditorConfig, a simple PR template, Renovate instead of Dependabot, and
`AGENTS.md`. They also requested protected `main`, blocked force pushes, and
one approval while allowing the owner to approve their own work. Codex
implemented the repository files, installed the local hook, tested it, and
checked GitHub's approval constraints.

### Actions and their purpose

1. **Share a branch validator.** `scripts/check-branch-name.sh` enforces
   `feat/`, `fix/`, `chore/`, or `docs/` with lowercase kebab-case descriptions.
   Both the local hook and CI call it. CI allows `main` only on push/manual
   runs, so merged changes can be checked. This checks branch names at commit
   time; commit messages have no additional format requirement.
2. **Install a native pre-commit hook.** `.githooks/pre-commit` exports the
   Git index to a temporary directory and runs Maven `validate` using the
   staged SDKMAN pin. This checks the exact staged Java sources, including
   partially staged files, without stashing or changing the working copy.
   `scripts/install-hooks.sh` sets the repository-local `core.hooksPath` and
   refuses to replace a different configured hooks path. New clones must
   run the installer too.
3. **Add editor and review defaults.** `.editorconfig` defines UTF-8, line
   endings, final newlines, and indentation consistent with Java formatting
   and the existing Maven POM. The PR template asks only for purpose,
   verification, and AI assistance.
4. **Document contributor and agent expectations.** `CONTRIBUTING.md` explains
   branches, hooks, and review workflow. `AGENTS.md` records the toolchain,
   verification commands, documentation requirements, and the fact that poker
   business requirements are still pending.
5. **Configure Renovate.** `renovate.json` enables the recommended preset for
   Maven, Maven Wrapper, and GitHub Actions. Update branches use the same
   `chore/` convention and automatic merging is disabled. The GitHub App
   must be enabled for the repository before updates can run.
6. **Investigate main-branch protection.** GitHub does not allow a PR author
   to approve their own PR, including repository owners. An owner bypass
   would skip the approval requirement rather than satisfy it. Codex asked
   the developer to choose between that exception and requiring a different
   reviewer, and requested GitHub authentication to administer the repository.

### Verification and current status

- Installed `.githooks` as this clone's hooks path and checked shell syntax.
- Passed 13 branch-validation cases, including invalid prefixes, uppercase,
  empty descriptions, nested names, and the special handling of `main`.
- Tested the hook in an isolated repository: valid staged sources passed;
  a staged lint violation failed even with a corrected unstaged file. The
  index and working file were unchanged after the failed check.
- Confirmed real Git commits invoke the installed hook in a temporary
  repository: a valid branch passed and a commit to `main` was rejected.
- Parsed the CI YAML and confirmed the branch gate precedes the Java checks.
- Parsed Renovate JSON and checked its option names against the current
  published schema. Full Renovate execution has not been run.

At the end of this step, GitHub protection had not been changed: administrative
authentication and the approval-policy decision were pending. The owner later
configured protection and required signing directly on GitHub (see Step 4).
Renovate activation and the hosted CI run remained pending at this point;
local files do not activate those services.
GitHub CLI was installed locally to support authenticated administration.

### References

- [GitHub review restrictions](https://docs.github.com/en/pull-requests/how-tos/review-pull-requests/reviewing-proposed-changes-in-a-pull-request)
- [GitHub rulesets](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/available-rules-for-rulesets)
- [Renovate configuration](https://docs.renovatebot.com/configuration-options/)

## Step 4: Preparing the first boilerplate PR

Date: September 29, 2026

### Goal and request

The developer configured branch protection and a GPG signing key, then asked
Codex to review `.gitignore`, create the first boilerplate commit on a `chore/`
branch, and push it for review in a PR.

### Actions and their purpose

- Reviewed all files included in the boilerplate change before staging.
- Extended `.gitignore` to cover IDE output/settings, heap dumps, OS metadata,
  and local `.env` values while allowing `.env.example` and a Maven Wrapper
  JAR if one is needed in future. Build output and local IDE state stay out
  of version control.
- Updated contribution instructions to reflect owner-configured GitHub
  protection and required signing. Private signing material stays outside
  the repository.
- Selected `chore/project-boilerplate` for the implementation branch and
  `chore: set up Spring Boot project boilerplate` for the signed commit and PR.

### Verification before committing

- Maven `verify` passed on the pinned JDK: formatting, lint, the context
  startup test (one test, zero failures/errors), and executable JAR packaging.
- Checked ignore rules for build/IDE output and local environment files;
  confirmed explicit exceptions for shared environment examples and the wrapper JAR.
- Checked documentation links and `git diff --check`.
- Confirmed local Git signing is enabled and retained the installed pre-commit
  checks for the commit. GitHub CI will run after the branch is pushed.

## Step 5: Check Spotless versions and reported warning

Date: September 29, 2026

### Goal and decisions

The developer reported an old-version warning and requested the latest
Spotless versions before continuing the boilerplate work. Codex inspected
`pom.xml` and checked the upstream release lists:

- [Spotless Maven releases](https://github.com/diffplug/spotless/releases):
  latest Maven plugin version is 3.10.3, already pinned in the project.
- [google-java-format releases](https://github.com/google/google-java-format/releases):
  latest version is 1.36.1, also already pinned.

No dependency or build changes were needed. Codex performed the release
comparison and local validation and recorded the results here. The reported
warning was not reproduced; its exact text is needed for further diagnosis.

### Actual verification

- Selected Java 26.0.2-tem using `sdk env` and ran `./mvnw validate`: passed,
  with two Java files clean and zero Checkstyle violations. Maven emitted
  no Spotless warning. SDKMAN reported unavailable internet access but
  successfully selected the installed JDK.
- Checked the upstream release links and ran `git diff --check`: passed.
- Did not run `verify` or `spotless:apply`: no build or Java files changed.

## Step 6: Publish the boilerplate for review

Date: September 29, 2026

### Goal, decisions, and AI contribution

After resolving GPG setup, the developer authorized a signed boilerplate
commit, branch push, and pull request. Codex reviewed the staged application,
build configuration, hooks, CI, and documentation on
`chore/project-boilerplate`, retaining the existing work and including the
Spotless version verification. The PR uses the repository template and targets
`main`; merging is outside this request.

### Verification before publication

- `sdk env` selected Java 26.0.2-tem; `./mvnw verify` passed formatting,
  Checkstyle, the context smoke test (one test, zero failures/errors), and
  executable JAR packaging.
- Branch naming and both staged and unstaged `git diff --check` passed.
- Confirmed the repository pre-commit hook remains installed at `.githooks`.
- Confirmed no existing open PR for this branch. Hosted CI results will be
  available on the PR after publication.
- The pre-commit hook's fresh staged-file validation passed and reproduced
  the reported warning: Spotless's `ModuleHelper` in `spotless-lib` 4.10.3
  calls the terminally deprecated `sun.misc.Unsafe::staticFieldBase` method.
  The earlier cached validation did not emit it. This warning occurs with
  the latest Spotless release; it does not indicate an outdated plugin or
  fail the checks. No warning suppression was added.
