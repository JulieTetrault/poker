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

## Step 7: Plan the card-shoe API and document its contract

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer supplied `docs/requirements.md` and requested a criterion-by-
criterion feature plan, endpoint/error design, and OpenAPI/Swagger documentation
that can be used before endpoints exist. Codex preserved the requirements and
created `docs/implementation-plan.md`, an OpenAPI 3.1.1 draft, a separate Swagger
UI preview, and a manual testing guide. README links and preview commands were
added; AGENTS.md now points to the requirements and distinguishes draft decisions.

The plan covers 12 operations, a common Problem Details error catalogue, card
conservation and concurrency, deterministic shuffle tests, and delivery slices.
Partial dealing, physical deck ownership, player removal/discard behavior and
other unspecified policies are explicitly proposed defaults pending review.
Codex asked for clarification on the three primary rule choices; no reply had
arrived when this draft was prepared. No game implementation or Maven dependency
was added. Swagger UI is pinned to 5.33.0 and the optional stateless Prism mock to
5.16.0. Future Spring Swagger integration is planned, not implemented.

### Actual verification

- OpenAPI validation passed with `openapi-spec-validator` 0.9.0 in a temporary
  environment; 93 request/response examples passed JSON Schema validation.
- Started the separate documentation server and fetched the Swagger HTML and
  OpenAPI file over local HTTP. Browser rendering was not visually tested.
- The initial Prism invocation failed with the active Node 22.7 runtime. Prism
  requires Node >=24.18.0; an isolated temporary Node 24.18.0 invocation succeeded.
  Both the prerequisite and temporary-runtime command are documented.
- Exercised 69 success/error mock examples across all 12 operations, checking
  expected status/body, CORS and empty 204 responses. These are stateless example
  checks, not implementation tests. The name-validation message was subsequently
  clarified and all 93 schema examples revalidated.
- Checked local documentation links and the referenced upstream documentation;
  `git diff --check` passed. Maven checks were not rerun because Java/build files
  were unchanged. No endpoints, real game acceptance tests, or Spring Swagger
  integration were claimed as implemented or tested.

## Step 8: Simplify the implementation plan and align contract examples

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer supplied the domain model and revised API routes, requested a
simpler implementation plan organized by development steps, and authorized
correcting OpenAPI examples to match their schemas. Codex rewrote the plan into
seven steps: domain classes, request/response DTOs, controllers, storage,
services, centralized errors, and verification. It preserves all existing error
codes and follows the domain's `Shoe`, `Hand`, card UUIDs, and derived rank values.

Codex updated 87 OpenAPI response examples, including full game/deck payloads,
card IDs, simplified errors, player arrays, suit counts, and deal payloads.
Paths, schemas, methods, and statuses were preserved. Examples match the current
schema references even where those references appear inconsistent with the
operation's purpose. The plan records those gaps, malformed array item
constraints, game-name input, 204 response content, and the error format for
resolution before implementation. Unconfirmed business policies remain proposals.
Existing user changes and other documentation were preserved; no Java, build,
or infrastructure changes were made.

### Actual verification

- OpenAPI 3.1 validation passed using the existing temporary validation
  environment (`openapi-spec-validator` 0.9.0).
- All 91 request/response payload examples passed JSON Schema validation with
  format checks. Array element constraints remain limited by the existing schemas.
- Local Markdown links in README, AGENTS.md, the implementation plan, and the
  manual testing guide resolve; `git diff --check` passed.
- Maven checks, mock execution, and API behavior tests were not run: only
  documentation and contract examples changed, and game endpoints are not implemented.

