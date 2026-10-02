# AGENTS.md — Is It Vegan?

## Project identity

Is It Vegan? is an offline Android application written in Kotlin with Jetpack Compose.

It analyzes ingredient lists from manual input or OCR and produces a deterministic vegan-status assessment using the embedded knowledge base.

The application must remain usable offline. Business decisions must not depend on generative AI, cloud inference, or remote services.

## Core invariants

These rules are business invariants and must not be changed unless explicitly requested.

- `VEGAN`: all considered ingredients are recognized as vegan.
- `NON_VEGAN`: at least one identified ingredient is non-vegetarian / animal-derived.
- `UNCERTAIN`: a known ingredient has variable origin or an uncertain status.
- `INCONCLUSIVE`: an unknown ingredient or insufficient analysis prevents a complete conclusion.
- `VEGETARIAN` is an informative detailed classification, not a positive vegan verdict.
- Cross-contamination traces are displayed separately and must not affect the verdict.
- Explicit ingredient presence such as `contient : lait` is not a trace and must be treated as actual presence.
- Unknown ingredients must remain visible to the user and must not silently become vegan.
- OCR output is fallible and must remain inspectable/editable by the user before final analysis.

Preserve deterministic behavior.

## Repository architecture

Main modules:

- `:app` — Android application, Compose UI, OCR integration and Android-specific code.
- `:mutation-core` — JVM-compatible business logic extracted for mutation testing and isolated validation.

Important project areas:

- `knowledge/ingredients.json` — editorial/reference ingredient knowledge base.
- `docs/` — maintained project documentation and MkDocs content.
- `reports/` — historical implementation, audit, benchmark and review reports.

Before making structural changes, inspect the relevant existing code and documentation rather than inferring architecture from filenames alone.

## Working rules

Work directly from the local repository.

Prefer local inspection and commands over asking the user to provide information that already exists in the repository.

Use targeted searches (`rg`, `git grep`, Gradle tasks, etc.) and read only files relevant to the current task.

Do not load or reproduce large source files unnecessarily.

Exclude generated or irrelevant directories from reasoning context, including:

- `build/`
- `.gradle/`
- `.idea/`
- generated outputs
- virtual environments
- temporary files

Prefer concise summaries, diffs and targeted excerpts over complete file reproduction.

## Git

Inspect repository state before significant modifications:

- `git status`
- `git diff`
- relevant history when needed

Preserve unrelated user changes.

Do not commit, amend, rebase, reset, push, force-push or modify repository history unless explicitly requested.

Do not silently discard working-tree changes.

## Build and tests

Use the repository Gradle wrapper.

After Kotlin/business-logic changes, run the smallest relevant test set first.

Expand validation only when justified by the scope of the change.

Typical commands include:

./gradlew test
./gradlew testDebugUnitTest
./gradlew assembleDebug

Use Windows equivalents when running from PowerShell.
For Android instrumentation tests, use the connected-device tasks when the task requires them.
Do not paste complete build or test logs into reports or responses. Analyze them locally and report:
- failing task/test;
- relevant error;
- likely cause when supported by evidence;
- validation result.
Do not claim a test passed unless it was actually executed successfully.

## Knowledge data

knowledge/ingredients.json is the reference knowledge source.
Preserve its schema, identifiers, multilingual mappings, aliases and status semantics.
Do not expand or mass-modify the knowledge base unless the task explicitly requires it.
Do not convert uncertain or unknown knowledge into confident classifications without supporting evidence.
Changes to knowledge data must preserve non-regression expectations for existing recognized ingredients.

## OCR and parsing

OCR is an acquisition layer, not the authority for the final business verdict.
Preserve the separation between:
1. image/OCR acquisition;
2. language/section detection;
3. ingredient parsing;
4. knowledge matching;
5. verdict computation;
6. user-facing explanation.
Avoid fixes that improve one OCR example by introducing hard-coded product-specific behavior.
Multilingual processing must remain compatible with the supported FR/NL/EN/DE workflow unless explicitly changing language support.

## Documentation

Consult existing documentation before significant changes to architecture, OCR, parsing, verdict logic or knowledge formats.
Update /docs when a modification changes documented behavior, architecture, workflows or data formats.
Generated task reports belong under /reports, not in the repository root.
Use the relevant version/workstream directory under /reports.
Existing reports are historical evidence. Do not rewrite or delete them unless explicitly requested.

## Reporting changes

At the end of an implementation task, report concisely:
- what changed;
- important files affected;
- tests/validation actually executed;
- result;
- remaining risks, limitations or unverified points.
Distinguish clearly between verified results and assumptions.

## Context efficiency

Minimize context usage.
Search first, then open only the relevant sections of files.
Do not recursively inspect the entire repository when targeted inspection is sufficient.
Do not include build artifacts, IDE metadata or generated files in analysis unless directly relevant.
For large datasets such as ingredients.json, prefer targeted queries or scripts over loading the complete file.
For recurring specialized workflows such as benchmarks, mutation testing, OCR regression, OFF imports or release validation, follow the corresponding project skill when available instead of rediscovering the procedure.
