# PIT mutation testing report — `:mutation-core`

Date: 2026-09-25  
Branch: `mutation-testing-pit`

## Git state

Before the original PIT change, `git status --short` was clean. At the start of
the diagnostic follow-up, the working tree was also clean at commit `3e15c43`.
After the diagnostic, only this report is modified. No commit or push was
performed by the diagnostic agent.

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

## Initial PIT execution result

The first execution performed its pre-scan and created 2 mutation test units,
then sent 116 tests to a coverage minion. Coverage generation stopped with:

```text
Coverage generator Minion exited abnormally due to UNKNOWN_ERROR
org.pitest.util.PitError: Coverage generation minion exited abnormally!
```

That first run produced no mutants or report. The diagnostic below supersedes
this initial result.

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

The 116 value reported by PIT is the number of tests examined, rather than an
unexpected number of compiled test classes. This report does not fix the test
weaknesses found by mutation analysis or alter production behavior.

## Recommendations

Keep the campaign confined to `:mutation-core`. A later agent can triage the
surviving and uncovered mutants, especially the matching boundary conditions,
without changing tests merely to raise the score. If the minion error recurs,
capture verbose PIT output and inspect host resource/process interference.

## Diagnostic of the coverage minion failure

The failure was reproduced originally with:

```text
./gradlew :mutation-core:pitest --no-daemon --console=plain
```

It could not be reproduced during the diagnostic. The first diagnostic command
disabled only the Gradle configuration cache and retained the existing Java 17
PIT toolchain:

```text
./gradlew :mutation-core:pitest --stacktrace --info \
  --no-configuration-cache --no-daemon --console=plain
```

It completed coverage and generated mutants. A second forced execution with the
configuration cache enabled also completed:

```text
./gradlew :mutation-core:pitest --rerun-tasks --stacktrace --info \
  --no-daemon --console=plain
```

The second command reused the configuration cache, which rules it out as the
cause observed here. `:mutation-core:test` succeeded between the diagnostic
attempts. No Gradle or PIT configuration correction was needed or retained.

The runtime split shown by `--info` is:

- Gradle launcher environment: Oracle JDK 27;
- Gradle daemon selected by `gradle-daemon-jvm.properties`: Eclipse Temurin 25;
- Kotlin compilation, PIT command process and PIT minions: Eclipse Temurin
  17.0.20.1.

The relevant versions are Gradle 9.7.1, Kotlin plugin 2.4.20, PIT Gradle plugin
1.19.0, and PIT engine 1.22.1 selected by that plugin. Both successful runs used
the same Java 17 PIT process as the failed run. There is therefore no evidence
of a Java 25/27, Kotlin bytecode, classpath, fork communication, worker, or
configuration-cache incompatibility. The most likely explanation for the
original `UNKNOWN_ERROR` is a transient minion process failure; the available
log did not preserve a more specific child-process exception, so its exact
external trigger cannot be confirmed.

Both diagnostic campaigns produced the same aggregate result:

- line coverage for the two mutated classes: 287/291 (99%);
- tests examined: 116;
- mutants generated: 157;
- killed: 106;
- survived: 41;
- no coverage: 10;
- timed out, non-viable, memory error, run error: 0;
- mutation score: 68%;
- test strength among covered mutants: 72%;
- total PIT duration: 8–10 seconds (approximately 20–23 seconds including the
  Gradle invocation);
- reports: `mutation-core/build/reports/pitest/index.html` and `mutations.xml`.

No mutant was left in source code. PIT performed its mutations in forked
processes and generated only build reports. Important survivors to investigate
later include conditional boundaries and negated conditions in
`IngredientMatcher` (`protectedPair`, `hasNoSemanticRemainder`, `isCovered`,
and `match`) and a negated condition in
`VerdictEngine.assessVeganCompatibility`. Several surviving removed Kotlin
null-check calls may be compiler-generated or equivalent; equivalence was not
proven and is not counted separately by PIT.

The retained correction is therefore documentation only. No JVM override,
plugin migration, source change, test change, or cache workaround is justified
by the successful controlled reruns.

## Verdict

`PIT_OPERATIONNEL` — coverage and mutation generation completed twice on the
limited two-class scope, including once with the configuration cache enabled.