## Step 9: Record confirmed API behavior and use the revised DTOs

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer confirmed partial/empty deals, cascading game deletion, discarded
removed hands, exclusive deck/shoe ownership, UUID tie ordering, named game
creation, and response behavior for deck/player additions. The developer also
provided updated request/response schemas and requested endpoint examples
matching them. Codex incorporated these decisions into the seven-step plan and
updated the OpenAPI endpoint references and examples without changing the schema
section. Game creation now accepts the required named request; add-deck returns
204 without content; add-player uses 201 with its ID/name response. Deal examples
include 13 requested with 10 available and an empty shoe, returning only the
actual dealt and remaining counts. Per-face counts and player-card arrays now
reference the dedicated response schemas. Confirmed policies are reflected in
operation descriptions; storage and initial card order remain proposals.

### Actual verification

- OpenAPI 3.1 validation passed in the existing temporary validation environment.
- All 97 request/response payload examples passed JSON Schema validation with
  format checks, including the developer's corrected array element schemas.
- Confirmed the complete schema section is unchanged from the start of this step.
- Local documentation links resolve and `git diff --check` passed.
- No Java/build changes: Maven checks and runtime API tests were not run.

## Step 10: Prepare the planning documentation PR

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested staging all planning changes, a commit prefixed by
`docs`, and a PR targeting `main`. Codex moved the changes from the previously
merged boilerplate branch to `docs/api-planning`, based on the latest
`origin/main`, and corrected the preview URLs to the existing `docs/index.html`.
All existing planning changes are included.

### Actual verification

- Local Markdown file links resolve, the OpenAPI JSON parses, and
  `git diff --check` passed.
- The signed commit's repository pre-commit hook passed staged Maven
  `validate`: Spotless passed and Checkstyle reported zero violations.
- Maven `verify` was not rerun for these documentation-only changes.

## Step 11: Prepare the domain implementation branch

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested a feature branch for implementation-plan step 1,
creating the domain classes. Codex created `feat/domain-classes` from local
`main`, preserving the developer's revised architecture, package structure,
and implementation plan byte for byte as uncommitted changes. Domain
implementation has not started; no commit or push was requested.

### Actual verification

- Confirmed the active branch and the unchanged implementation-plan Git blob hash.
- All local links in the implementation plan resolve.
- `git diff --check` reports 20 existing trailing-whitespace lines in the
  revised plan, used as Markdown hard breaks; preserved the developer's content.
- Maven checks were not run because no Java or build files changed.

## Step 12: Implement domain classes and unit tests

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested implementation-plan step 1 and unit tests on
`feat/domain-classes`. Codex implemented `Game`, `Shoe`, `Deck`, `Card`,
`Player`, `Hand`, `Suit`, and `Rank` in `com.example.poker.domain.model`,
using only Java types and no framework annotations or new dependencies.
The developer's revised implementation plan remains unchanged.

Decks generate 52 physical cards with unique UUIDs and original deck IDs.
Attachment is permanent; duplicate attachment and foreign-game players are
rejected before mutation. The shoe tracks undealt cards separately from the
original deck collections, supports partial/empty deals, returns ordered
suit counts including zeros, and implements Fisher–Yates with an injectable
`RandomGenerator`. Collection access returns immutable snapshots.
Hand totals are derived from rank values. Player removal clears the hand
without replenishing the shoe. Player ordering uses descending hand totals
and ascending canonical UUID text for ties (including UUIDs with the high bit
set). Initial deck iteration order is an implementation detail, not a confirmed
API guarantee. HTTP field validation and persistence remain later steps.

Codex added 16 JUnit Jupiter/AssertJ unit test cases without Spring contexts,
covering deck composition, values, ownership, rejected mutations, collection
protection, partial and empty deals, independent games, ordering, discards,
suit counts, controlled Fisher–Yates behavior, and complete one/two-deck deals.

### Actual verification

- Selected Java `26.0.2-tem` with `sdk env` and used the Maven Wrapper.
- `./mvnw spotless:apply` passed.
- `./mvnw -Dtest=DomainTest test` passed: 16 cases, zero failures/errors/skips.
- `./mvnw validate` passed: formatting clean and zero Checkstyle violations.
- `./mvnw verify` passed: 17 tests total, including the existing application
  context test, and executable JAR packaging succeeded.
