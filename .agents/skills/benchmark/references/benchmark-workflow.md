# Benchmark workflow — Is It Vegan?

## Purpose

This reference contains project-specific benchmark guidance.

Benchmark protocols evolve. Always inspect the current repository and historical reports before assuming that commands, test classes or sample counts are still current.

## Historical benchmark reports

Performance reports live under versioned directories in:

`reports/`

For v0.7 benchmarks, inspect:

`reports/0.7/benchmark/`

Also inspect preparation/performance reports when they define the protocol being reproduced.

The most recent compatible baseline should take precedence over an older measurement.

Do not treat old measurements as current repository state.

## Repository state

Before measuring:

git status --short --branch
git rev-parse HEAD
git log -5 --oneline

Record the exact revision.

Understand any existing working-tree changes before proceeding.

Do not clean, reset or discard unrelated changes for the sake of obtaining a clean benchmark environment.

If the task requires a clean reference and the worktree is not clean, report that limitation rather than silently removing changes.

## Locate the reference protocol

Before running a before/after comparison:
1. locate the relevant historical report;
2. identify the benchmark test;
3. identify the method/scenario;
4. record warmups and repetitions;
5. record the aggregation method;
6. identify the corpus and data counts;
7. identify the execution platform;
8. identify the device when Android is involved.
Prefer reproducing an established protocol over creating a superficially similar benchmark.

## Current Nokia-style Android reference

The established pre-phase-5 Nokia benchmark used:
- Nokia G42 5G;
- Android 15;
- debug APK;
- AndroidJUnitRunner;
- Phase4MatcherProfileInstrumentedTest;
- targeted connected instrumentation;
- five warmups;
- ten measured repetitions;
- minimum, lower median and maximum.
The reported median is specifically the lower median for ten samples, not the arithmetic mean of the two central values.

When comparing against that baseline, preserve this method unless the task explicitly introduces a new protocol.

If the protocol changes, create a new baseline rather than pretending that the measurements are directly equivalent.

## Android preconditions

Check the connected device:
adb devices
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release

When relevant, record:
adb shell dumpsys battery
adb shell dumpsys thermalservice

Thermal state should be observed before and after a performance-sensitive run.

A device heating during the run is not automatically a failure, but it must be reported because CPU frequency and thermal management can affect measurements.

## Running a targeted Android benchmark

Prefer the exact benchmark class already defined by the reference protocol.
A connected Android test class can be targeted with an instrumentation runner argument such as:
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.Phase4MatcherProfileInstrumentedTest" --console=plain

Inspect the current task and class before executing this command.

Do not assume --tests works for connected instrumentation tasks.

Avoid running the full connected test suite when the benchmark task alone is sufficient.

## Raw artifacts

Gradle/JUnit/logcat artifacts under build/ are temporary.

Inspect them before cleanup.

Typical relevant artifacts may include:
- Gradle output;
- connected Android JUnit XML;
- per-test logcat;
- benchmark-specific marker lines.

Do not treat generated build/ files as durable project documentation.

Persist important benchmark values in a report under reports/.

## Dataset counts

Performance may depend heavily on knowledge size.

Record relevant corpus counts when the benchmark concerns knowledge loading, matching or analysis.

Examples include:
- concepts;
- multilingual mappings;
- alias groups;
- normalized aliases.

Do not compare matcher or knowledge-loading results across materially different corpus sizes without noting the difference.

## JVM measurement

JVM benchmarks may use a different protocol from Android.

Historical JVM measurements have used explicit warmup and repeated sequential execution.

Do not transfer Android sample counts or aggregation rules automatically to JVM measurements.

Inspect the corresponding JVM benchmark test/report first.

## Functional parity

Benchmarking must not silently replace correctness validation.

For experimental structures or indexes, verify functional parity independently when required.

A candidate lookup structure may expose a broader candidate set than the production matcher while the matcher still applies:
- contextual protection;
- priorities;
- boundaries;
- conflict blocking.

Performance of candidate enumeration therefore does not by itself prove production-equivalent behavior.

## Memory observations

Treat process or heap memory measurements cautiously.

Android PSS, JVM used heap and before/after process memory are affected by:
- GC;
- process state;
- allocations outside the measured structure;
- runtime caches.

Report them as process-level observations unless a dedicated measurement proves object-level memory usage.

Do not claim precise memory savings from simple before/after PSS or heap values.

## Comparing results

Before calculating a difference or percentage, check:
- platform matches;
- device matches when Android;
- corpus matches;
- benchmark implementation matches;
- operation semantics match;
- warmups match;
- repetition count matches;
- aggregation method matches;
- functional path matches.

If any material factor differs, describe the comparison as indicative rather than quantitative.

Never turn an indicative comparison into a performance claim.

## Benchmark report

A durable report should include:
Reference commit:
Compared commit:
Benchmark class/method:
Platform:
Device:
Android/JVM version:
Dataset counts:
Warmups:
Measured repetitions:
Aggregation:
Functional assertions:
Battery/thermal before:
Battery/thermal after:

Measurements:
- operation:
  min:
  median:
  max:

Historical baseline:
Comparability:
Observed difference:
Limitations:
Raw artifacts:
Validation commands:

## Scope discipline

Do not automatically run:
- PIT;
- knowledge importers;
- data generators;
- documentation builds;
- the complete instrumented suite;

unless they are required by the benchmark task.

A performance benchmark should remain as narrow and reproducible as possible.
