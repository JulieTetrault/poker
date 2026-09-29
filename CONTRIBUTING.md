# Contributing

## Branches

Create a branch before committing:

```sh
git checkout -b chore/repository-workflow
```

Use `feat/`, `fix/`, `chore/`, or `docs/` followed by lowercase words or numbers
separated with hyphens, for example `feat/hand-ranking` or `fix/tie-breaker`.
The same rule is checked before committing and in CI. Direct local commits to
`main` are rejected; CI permits `main` for push/manual runs after merging.
Renovate uses `chore/renovate-...` branches to follow the same convention.

## Local checks

After cloning, install the pinned Java SDK and repository hook:

```sh
sdk env install
sdk env
sh scripts/install-hooks.sh
```

The native Git pre-commit hook checks the branch name and runs Maven `validate`
(Spotless and Checkstyle) against a temporary export of the staged files.
Partially staged files are checked as they will be committed; unstaged edits
remain untouched. The hook selects the staged `.sdkmanrc` SDK from SDKMAN's
installation directory. No separate pre-commit framework is required.

Stage the build configuration, Maven Wrapper, and source files before the
initial commit. When formatting fails, run `./mvnw spotless:apply`, review the
changes, and stage the corrected files. Fix lint errors manually. Tests run
in CI; run `./mvnw verify` locally before opening a PR.

Hook installation is per clone. Git hooks can be skipped by Git options, so
the required CI checks remain the server-side gate. Commit messages should
briefly explain the change; no commit-message format is enforced.

Commits must be signed. Configure your GPG signing key in Git, register its
public key with GitHub, and use `git commit -S` (or enable `commit.gpgsign`).
Keep private keys and passphrases outside the repository.

## Pull requests and documentation

Use the PR template to explain the change, checks, and AI assistance. Update
the README when commands or setup change, and record development steps and
their goals in [the development log](docs/development-log.md).

## Dependency updates

[Renovate configuration](renovate.json) covers Maven dependencies/plugins,
Maven Wrapper, and GitHub Actions, using the recommended preset and manual
merging. Enable the [Renovate GitHub App](https://github.com/apps/renovate) for
this repository to activate scheduled updates; a configuration file alone
does not install the app. Review JDK upgrades manually and keep `.sdkmanrc`
and `pom.xml` compatible with Spring Boot.

## Main-branch protection

The repository owner configured branch protection on GitHub and enabled
required commit signing. Follow the active rules shown on each PR before
merging, and never force-push `main`. GitHub does not let an author approve
their own PR, including owners; the owner can approve a PR authored by
someone else, such as Renovate.