- New Java files and the development-log changes pass whitespace checks.
  The full diff still reports the 20 preserved Markdown hard-break lines in
  the developer's revised implementation plan.
- No commit or push was performed.

## Step 13: Organize unit tests by domain class

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested separate test classes for each domain type. Codex
replaced `DomainTest` with `DeckTest`, `RankTest`, `HandTest`, `PlayerTest`,
`ShoeTest`, and `GameTest`. The combined hand/player test now has separate
cases: direct hand totals and collection protection in `HandTest`, and card
receipt and derived player values in `PlayerTest`. Existing coverage is
preserved; domain production code and the revised plan are unchanged.

### Actual verification

- Used Java `26.0.2-tem` selected with `sdk env` and the Maven Wrapper.
- `./mvnw spotless:apply` and `./mvnw validate` passed, with zero lint violations.
- `./mvnw clean verify` passed: 17 domain cases plus the existing application
  test, zero failures/errors/skips. The clean build removed the obsolete
  compiled `DomainTest` before running the reorganized suite.
- Changed test files and development-log whitespace checks passed. The
  existing implementation-plan Markdown hard breaks remain unchanged.

## Step 14: Adopt given/when/then test names

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested the `given__when__then` naming convention. Codex
renamed every test method to `givenCondition__whenAction__thenExpectedResult`,
including the existing application context test. Assertions and production
behavior are unchanged. Checkstyle now accepts this exact naming pattern
alongside conventional camelCase methods. CONTRIBUTING.md records the test
naming and per-class organization conventions.

### Actual verification

- Java `26.0.2-tem` selected with `sdk env`; Maven Wrapper used.
- `./mvnw spotless:apply`, `./mvnw validate`, and `./mvnw verify` passed.
- All 18 tests passed, with zero failures/errors/skips and no lint violations.
- Verified all 16 test methods (18 cases including parameterized cases)
  follow the naming convention; changed files pass whitespace checks.
- CONTRIBUTING.md local links resolve. The revised implementation plan
  remains unchanged, including its previously noted Markdown hard breaks.

## Step 15: Use four-space Java indentation

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested four-space indentation and code reformatting. Codex
configured Spotless's existing google-java-format formatter to use AOSP
style, aligned Java EditorConfig indentation to four spaces, and reformatted
all 16 production/test Java files with Spotless. README documents the selected
style; formatter versions, behavior, and the revised plan are unchanged.

### Actual verification

- Used Java `26.0.2-tem` selected with `sdk env` and the Maven Wrapper.
- `./mvnw spotless:apply` reformatted all 16 Java files successfully.
- `./mvnw validate` passed with zero Checkstyle violations.
- `./mvnw verify` passed: all 18 tests, zero failures/errors/skips, successful
  executable JAR packaging.
- Changed files pass whitespace checks and README local links resolve.
  The preserved implementation-plan hard breaks remain unchanged.

## Step 16: Create aggregates through domain factories

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer revised the architecture to assign identity generation and valid
initial state to domain factories, with separate creation and rehydration
paths, and requested implementation with unit tests. Codex added
`GameFactory`, `DeckFactory`, and `PlayerFactory` in `domain.factory`, plus
`IdGenerator` and its production `UUIDGenerator` implementation. Player has
its own factory because player creation is a separate application operation.
No additional child factories are needed: GameFactory creates the shoe,
DeckFactory creates all 52 cards, and PlayerFactory creates the empty hand.

Models now receive existing IDs and state. Game construction accepts its shoe
and players; deck construction accepts attachment and its original cards.
Shoe and player/hand construction support restored undealt-card and hand
state without recreating cards. Constructors retain ownership checks and
protect collections; decks require 52 distinct faces and card identities.
Creation factories enforce the documented nonblank, at-most-100-character
names before requesting IDs. Rehydration uses persisted IDs/state and never
calls the ID generator or resets attachment/dealt/discarded state.

