# AGENTS.md

This file provides guidance to coding agents when working with code in this repository.

## Project Overview

Gradle plugin that simplifies publishing Android library artifacts to Automattic's S3 Maven repository. Provides two plugin variants:

- `com.automattic.android.publish-to-s3` — publishes with sources
- `com.automattic.android.publish-to-s3-without-sources` — publishes without sources

## Build Commands

All commands run from the repository root. The project uses a **composite build** — the root project includes the `plugin` directory via `includeBuild("plugin")`.

```bash
./gradlew :plugin:check            # All checks: unit tests + functional tests + detekt
./gradlew :plugin:test             # Unit tests only
./gradlew :plugin:functionalTest   # Functional tests only (uses GradleRunner/TestKit)
./gradlew :plugin:detekt           # Static analysis only
```

To run a single test class:

```bash
./gradlew :plugin:test --tests "com.automattic.android.publish.BuildEnvironmentTest"
```

## Architecture

### Composite Build Structure

The root `settings.gradle.kts` includes the plugin as a composite build (`includeBuild("plugin")`). The actual plugin code lives entirely in the `plugin/` directory.

### Source Sets

| Source Set | Path | Purpose |
|-----------|------|---------|
| main | `plugin/src/main/kotlin/` | Plugin implementation |
| test | `plugin/src/test/kotlin/` | Unit tests (ProjectBuilder) |
| functionalTest | `plugin/src/functionalTest/kotlin/` | Integration tests (GradleRunner/TestKit) |

### Key Classes

- **PublishToS3Plugin** — Entry point. Applies `maven-publish`, adds S3 repository, registers tasks. Only activates Android-specific publishing when `com.android.library` plugin is detected.
- **BuildEnvironment** — Sealed class: `FromTag`, `FromPR`, `FromBranch`. Calculates version name based on build context.
- **PrepareToPublishToS3Task** — Main task. Parses CLI args (`--tag-name`, `--branch-name`, `--sha1`, `--pull-request-number`), calculates version, checks S3 for duplicates, sets version on all Maven publications.
- **ProjectExtensions.kt** — Extension functions on `Project` for S3 repository config, version management, and plugin marker POM updates.

### Version Naming Strategy

1. If `--tag-name` provided → use tag as version
2. Else if `--pull-request-number` provided → `{pr_number}-{sha1}`
3. Else → `{branch_name}-{sha1}` (branch names sanitized: `/` → `_`)

## Testing Conventions

- Unit tests use `kotlin.test` assertions (`assertEquals`, `assertNotNull`, etc.) — **not** AssertJ
- Functional tests use `GradleRunner` with `functionalTestRunner()` helper that creates temporary test projects
- Test naming follows backtick format: `` `given ..., when ..., then ...` ``

## Important Gotchas

- **Task ordering limitation**: Due to a [Gradle limitation](https://docs.gradle.org/current/userguide/custom_tasks.html#limitations), users must invoke `prepareToPublishToS3` and `publish` as separate tasks in the same command: `./gradlew :module:prepareToPublishToS3 [args] :module:publish` — passing args to just `:module:publish` won't work.
- **S3 returns 403 for missing artifacts**: The plugin treats both 404 and 403 as "version not found" when checking S3.
- **AWS credentials**: Publishing requires `AWS_ACCESS_KEY` and `AWS_SECRET_KEY` environment variables.
- **No version catalog**: Dependencies use inline versions in `build.gradle.kts` (no `libs.versions.toml`).
