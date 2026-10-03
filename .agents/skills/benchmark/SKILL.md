---
name: benchmark
description: Run, inspect, compare, or design performance benchmarks for Is It Vegan. Use for performance baselines, before/after measurements, profiling, runtime regressions, Nokia G42 device benchmarks, JVM benchmarks, matcher performance, parsing performance, memory observations, or performance claims.
---

# Benchmark

Use this skill for performance measurement and comparison in Is It Vegan.

Read `references/benchmark-workflow.md` before running or comparing benchmarks.

## Core rules

- Inspect repository state before benchmarking.
- Identify the exact commit being measured.
- Prefer an existing versioned benchmark protocol over inventing a new one.
- Search `reports/` for the most relevant prior baseline before running measurements.
- Preserve unrelated working-tree changes.
- Do not modify production code merely to make benchmarking easier unless explicitly requested.
- Do not run broad test suites when a targeted benchmark answers the question.
- Do not claim a performance improvement or regression from non-comparable measurements.
- Never directly compare Android and JVM timings as a percentage gain or regression.
- Preserve functional correctness checks alongside performance measurements.
- Report environmental uncertainty such as JIT, GC, device temperature and background load.

Performance measurements are evidence, not architecture decisions by themselves.

## Benchmark comparability

Before comparing two measurements, verify that the relevant conditions are compatible:

- same benchmark scenario;
- same operation being measured;
- same dataset or corpus;
- same device or equivalent execution platform;
- same Android/JVM context;
- same runner;
- same warmup protocol;
- same repetition count;
- same aggregation method;
- compatible code path;
- compatible knowledge-data size.

If these differ materially, report the measurements separately.

Do not manufacture a percentage comparison when comparability is not established.

## Workflow

1. Inspect Git state and current revision.
2. Locate the relevant historical benchmark report.
3. Inspect the benchmark test and protocol currently present in the repository.
4. Identify the execution platform: JVM or Android device.
5. Verify benchmark preconditions.
6. Record the environment before measurement.
7. Run the smallest targeted benchmark.
8. Preserve the raw result artifacts long enough to inspect them.
9. Verify functional assertions and benchmark success.
10. Record the environment after measurement when relevant.
11. Compare only against compatible historical measurements.
12. Report results, limitations and comparability explicitly.

## Android benchmarks

Before a real-device benchmark:

- verify the device with `adb devices`;
- record device model and Android version;
- inspect battery and thermal state when performance sensitivity warrants it;
- use the same device as the reference baseline whenever possible.

For the established Nokia reference, prefer the Nokia G42 5G when reproducing or comparing that baseline.

Do not silently substitute another device and present the result as directly comparable.

## JVM benchmarks

JVM measurements may be useful for targeted algorithmic investigation.

Account for:

- JVM warmup;
- JIT;
- GC;
- daemon state;
- host-machine load.

Do not treat JVM timings as equivalent to Android-device timings.

## Functional safety

A faster result is not useful if behavior changes.

When the benchmark exercises business logic, preserve relevant assertions for:

- matching;
- verdict behavior;
- unknown handling;
- multilingual behavior;
- traces;
- diagnostics;
- structured ingredients.

If the benchmark itself does not assert the measured business result, state that explicitly and rely on appropriate separate regression tests when required.

## Reporting

At completion report:

- commit/revision measured;
- benchmark test and method;
- platform/device;
- relevant environment;
- corpus/data counts when applicable;
- warmup count;
- repetition count;
- aggregation method;
- measured values;
- functional-test result;
- historical baseline used;
- whether comparison is valid;
- limitations and uncontrolled variables.

Clearly distinguish raw measurements from interpretation.
