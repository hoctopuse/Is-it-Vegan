---
name: android-validation
description: Validate changes to the Is It Vegan Android project using the smallest relevant Gradle test and build scope. Use after Kotlin, Android, Compose, parsing, matching, verdict, OCR integration, or build configuration changes, and when asked to verify that an implementation is safe to continue.
---

# Android validation

Validate the current change with the smallest useful scope before expanding to broader validation.

## 1. Inspect the change

Before running tests:

1. Run `git status --short`.
2. Inspect the relevant diff.
3. Identify the affected module, behavior and path from the app entry point through relevant callers. `:app` depends on `:mutation-core` in production; the latter also supports JVM tests and PIT.
4. Preserve pre-existing staged, unstaged and untracked changes.

Do not modify production code merely to make validation easier unless the task explicitly requires a fix.

## 2. Select the smallest relevant validation

Prefer targeted validation over immediately running the entire project test suite.

### JVM/business logic

For changes isolated to `:mutation-core`, run the relevant module tests first.

Use a targeted test task or test filter when the affected test class is known.

Expand to the complete `:mutation-core` test suite when appropriate.

### Android application logic

For changes under `:app` that are covered by local unit tests, run the relevant `testDebugUnitTest` scope first.

Use test filters when practical.

### Build-sensitive changes

For Gradle, dependency, resource, manifest, Compose, or Android integration changes, run an appropriate build task such as:

`.\gradlew assembleDebug`

### Instrumented behavior

Do not require connected-device tests for every change.

Use instrumentation tests when the modified behavior depends on Android runtime behavior, Compose instrumentation, OCR/device integration, or when explicitly requested.

Before running connected tests, verify that a suitable device or emulator is available.

For changes to user-visible state or verdicts, select tests for the relevant transitions in addition to isolated functions. Where applicable cover analysis of edited text, editing after a result, stale-result identification and reanalysis, plus visible unknowns, uncertainty and separately displayed traces. Use state, UI or instrumentation tests according to the behavior and risk; report runtime gaps when a suitable device is unavailable.

Compilation or packaging alone does not establish that a feature is called, visible or validated at runtime. Trace the path and report the evidence for each relevant level.

## 3. Escalate only when justified

If targeted validation passes, decide whether broader validation adds useful confidence.

Do not automatically run every available test suite.

Escalate when:

- shared business logic changed;
- parsing, matching or verdict behavior changed broadly;
- build configuration changed;
- multiple modules are affected;
- regression risk is significant;
- the task explicitly requires complete validation.

If a targeted test fails, inspect the failure before running broader suites.

## 4. Handle failures

When validation fails:

1. identify the failing Gradle task or test;
2. extract the relevant error;
3. determine whether the failure is related to the current change;
4. inspect relevant code or tests;
5. do not hide unrelated pre-existing failures.

Do not repeatedly rerun an unchanged failing command without a reason.

## 5. Report the result

At completion, report concisely:

- validation commands actually executed;
- tests/builds that passed;
- failures, if any;
- relevant failures resolved during validation and the successful rerun;
- device or emulator used for runtime tests, when applicable;
- validation that was intentionally not run;
- remaining uncertainty or device-dependent validation.

Never claim validation that was not actually executed.
