# PIT workflow — Is It Vegan?

## Purpose

This document contains project-specific guidance for PIT mutation testing.

It complements the permanent rules in the mutation-testing skill.

Always inspect the current Gradle configuration before executing commands because plugin versions, target classes, test filters, and report locations may evolve.

## Architecture

The application is split into:

- `:app` — Android integration, UI, Compose, Context/assets loading, Bitmap/Uri, ML Kit and device-specific behavior.
- `:mutation-core` — JVM business logic suitable for ordinary JVM tests and PIT.

The only allowed module direction is:

`:app -> :mutation-core`

The mutation core must remain independent of Android APIs.

Do not move Context, Bitmap, Uri, Compose, ML Kit or Android-specific resource loading into `:mutation-core` for the purpose of mutation testing.

## Why PIT targets mutation-core

An earlier experiment with the Android-specific PIT plugin resolved the plugin but broke Gradle Kotlin DSL configuration before any PIT task became usable.

That path was abandoned.

The permanent JVM module was introduced so standard JVM mutation testing can operate on the same business code used by the Android application.

Do not recreate a copied JVM harness unless explicitly requested for an isolated experiment.

## Baseline

Before running PIT, establish the smallest relevant ordinary-test baseline.

Typical module baseline:

.\gradlew :mutation-core:test

Use --rerun-tasks when a clean re-execution is required:
.\gradlew :mutation-core:test --rerun-tasks

If the change can affect Android integration with the core, expand validation as justified:
.\gradlew testDebugUnitTest
.\gradlew assembleDebug

Do not require connectedDebugAndroidTest solely for a JVM mutation campaign.
Run device tests only when the task affects Android/device behavior or explicitly requires them.

## Workspace isolation

Mutation testing is intentionally isolated from the main development branch.

The project uses the dedicated branch:

`mutation-testing-pit`

Before every real PIT campaign:

git status --short
git branch --show-current

If the active branch is not mutation-testing-pit, switch to it before continuing:
git switch mutation-testing-pit

Do not run PIT directly on master.
Do not automatically merge mutation-testing changes back into master.
Tests or implementation changes discovered during mutation analysis must be reviewed explicitly before integration into the main branch.
Preserve unrelated working-tree changes and never use destructive Git operations merely to obtain a clean mutation workspace.

## Synchronizing the mutation branch

Before every real PIT campaign, inspect the repository:


git status --short
git branch --show-current
git fetch origin

The working tree should be understood before switching branches. Do not discard local changes.

## Verify master

Inspect the relationship between local and remote master:
git rev-list --left-right --count master...origin/master

Interpret the result before continuing.
If local master is behind origin/master, update it only with a safe fast-forward when appropriate:
git switch master
git pull --ff-only

Do not use reset, force operations, or history rewriting to make master match the remote.

## Verify the mutation branch contains master

Switch to the dedicated mutation branch:
git switch mutation-testing-pit

Check whether the current master revision is already contained:

git merge-base --is-ancestor master HEAD

Exit code 0 means the mutation branch already contains the current master.
A non-zero result means the mutation branch is stale and must receive the current master before PIT results are trusted.
Configuration inspected on a stale mutation branch must be reported explicitly as stale branch configuration and must not be assumed to represent the current master state.

## Synchronize

The normal project workflow is to bring master into the mutation branch:
git merge master

Do not merge mutation-testing-pit back into master automatically.
If synchronization produces conflicts, stop the mutation campaign and report them instead of improvising conflict resolution.
After synchronization, verify:

git status --short
git merge-base --is-ancestor master HEAD

Only start the mutation baseline when the ancestry check succeeds.

## Mutation scope

Start with the smallest meaningful production scope.
Good mutation candidates include deterministic business components such as:
- verdict logic;
- ingredient matching;
- parsing;
- tokenization;
- normalization;
- unknown collection;
- language segmentation;
- section extraction;
- origin rules;
- diagnostic business logic.

Avoid mutating broad packages immediately when a class-level campaign can answer the question.
For a new or changed PIT configuration, start with at most one or two production classes.

Expand only after:
1. ordinary tests are green;
2. PIT executes successfully;
3. generated mutants are meaningful;
4. reports are reproducible.

## Running PIT

First inspect available Gradle tasks and the current PIT configuration rather than assuming a historical task name:
.\gradlew tasks --all

Then run the PIT task configured for :mutation-core.
Use existing project configuration when present.
Do not add, upgrade, or replace PIT plugins merely because a command from an old report no longer matches the repository.
If the PIT task is missing or configuration is broken, report that state before altering build configuration.

## Interpreting mutants

Classify mutation results into these categories.

## Killed

A test detects the behavioral change introduced by the mutant.
No further action is required unless the kill depends on an unrelated or fragile assertion.

## Survived

A mutant changed executable behavior but all selected tests still passed.

Inspect:
- mutated expression;
- affected business behavior;
- current assertions;
- whether the behavior matters to the product contract.

If meaningful, strengthen the smallest appropriate test.
Do not change production code merely to make the mutant disappear.

## No coverage / uncovered

The mutated instruction was not exercised by the selected tests.

Determine whether:
- the relevant test was excluded by the mutation scope;
- the behavior genuinely lacks a test;
- the code is unreachable or obsolete;
- Android integration is intentionally outside the JVM mutation perimeter.

Add coverage only when the behavior is part of the supported contract.

## Equivalent

Some Kotlin/JVM mutants may produce no observable semantic difference.
Examples can arise from generated Kotlin bytecode, null-safety, synthetic methods, data classes, or logically equivalent boundary transformations.
Do not create artificial tests merely to kill an equivalent mutant.
Document why it is considered equivalent or non-actionable.

## Test changes

When a survivor reveals a real testing gap:
1. reproduce the affected behavior with an ordinary unit test;
2. add or strengthen the smallest assertion that expresses the actual contract;
3. run the targeted ordinary test;
4. run the relevant module tests if justified;
5. rerun the same PIT scope;
6. confirm that the intended mutant is now killed.

Avoid tests coupled to PIT implementation details.
A good test should remain valuable even if PIT is removed.

## Knowledge-related mutations

The ingredient knowledge base is not a mutation-testing playground.
Do not alter:
- knowledge/ingredients.json;
- multilingual aliases;
- regulatory sources;
- generated production knowledge assets;

merely to improve mutation results.

When mutation work touches matching or status propagation, preserve established concepts, aliases and statuses.
Knowledge changes require their own evidence and validation.

## Historical evidence

Reports under:
reports/mutation-testing/

contain architecture decisions and previous experiments.

Important lessons from them:
- direct PIT integration into the Android app was problematic;
- a permanent JVM core was preferred over a copied harness;
- ordinary tests must be green before mutation results are trusted;
- Android/device behavior is outside the mutation-core measurement;
- initial campaigns should be deliberately narrow;
- historical mutation scores must not be compared blindly when target classes or test scopes differ.

## Reporting

A useful mutation report should include at least:
Scope:
Target classes:
Target tests:
Ordinary-test baseline:
PIT task:
Mutants generated:
Killed:
Survived:
No coverage:
Mutation score:
Tests changed:
Equivalent/non-actionable mutants:
Remaining risks:

When comparing two campaigns, verify that they used compatible:
- production classes;
- tests;
- mutators;
- PIT configuration;
- filters/exclusions.
If the scopes differ, say so explicitly rather than presenting the scores as a direct progression.
