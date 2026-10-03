---
name: mutation-testing
description: Run, inspect, or analyze PIT mutation testing for the Is It Vegan project's JVM business core. Use when asked about PIT, mutation score, surviving mutants, uncovered mutants, mutation regressions, mutation campaigns, or strengthening tests against mutations.
---

# Mutation testing

Use this skill for mutation testing of the Is It Vegan business logic.

Mutation testing belongs to the JVM module `:mutation-core`.

Do not attempt to run PIT against the Android `:app` module unless the task explicitly asks for a new compatibility experiment.

Read `references/pit-workflow.md` before running or modifying a mutation-testing campaign.

## Core rules

- Inspect the current repository state and Gradle configuration before assuming historical commands or versions still apply.
- Preserve unrelated working-tree changes.
- Establish a green ordinary-test baseline before interpreting mutation results.
- Prefer small targeted mutation campaigns before broader runs.
- Do not modify production behavior merely to improve the mutation score.
- Do not weaken assertions or remove valid tests to eliminate surviving mutants.
- Distinguish valid surviving mutants from equivalent, irrelevant, unreachable, or uncovered mutants.
- Treat mutation score as diagnostic evidence, not as a goal by itself.
- Never report a mutant as killed, surviving, equivalent, or uncovered unless supported by the actual PIT result or direct inspection.
- Do not claim a full-project mutation score when only `:mutation-core` or selected classes were mutated.

## Git isolation and synchronization

All real mutation-testing work is performed on:

`mutation-testing-pit`

`master` remains the reference development branch.

Before every mutation campaign:

1. inspect the working tree;
2. fetch the remote repository state;
3. verify that local `master` represents the latest expected master state;
4. verify whether `mutation-testing-pit` contains the current `master`;
5. synchronize `master` into `mutation-testing-pit` when required;
6. only then establish the ordinary-test baseline and run PIT.

Do not run PIT against a stale mutation branch.

If `mutation-testing-pit` does not contain the current `master` revision:

- do not run the ordinary mutation baseline;
- do not run PIT;
- report the synchronization gap first;
- synchronize `master` into `mutation-testing-pit` before any campaign.

A mutation campaign is not considered valid if `mutation-testing-pit` does not include the master revision being evaluated.

Preserve unrelated working-tree changes and never use destructive Git operations to synchronize branches.

## Workflow

1. Inspect Git state.
2. Inspect the current `:mutation-core` PIT configuration.
3. Run the smallest relevant ordinary-test baseline.
4. Select a mutation scope justified by the task.
5. Run PIT.
6. Inspect the generated mutation report.
7. Classify survivors and uncovered mutants.
8. Strengthen tests only where the mutant reveals a meaningful missing assertion or behavior.
9. Re-run the targeted ordinary tests.
10. Re-run the relevant PIT scope.
11. Expand the mutation scope only when justified.

## Safety

Do not introduce Android dependencies into `:mutation-core`.

Preserve the architecture direction:

`:app -> :mutation-core`

Do not duplicate production business classes or tests into a temporary harness when the real module can be tested directly.

Do not modify the ingredient knowledge base, aliases, regulatory sources, or production assets merely to satisfy mutation testing.

If knowledge-related behavior is affected, verify that historical concepts and statuses are not silently lost or changed.

## Historical reports

Historical reports under `reports/mutation-testing/` document why the JVM module exists and previous experiments.

Use them for context, not as proof of the current repository state.

In particular, do not retry the historical Android PIT plugin approach unless explicitly requested.

## Result reporting

At the end of a mutation-testing task, report:

- mutation scope;
- ordinary tests executed;
- PIT command/task executed;
- classes targeted;
- mutants generated;
- killed mutants;
- surviving mutants;
- uncovered mutants;
- mutation score when available;
- meaningful survivors investigated;
- tests added or strengthened;
- equivalent or intentionally accepted mutants;
- remaining uncertainty.

Clearly distinguish measured results from interpretation.