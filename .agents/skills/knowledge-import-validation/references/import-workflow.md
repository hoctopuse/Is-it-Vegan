# Knowledge import workflow — Is It Vegan?

## Purpose

This document describes the generic workflow for integrating external knowledge.

It does not define how every external source is parsed.

Source-specific acquisition and extraction belong in `source-adapters.md`.

## 1. Inspect repository state

Before any import:

```powershell
git status --short --branch
git rev-parse HEAD
```

Understand existing changes before proceeding.
Do not discard unrelated modifications.
Record the starting revision when producing an import report.
When practical, establish the current validation baseline before the import so pre-existing stale generated files, failing checks, or known inconsistencies are not incorrectly attributed to the new data.

## 2. Identify the source

Before touching knowledge, establish:
- source name;
- source owner/publisher;
- source location;
- dataset or document identity;
- revision, commit, snapshot or consolidation date;
- retrieval date when relevant;
- applicable license;
- attribution requirements;
- scope and known limitations.
For external datasets, prefer a stable snapshot or revision over an unversioned live source when reproducibility matters.
If the actual dataset cannot be identified precisely, stop before import.
If licensing or reuse rights are required for the intended import and cannot be established, stop before import.
A documentation page describing a dataset is not necessarily the dataset itself.

## 3. Determine the acquisition layer

Inspect existing repository tools.
Ask:
- Is there already an extractor for this source?
- Is there already an importer?
- Is the source already structured?
- Does the source-specific tool still match the current source format?
- Does the tool validate its own source assumptions?
Do not force a PDF-oriented extractor onto JSON, CSV, API, taxonomy or repository data.
Do not generalize a source-specific script silently.

## 4. Inspect importer modes

Before invoking an importer, inspect its CLI.
Repository importers are not guaranteed to expose identical modes.
Possible modes include:
- `--dry-run`
- `--check`
- `--write`
- report-only behavior
- dry-run behavior by default
Do not assume a mode exists.
Prefer the safest available non-writing mode first.

### Meaning

`dry-run` should be treated as:
compute the proposed transformation without writing it.

`check` should be interpreted according to the specific tool.
It may mean:
confirm that the repository is already synchronized with the importer.

Do not assume `--check` means the same thing as `dry-run`.
`write` performs the actual mutation and must only be used after the proposed changes are understood.

## 5. Inspect candidate mappings

Before writing, review the proposed effect on:

### Concepts

Check:
- canonical IDs;
- names;
- statuses;
- reasons;
- provenance;
- optional origin metadata.

### Aliases

Check:
- canonical owner;
- normalized form;
- language;
- collision with existing aliases;
- whether the alias represents the concept itself or only a context.

### Multilingual mappings

Check:
- `surfaceForm`;
- language;
- `conceptId`;
- `mappingGroup`;
- relation;
- `normalizedForm`;
- source;
- confidence.

### Sources

Check that source identifiers are:
- stable;
- unique;
- actually referenced;
- consistent with existing source records.

## 6. Detect dangerous changes

Before write, explicitly look for:
- removed concepts;
- changed IDs;
- status changes on existing concepts;
- overwritten reasons;
- provenance loss;
- removed aliases;
- alias ownership changes;
- multilingual collisions;
- duplicate mappings;
- conflicting source definitions;
- large unexpected decreases in concept or mapping counts.
Unexpected destructive changes are blocking until explained.
Do not use fixed historical counts as universal future thresholds.
Historical counts may be useful as evidence for a specific migration, but the generic rule is to compare against the actual pre-import repository state.

## 7. Classification review

Classification requires separate reasoning from vocabulary import.
Ask:
1. Does the source prove the concept's composition?
2. Does it prove biological origin?
3. Does it only define a legal/product category?
4. Can manufacturers vary composition?
5. Can the term describe mixtures containing animal ingredients?
6. Is a `possibleOriginNote` more appropriate than a confident status?
Do not make a term `VEGAN` merely because its name sounds plant-based.
Do not make a regulatory category confidently vegan if the regulation permits non-vegan composition.
Prefer `UNCERTAIN` when the evidence supports recognition but not confident vegan suitability.

## 8. Apply the import

When write mode is explicitly appropriate:
1. execute the existing importer;
2. inspect its output;
3. inspect git status;
4. inspect the exact diff.
Useful commands:

```powershell
git status --short
git diff --stat
git diff -- knowledge app/src/main/assets docs/generated
```

Do not assume a successful exit code proves that the semantic diff is correct.

## 9. Validate editorial knowledge

Select validators according to what changed.

### Ingredients and multilingual asset parity

When `ingredients.json` or multilingual aliases change, inspect and normally run:

```powershell
python tools/build_ingredients.py --check
```

If assets are expected to be regenerated, inspect the script first and run its generation mode as appropriate, then run --check.
The goal is:
- structural knowledge validation;
- valid statuses and sources;
- alias-collision validation;
- current generated assets.

### Multilingual mapping

When aliases, OCR variants or mapping metadata change:

```powershell
python tools/build_multilingual_ingredient_mapping.py --check
```

Inspect report mode when useful.
Validate:
- canonical concepts exist;
- mapping metadata is complete;
- language identifiers are supported;
- normalized collisions are understood;
- generated mapping documentation is current;
- knowledge and Android alias assets remain in parity.

### Origin rules

When `origin_qualifier_rules.json` changes:

```powershell
python tools/build_origin_rules.py --check
```

Validate:
- schema version;
- source references;
- rule IDs;
- allowed statuses;
- target definitions;
- attachment forms;
- contradictory qualifier outcomes;
- generated Android asset parity.

### Generated knowledge documentation

When knowledge changes affect generated documentation:

```powershell
python tools/build_knowledge_docs.py --check
```

If stale, regenerate through the repository tool and re-check.
Do not edit generated knowledge documentation manually.

## 10. Business regression testing

After structural validation, select tests that exercise the affected behavior.
Examples:
- matching;
- multilingual aliases;
- origin qualifiers;
- verdicts;
- uncertain classifications;
- regulatory aliases;
- protected contexts;
- traces.
Prefer targeted tests first.
Expand to broader module/application validation when the scope justifies it.
Follow the project's Android validation skill when ordinary Kotlin/Android validation is required.

## 11. Non-regression

Compare pre-import and post-import knowledge.
At minimum consider:
- historical concept IDs retained;
- existing statuses retained unless deliberately migrated;
- existing aliases retained unless deliberately migrated;
- mappings preserved;
- generated assets synchronized.
For sensitive imports, explicitly audit historically important concepts.
Do not recreate missing concepts or aliases from memory merely because they are absent.
Absence requires evidence before restoration.

## 12. Idempotence

A well-behaved importer should normally converge.
After a successful write and regeneration, a subsequent check or dry-run should report no unintended changes.
Where the importer supports it, validate this property.
A repeated import that continues mutating unrelated data is suspicious.

## 13. Final diff review

Before declaring success:

```powershell
git status --short
git diff --check
git diff --stat
```

Inspect the actual changed knowledge files.
Confirm that no unrelated:
- application code;
- build configuration;
- versions;
- assets;
- documentation;
changed unexpectedly.

## 14. Outcome

Classify the result clearly.
Possible outcomes include:
- GO
- GO_WITH_WARNINGS
- BLOCKED_SOURCE
- BLOCKED_LICENSE
- BLOCKED_COLLISION
- BLOCKED_CLASSIFICATION
- BLOCKED_NON_REGRESSION
- BLOCKED_VALIDATION
Do not force an import to completion when the evidence supports a blocked result.
