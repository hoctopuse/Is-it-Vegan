---
name: knowledge-import-validation
description: Validate and safely integrate external knowledge into Is It Vegan. Use for knowledge imports, external datasets, taxonomies, regulatory data, Open Food Facts data, CSV/JSON/API snapshots, source-specific importers, alias additions, concept additions, multilingual mappings, provenance checks, collision analysis, knowledge non-regression, or generated asset parity.
---

# Knowledge import validation

Use this skill whenever external information may modify the Is It Vegan knowledge base.

Read:

- `references/import-workflow.md`
- `references/source-adapters.md`

before performing a real import.

## Scope

This skill applies regardless of source format.

Possible sources include:

- JSON or CSV datasets;
- API snapshots;
- external repositories or taxonomies;
- Open Food Facts data;
- regulatory datasets;
- reviewed structured exports;
- PDFs processed through a source-specific extractor.

PDF extraction is not the generic import workflow.

The generic workflow begins when source evidence or structured candidate data can be inspected and mapped into the Is It Vegan knowledge model.

## Core principles

- Never import from an unidentified or non-reproducible source.
- Establish provenance before modifying knowledge.
- Record source revision, version, snapshot date, or equivalent stable identifier when available.
- Establish license and attribution requirements before importing third-party datasets.
- Do not infer missing licensing terms.
- Prefer existing repository importers and validators over ad-hoc modification.
- Inspect a tool's current CLI before invoking it.
- Prefer dry-run or check modes before write mode when supported.
- Do not assume all repository importers expose the same command-line modes.
- Never treat a successful importer execution alone as proof that an import is valid.
- Preserve historical concept IDs and established semantics.
- Do not silently replace the complete knowledge base when a merge is sufficient.
- Detect collisions and ambiguity before accepting aliases or mappings.
- Do not convert uncertainty into a confident classification without evidence.
- Generated Android assets are outputs, not editorial knowledge sources.
- Validate knowledge-to-asset parity after a successful import.

## Editorial sources

The primary editable knowledge files include:

- `knowledge/ingredients.json`
- `knowledge/ingredient_aliases_multilingual.json`
- `knowledge/origin_qualifier_rules.json`
- `knowledge/sources.json`

Generated Android assets under `app/src/main/assets/` must normally be derived from these editorial sources.

Do not edit generated assets directly to perform a knowledge import.

## Import safety

Before any write:

1. inspect Git state;
2. identify the source and its provenance;
3. inspect any existing source-specific importer or extractor;
4. determine the tool's supported modes;
5. perform read-only validation or dry-run when available;
6. inspect the proposed concepts, aliases, mappings, sources and classifications;
7. detect collisions, removals and unexpected status changes;
8. only write when the intended transformation is understood.

Do not use destructive Git operations to prepare an import.

Preserve unrelated user changes.

## Classification safety

External vocabulary does not automatically establish vegan status.

Distinguish between:

- recognition of a term;
- identification of a regulatory or taxonomy category;
- evidence about composition;
- evidence about origin;
- vegan or vegetarian classification.

A source that proves a name exists does not necessarily prove the product is vegan.

When origin or composition remains variable, preserve an uncertain classification rather than inventing certainty.

## Multilingual safety

Do not infer that a term in one language is an official term in another language unless the source supports that relation.

Preserve language metadata.

Do not silently assign one normalized surface form to multiple concepts.

Ambiguity must be:

- rejected;
- explicitly modelled;
- or retained as documented uncertainty.

## Historical preservation

An import must not silently:

- delete existing concept IDs;
- remove established aliases;
- change existing statuses;
- overwrite reasons or provenance;
- reassign aliases to another concept;
- discard multilingual mappings.

If a deliberate migration requires one of these actions, document and validate it explicitly.

## Validation after import

After writing knowledge:

1. inspect the Git diff;
2. run the relevant repository builders and validators;
3. verify generated Android assets;
4. verify multilingual mapping consistency when aliases changed;
5. verify origin-rule consistency when origin rules changed;
6. verify generated knowledge documentation when relevant;
7. run relevant business tests;
8. perform knowledge non-regression checks appropriate to the scope.

Do not broaden validation blindly. Select validators based on the files changed.

## Existing tools

Repository tools are the authoritative implementation.

Do not copy them into this skill.

Discover and inspect tools under `tools/` before running them.

Important generic builders currently include:

- `build_ingredients.py`
- `build_multilingual_ingredient_mapping.py`
- `build_origin_rules.py`
- `build_knowledge_docs.py`

Source-specific extractors and importers may also exist.

Their presence does not make them generic.

## Reports

Import reports belong under the relevant version/workstream directory in `reports/`.

A useful report distinguishes:

- source facts;
- proposed mappings;
- accepted changes;
- rejected or ambiguous candidates;
- validation results;
- unresolved licensing or provenance issues.

Do not represent an aborted or blocked import as completed.

## Result reporting

Report concisely:

- source used;
- revision/snapshot/date when applicable;
- license/provenance state;
- importer or extractor used;
- execution mode;
- concepts added or changed;
- aliases/mappings added or changed;
- collisions or ambiguities;
- classifications changed;
- validation commands actually executed;
- generated assets checked;
- non-regression result;
- remaining uncertainty.

Clearly distinguish measured repository state from assumptions.