Codex retained the developer's `Deck.setShoeId` API and revised plan, moved
card generation out of Deck, adapted existing domain tests to factory-created
games/decks, and added per-class factory and UUID generator tests. New factory
tests use deterministic generators, given/when/then method names, nested
creation/rehydration groups, and display names. Four-space formatting remains.
No framework wiring, persistence adapters, or new dependencies were added.

### Actual verification

- Java `26.0.2-tem` selected with `sdk env`; Maven Wrapper used.
- `./mvnw spotless:apply` passed.
- `./mvnw -Dtest='*FactoryTest,UuidGeneratorTest' test` passed: 16 cases,
  zero failures/errors/skips.
- `./mvnw validate` passed with zero Checkstyle violations.
- `./mvnw verify` passed: all 34 tests and executable JAR packaging.
- Verified production `UUID.randomUUID()` occurs only in `UUIDGenerator`;
  domain models contain no UUID generation calls.
- Full `git diff --check`, new Java whitespace checks, and revised-plan local
  links passed. The revised plan's Git blob hash is unchanged from this step's
  initial inspection (`f1222a421c316e351fff1c4ec794fe04d46269d6`).
- No commit or push was performed.

## Step 17: Inject ShoeFactory into GameFactory through Spring

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested ShoeFactory and Spring injection into GameFactory.
Codex added ShoeFactory, which requests an ID from IdGenerator and constructs
an empty shoe for the supplied game. GameFactory now receives ShoeFactory
through its single constructor and delegates shoe creation. GameFactory,
ShoeFactory, and UuidGenerator are Spring components; Spring automatically
injects their sole constructors. Model classes retain no Spring annotations.

Codex updated constructor call sites, added per-class ShoeFactory unit tests
and a delegation test using separate deterministic generators, and extended
the existing Spring context test to exercise injected factories. The user's
recent simplified Deck/Game/Player factory changes are preserved. A malformed
existing duplicate-deck assertion in ShoeTest prevented Java formatting;
Codex repaired it using the user's current Deck constructor. Spotless also
formatted existing user edits and removed unused test imports.

### Actual verification

- Selected Java `26.0.2-tem` with `sdk env` and used the Maven Wrapper.
- Initial checks stopped at the existing ShoeTest syntax error; after repairing
  it, `./mvnw spotless:apply` and `./mvnw validate` passed with zero lint violations.
- `./mvnw verify` compiled successfully and ran 33 cases: 24 passed, nine failed,
  zero errors/skips. Both new ShoeFactory cases, GameFactory delegation, and
  the Spring context wiring test passed.
- The nine remaining failures reflect existing test expectations versus the
  user's simplified implementations: six game/player name-validation cases,
  two deck factory identity/duplicate-ID cases, and deck collection protection.
  These behaviors were not restored or tests removed as part of shoe injection.
- Whitespace checks passed. No commit or push was performed.

## Step 18: Align tests with simplified models and factories

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer intentionally simplified factories/models while deciding which
business logic belongs in services or the domain, and requested test cleanup
with clearly separated GIVEN, WHEN, THEN blocks. Codex revised the per-class
tests to cover only current behavior: factory identities and initial state,
52-card deck composition, shoe assignment, hand accumulation and values,
player card receipt, game membership/removal and ordering, rank values, UUID
generation, and Spring factory wiring.

Obsolete tests and references for validation, dealing, shuffling, discarded
hands, collection protection, and removed rehydration APIs were removed.
DeckFactory tests no longer expect card IDs to come from IdGenerator: the
current simplified factory generates those separately. Inputs use fixed IDs
where useful. Every test body separates setup, action, and assertions with
`// GIVEN`, `// WHEN`, and `// THEN`; CONTRIBUTING.md records this convention.
Factory tests retain nested groups and display names. Production behavior is
preserved; Spotless only reformatted the developer's Card record declaration.
Deferred business rules are not claimed as implemented or verified.

