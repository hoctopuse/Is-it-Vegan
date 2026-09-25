# PIT mutation testing report — `:mutation-core`

Date: 2026-09-25  
Branch: `mutation-testing-pit`

## Git state

Before the PIT change, `git status --short` was clean. After the change, only
`mutation-core/build.gradle.kts` and this report are modified/untracked. No
commit or push was performed.

## Baseline

All required baseline commands succeeded:

- `./gradlew :mutation-core:test`
- `./gradlew testDebugUnitTest`
- `./gradlew assembleDebug`

The Android build emitted the existing native-library strip warning for
`libandroidx.graphics.path.so` and `libmlkit_google_ocr_pipeline.so`; the build
still completed successfully.

## Versions

- Gradle wrapper: 9.7.1
- Gradle launcher JVM: Oracle JDK 27; the module toolchain and PIT fork used
  Eclipse Temurin/OpenJDK 17.0.20.1
- Kotlin plugin: 2.4.20 (the Gradle runtime reports Kotlin 2.4.0)
- AGP: 9.4.1
- PIT Gradle plugin: `info.solidsoft.pitest` 1.19.0
- Java source/target and Kotlin toolchain for `:mutation-core`: 17

## Gradle changes

Only `:mutation-core` was changed. The classic PIT plugin was applied there,
with HTML/XML output, two worker threads, no timestamped report directory, and
the initial target scope limited to `VerdictEngine` and `IngredientMatcher`.
`:app` was not changed and no Android PIT plugin was used.

## Tasks and commands

Before the change, `:mutation-core:tasks --all` contained no PIT task. After the
change it exposed `:mutation-core:pitest` (`Run PIT analysis for java classes`).
Commands run included:

```text
./gradlew :mutation-core:test
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew :mutation-core:tasks --all
./gradlew :mutation-core:test :mutation-core:tasks --all
./gradlew :mutation-core:pitest --no-daemon --console=plain
```

## PIT execution result

PIT performed its pre-scan and created 2 mutation test units, then sent 116
test classes to a coverage minion. Coverage generation failed before mutation
generation:

```text
Coverage generator Minion exited abnormally due to UNKNOWN_ERROR
org.pitest.util.PitError: Coverage generation minion exited abnormally!
```

The Gradle task failed with the Java 17 fork exiting with code 1. Therefore:

- mutants generated: 0
- killed: 0
- survivors: 0
- equivalent: 0
- not covered: 0
- mutation score: unavailable
- HTML/XML mutation report: not produced

Duration was approximately 18 seconds for the PIT task. No memory exhaustion
was reported. The campaign was not retried or expanded after this compatibility
failure.

## Scope

The intended production scope was `com.example.isitvegan.VerdictEngine` and
`com.example.isitvegan.IngredientMatcher`. Tests were restricted to the
`com.example.isitvegan.*` package in `:mutation-core`; the module currently
contains the migrated parser/matching tests and `CoreTestAnalyzer`.

## Interpretation and limitations

Line coverage and mutation score measure different properties: line coverage
only records execution, while mutation testing requires assertions to fail when
behavior is deliberately changed. No mutation score can be inferred from this
run because PIT failed during coverage discovery.

The failure is a PIT/Gradle/JVM execution incompatibility or runtime minion
failure that needs investigation by a later agent. The unexpectedly large test
class count (116) should also be checked before any future campaign. This report
does not attempt to fix test weaknesses or alter production behavior.

## Recommendations

Validate the PIT 1.19.0 and Gradle 9.7.1 combination with a minimal Java-only
fixture, inspect the minion stderr with PIT verbose diagnostics, and then retry
on exactly the two target classes. Keep PIT confined to `:mutation-core` and
preserve the existing Android configuration and assets.

## Verdict

`PIT_PARTIELLEMENT_OPERATIONNEL` — the classic plugin resolves, the dedicated
task is available, and PIT starts its pre-scan, but no mutation campaign reaches
coverage or mutant generation.
