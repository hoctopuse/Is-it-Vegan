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
- Known uncertainties must remain visible to the user.
- OCR output is fallible and must remain inspectable/editable by the user before final analysis.
- Analysis must use the current edited text; after an edit, any result for earlier text must be identified as stale until reanalysis.

Preserve deterministic behavior.

## Repository architecture

Main modules:

- `:app` — Android application, Compose UI, OCR integration and Android-specific code; it has a production dependency on `:mutation-core`.
- `:mutation-core` — Android-independent JVM business logic used by the application, ordinary JVM tests and PIT mutation testing. Keep the dependency direction `:app -> :mutation-core`.

PIT is a test tool for evaluating tests. For each file or change, establish its actual role: shared business code, test, PIT tooling or prototype. Temporary mutants and mutation-testing scripts, configuration, reports and helpers are not application features. Do not introduce PIT dependencies or mutation logic into the production path; after a campaign, verify that source files retain no temporary mutations. PIT results alone do not establish that a feature is connected to the UI.

Important project areas:

- `knowledge/ingredients.json` — editorial/reference ingredient knowledge base.
- `docs/` — maintained project documentation and MkDocs content.
- `reports/` — historical implementation, audit, benchmark and review reports.

Before choosing a location or making structural changes, inspect the relevant Gradle dependencies, source and documentation. Place Android integration in `:app` and shared, Android-independent logic in the appropriate existing core layer. Trace the affected path from the application entry point through its callers to the behavior; do not infer a feature's role from a module name.

Distinguish evidence that code compiles or is packaged from evidence that the user flow calls it, that the UI exposes it, and that its behavior was validated at runtime. Report only the levels established by inspection or testing.

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

Preserve pre-existing staged, unstaged and untracked changes, including unrelated files.

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
When a change affects user-visible state or a verdict, cover relevant transitions as well as isolated logic: for example edit, analyze, modify and reanalyze, including stale results and visible unknowns, uncertainty and traces where applicable. Choose a suitable state, UI or instrumented test for the risk; do not require a device for every change. Report any runtime flow that could not be verified.
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
Before publication, update `/docs` and `docs/changelog.md` as appropriate to the changes actually delivered. Distinguish shipped behavior, experimental work and behavior not yet validated; do not describe compiled or uncalled code as a delivered user feature. Update documentation when behavior, architecture, workflows or data formats change.
Generated task reports belong under /reports, not in the repository root.
Use the relevant version/workstream directory under /reports.
Existing reports are historical evidence. Do not rewrite or delete them unless explicitly requested.

## Reporting changes

At the end of an implementation task, report concisely:
- what changed;
- important files affected;
- tests/validation actually executed;
- result, including the device or emulator used for runtime validation and relevant failures resolved during validation;
- remaining risks, limitations or unverified points.
Distinguish clearly between verified results and assumptions.

## Context efficiency

Minimize context usage.
Search first, then open only the relevant sections of files.
Do not recursively inspect the entire repository when targeted inspection is sufficient.
Do not include build artifacts, IDE metadata or generated files in analysis unless directly relevant.
For large datasets such as ingredients.json, prefer targeted queries or scripts over loading the complete file.
For recurring specialized workflows such as benchmarks, mutation testing, OCR regression, OFF imports or release validation, follow the corresponding project skill when available instead of rediscovering the procedure.