### Actual verification

- Java `26.0.2-tem` selected with `sdk env`; Maven Wrapper used.
- `./mvnw spotless:apply` and `./mvnw validate` passed with zero lint violations.
- `./mvnw clean verify` passed: 15 tests, zero failures/errors/skips, executable
  JAR packaging succeeded. Cleaning removed stale compiled test classes.
- All 15 test methods have ordered GIVEN/WHEN/THEN blocks and follow the
  given/when/then naming convention.
- Java whitespace checks, `git diff --check`, and CONTRIBUTING.md local links
  passed. No commit or push was performed.

## Step 19: Add builder fixtures with Java Faker defaults

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested PlayerFixture and CardFixture builders for later use.
Codex added both to `src/test/java/com/example/poker/fixture` with `builder()`,
fluent `with...` overrides, and `build()` returning domain objects. Java Faker
provides default UUIDs, player names, and random suit/rank selections. Players
start with an empty hand unless `withCards(...)` supplies cards. Defaults are
chosen once per builder. Java Faker `1.0.2` is explicitly pinned and test-scoped
in pom.xml, using the coordinates documented by the library's official README.
Existing tests were not migrated to fixtures; no new fixture tests were added.
Spotless formatted existing developer edits alongside the fixtures.

### Actual verification

- Used Java `26.0.2-tem` selected with `sdk env` and the Maven Wrapper.
- `./mvnw spotless:apply` passed.
- `./mvnw validate` and `./mvnw verify` stopped at three existing Checkstyle
  method-name violations in ShoeTest, ShoeFactoryTest, and UUIDGeneratorTest:
  their current `when...__then...` names do not match the configured convention.
  The developer's test names were preserved; tests did not run.
- Separate `./mvnw compiler:testCompile` passed, compiling all 14 test source
  files including both fixtures. This checks compilation only, not full build
  verification or fixture execution.
- `git diff --check` and new fixture whitespace checks passed.
- No commit or push was performed.

## Step 20: Add ShoeFixture

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested a shoe fixture alongside the existing player/card
fixtures. Codex added ShoeFixture with Faker-generated shoe/game UUIDs,
fluent `withId`, `withGameId`, and `withDecks` overrides, and `build()`.
Both construction with `new ShoeFixture()` and `ShoeFixture.builder()` are
supported. The default shoe has no decks; provided decks are copied into each
built shoe's collection using the current simplified model, without adding
attachment rules. Existing tests were not migrated. Spotless also formatted
the developer's current GameTest edits.

### Actual verification

- Java `26.0.2-tem` selected with `sdk env`; Maven Wrapper used.
- `./mvnw spotless:apply` passed.
- `./mvnw validate` and `./mvnw verify` stopped at the same three existing
  Checkstyle test-name violations noted in step 19; tests did not run.
- Separate `./mvnw compiler:testCompile` passed for all 15 test source files,
  including ShoeFixture. This checks compilation only.
- `git diff --check` and ShoeFixture whitespace checks passed.
- No commit or push was performed.

## Step 21: Mock IdGenerator in factory tests

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested mocked IdGenerator dependencies in all factory tests.
Codex replaced lambda generators in DeckFactoryTest, PlayerFactoryTest,
ShoeFactoryTest, and GameFactoryTest with Mockito mocks and BDD `given(...)`
stubbing. GameFactoryTest uses separate mocks for game and shoe identity
while retaining the real injected ShoeFactory. Existing identity and initial
state assertions remain; the updated bodies separate GIVEN, WHEN, and THEN.
ShoeFactoryTest's updated name reflects its mocked dependency and matches the
configured convention. UUIDGeneratorTest still tests the real generator.
No production changes or dependencies were added. Spotless also formatted
the developer's current GameTest and HandTest edits.

### Actual verification

