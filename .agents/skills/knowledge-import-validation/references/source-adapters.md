# Source adapters — Is It Vegan?

## Purpose

External sources differ.

This document describes how source-specific acquisition fits into the generic knowledge import workflow.

A source adapter converts or verifies external evidence.

It does not bypass knowledge validation.

## Core rule

Source-specific extraction and generic knowledge validation are separate responsibilities.

Conceptually:

```text
external source
    ↓
source-specific acquisition / extraction
    ↓
reviewable structured candidate data
    ↓
source-specific importer
    ↓
generic knowledge validation
    ↓
editorial knowledge
    ↓
generated Android assets

```

## Existing PDF tooling

The repository contains several historical tools that read local EUR-Lex PDFs.
These are source-specific.
Examples include extractors and importers for:
- food additives;
- general food-labelling/FIC material;
- honey;
- preserved milk;
- fruit juice;
- jams;
- cocoa/chocolate;
- agricultural products;
- flavourings.
Do not interpret these scripts as one generic PDF-import framework.
Their assumptions differ by regulation.
Some search specific annexes or pages.
Some contain manually reviewed aliases.
Some verify exact or normalized wording.
Some model specific regulatory exceptions.
Reuse them only for the source they were designed for unless explicitly refactored and validated.

## PDF sources

When a source adapter reads PDF:
- keep the original document as primary evidence when that is the established project convention;
- preserve document identity and consolidation date;
- preserve language identity;
- detect missing or malformed extraction;
- do not infer official wording in another language;
- treat extracted text as evidence, not automatically as application knowledge.
PDF extraction may produce:
- UTF-8 review text;
- page-based excerpts;
- CSV;
- verified alias lists;
- other intermediate data.
Those outputs still require normal import validation.

## Structured datasets

Future imports may start directly from structured data such as:
- CSV;
- JSON;
- NDJSON;
- API snapshot;
- Git repository;
- taxonomy export.
Do not route these through PDF tooling.
For structured sources, validate:
- schema;
- identifiers;
- revision;
- completeness;
- language metadata;
- relationships;
- duplicate records;
- licensing;
- provenance.
Prefer preserving the original upstream identifiers separately from Is It Vegan canonical IDs when useful.

## Open Food Facts or similar taxonomies

Treat an external taxonomy as vocabulary evidence first.
Do not assume that:
- a taxonomy node maps one-to-one to an Is It Vegan concept;
- parent/child relationships imply vegan status;
- synonyms are safe aliases;
- multilingual terms are equivalent in every context;
- source classification semantics match Is It Vegan semantics.
Before import:
1. identify the exact dataset/snapshot;
2. establish reuse/license requirements;
3. inspect the upstream schema;
4. map candidate concepts without writing;
5. detect collisions with existing concepts and aliases;
6. preserve upstream relationships separately when they are not equivalent to Is It Vegan relationships;
7. classify only from evidence appropriate to Is It Vegan.
A useful taxonomy can enrich recognition without being trusted for vegan classification.

## Existing importer behavior

Several historical regulatory importers follow a useful pattern:
1. verify source evidence;
2. load current editorial knowledge;
3. check historical preservation assumptions;
4. detect canonical and multilingual alias collisions;
5. merge changes;
6. add or validate source metadata;
7. expose a non-writing mode;
8. write only when explicitly requested.
Use this as a design reference for future importers.
Do not copy regulation-specific thresholds, concept lists or special cases into unrelated importers.

## CLI variation

Existing importers are not uniform.
Examples of supported interfaces may include:

```text
--dry-run / --write
```

or:

```text
--check / --dry-run / --write
```

or an implicit dry-run with optional:

```text
--write
```

Builders may use `--check` to validate generated output rather than simulate an import.
Always inspect the tool before execution.
Never invent a command-line flag based on another importer.

## Source-specific semantics

Keep source-specific decisions inside the adapter/importer or source documentation.
Examples include:
- regulatory aliases;
- ambiguous legal names;
- categories whose composition can vary;
- deliberately excluded aliases;
- language-specific spellings;
- known structural extraction recovery;
- regulation-specific concept mapping.
The generic skill should not encode those facts as global rules.

## Creating a future importer

A new importer should ideally separate:

### Acquisition

Read and validate upstream material.

### Transformation

Convert upstream records into candidate Is It Vegan concepts/mappings.

### Validation

Detect:
- collisions;
- missing required fields;
- ambiguous ownership;
- invalid classifications;
- destructive changes.

### Mutation

Write only after candidate validation.

### Verification

Run project builders, tests and non-regression checks.
When practical, provide a non-writing mode.
Prefer deterministic and idempotent behavior.

## When to stop

Stop before mutation when:
- the dataset cannot be identified;
- licensing is unresolved;
- source revision is unknown and reproducibility matters;
- extraction is incomplete;
- mappings are ambiguous;
- alias collisions cannot be resolved;
- classification requires unsupported inference;
- historical data would be destroyed;
- validation tooling reports inconsistency.
A blocked import with a precise reason is preferable to a speculative successful import.