- Used Java `26.0.2-tem` selected with `sdk env` and Maven Wrapper.
- `./mvnw spotless:apply` passed.
- `./mvnw validate` and `./mvnw verify` stopped at two existing Checkstyle
  naming violations in ShoeTest and UUIDGeneratorTest; full tests/build did
  not run. These other test names were preserved.
- Separate focused execution using `./mvnw compiler:testCompile
  dependency:properties surefire:test -Dtest='*FactoryTest'` passed all four
  factory cases, zero failures/errors/skips. This is focused verification,
  not a passing full lifecycle build.
- New factory test whitespace checks and `git diff --check` passed.
- No commit or push was performed.

## Step 22: Use Mockito extension and annotation-based factory injection

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer provided a preferred MockitoExtension, @Mock, and @InjectMocks
structure. Codex applied it to all four factory test classes, retaining the
repository's `*Test` names, nested creation groups, and GIVEN/WHEN/THEN blocks.
Mocks and factories are now fields initialized by Mockito. GameFactoryTest
also mocks its ShoeFactory dependency and stubs it with a ShoeFixture-built
shoe, checking that the returned game contains that exact shoe. Production
classes, other tests, and dependencies are unchanged.

### Actual verification

- Java `26.0.2-tem` selected with `sdk env`; Maven Wrapper used.
- `./mvnw spotless:apply` passed.
- `./mvnw validate` and `./mvnw verify` stopped at the existing ShoeTest and
  UUIDGeneratorTest naming violations; full lifecycle verification remains
  unsuccessful.
- Separate focused execution using `./mvnw compiler:testCompile
  dependency:properties surefire:test -Dtest='*FactoryTest'` passed all four
  cases, zero failures/errors/skips, including Mockito initialization of
  fields used by nested tests.
- Factory test whitespace checks and `git diff --check` passed.
- No commit or push was performed.

## Step 23: Make the GIVEN test-name prefix optional

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested an optional GIVEN part in test method names. Codex
updated the Checkstyle MethodName pattern to accept both
`givenCondition__whenAction__thenExpectedResult` and
`whenAction__thenExpectedResult`, preserving ordinary camelCase method names.
CONTRIBUTING.md now documents the optional prefix. Existing user changes and
test bodies were preserved.

### Actual verification

- Used Java `26.0.2-tem` selected with `sdk env` and Maven Wrapper.
- `./mvnw validate` passed with zero Checkstyle violations, including the
  existing ShoeTest and UUIDGeneratorTest names without GIVEN prefixes.
- `./mvnw verify` ran 15 tests: 14 passed and one failed in GameTest at
  line 70 because the actual player list contained an extra element.
  Full build verification therefore failed on an unrelated assertion.
- No Java files were edited; Spotless formatting checks passed in both runs.
- `git diff --check` passed.
- No commit or push was performed.

## Step 24: Combine domain implementation steps and prepare the domain PR

Date: September 29, 2026

### Goal, decisions, and AI contribution

The developer requested combining implementation-plan steps 1 and 2, then
committing the domain models and opening a PR. Codex combined model and factory
creation into step 1, retained factory guidance as a subsection, and renumbered
the remaining steps. Existing model, factory, fixture, build, and documentation
changes are included together on `feat/domain-classes`.

Codex corrected GameTest's ordering test name to describe the existing name
comparison for equal hand totals; production behavior and assertions remain
unchanged. UUID tie-breaking remains documented work for the domain-behavior
step. Rehydration, full domain rules, persistence, and HTTP endpoints remain
future implementation work.

### Actual verification

- Selected Java `26.0.2-tem` with `sdk env` and used Maven Wrapper.
- Initial validate/verify attempts stopped at GameTest formatting; corrected
  with `./mvnw spotless:apply`.
- `./mvnw validate` passed with zero Checkstyle violations.
- `./mvnw clean verify` passed: 15 tests, zero failures/errors/skips, executable
  JAR packaging succeeded.
- Implementation-plan local links resolve; `git diff --check` passed.
