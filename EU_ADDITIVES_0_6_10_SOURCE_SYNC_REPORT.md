# EU additives 0.6.10 — source sync report

## Decision

**GO.** The editorial source, generated Android assets, runtime tests and documentation are consistent for 0.6.10 (version code 47).

## Initial audit

- Git was clean on `master` before this work.
- `knowledge/ingredients.json` had 142 entries; `app/src/main/assets/ingredients.json` had 452. The 310-entry gap showed that prior data had been written to the generated asset.
- `knowledge/ingredient_aliases_multilingual.json` did not exist. The only lexicon was the Android asset, with 250 language blocks and 347 reviewed terms.
- `tools/build_ingredients.py` read only `knowledge/ingredients.json`; `tools/build_knowledge_docs.py` read the Android lexicon asset; `AndroidIngredientKnowledgeLoader` read both Android assets directly. Therefore the next ingredients generation would have discarded the asset-only additive concepts.

## Corrected sources and outputs

- `knowledge/ingredients.json` now has 452 editorial concepts, including all 338 importable EU keys. Existing IDs, statuses, reasons, sources and business aliases were retained.
- `knowledge/ingredient_aliases_multilingual.json` is now the source for reviewed language aliases. It has 1,538 concept/language blocks after consolidating pre-existing blocks.
- `tools/import_eu_additives_reference.py --write` migrates the former asset-only history once, imports by exact E number, writes only `knowledge/`, and is idempotent.
- `tools/build_ingredients.py` generates both `app/src/main/assets/ingredients.json` and `app/src/main/assets/ingredient_aliases_multilingual.json` from `knowledge/`.
- `tools/build_knowledge_docs.py` now reads the editorial lexicon source. `docs/generated/base-connaissances-data.md` was regenerated.

The resulting editorial and Android ingredient counts are both 452. Their status distribution is 114 `VEGAN`, 8 `VEGETARIAN`, 6 `NON_VEGAN` and 324 `UNCERTAIN`.

## Regulatory coverage and exclusions

- 340 regulatory keys were audited; 338 are importable and present in both source and asset.
- `E345` and `E345(i)` remain excluded because their relationship is ambiguous in the consolidated PDFs.
- `E322a` has FR/NL/EN aliases only; no German alias was invented.
- `E160b(i)`, `E322a`, `E470b`, `E572`, `E960a` and `E960b` remain distinct IDs. The documented `E470b`/`E572` textual collision remains `UNCERTAIN`.
- New additive concepts are `UNCERTAIN`; regulatory authorization was never converted into a vegan classification.

## Validation

Passed:

- `python tools/build_ingredients.py --check`
- `python tools/build_origin_rules.py --check`
- `python tools/build_knowledge_docs.py --check`
- generation with all three builders, followed by the same three checks
- a second import and generation: SHA-256 comparison of both sources, both generated knowledge assets and generated documentation produced no difference
- `./gradlew :mutation-core:test`
- `./gradlew testDebugUnitTest`
- `./gradlew assembleDebug`
- `./gradlew assembleDebugAndroidTest`
- `./gradlew connectedDebugAndroidTest` on Nokia G42 5G / Android 15
- `git diff --check`

The strengthened JVM test loads `knowledge/ingredients.json`, production assets and the production multilingual lexicon. It checks source-to-asset coverage, exclusions, exact E forms, suffixes, `UNCERTAIN` statuses, multilingual placement and legacy classifications.

## Remaining limits

The reference establishes authorization and official designations only. It does not establish ingredient origin or a vegan conclusion. `E345` and `E345(i)` await an unambiguous editorial source before any application import.

## Modified files

- `app/build.gradle.kts`
- `app/src/androidTest/java/com/example/isitvegan/RealLabelsInstrumentedTest.kt`
- `app/src/main/assets/ingredient_aliases_multilingual.json`
- `app/src/main/assets/ingredients.json`
- `app/src/test/java/com/example/isitvegan/EuAdditivesReference06910Test.kt`
- `docs/additifs.md`
- `docs/base-connaissances.md`
- `docs/changelog.md`
- `docs/generated/base-connaissances-data.md`
- `docs/sources/eu-additives-annex-ii-b.md`
- `knowledge/ingredient_aliases_multilingual.json`
- `knowledge/ingredients.json`
- `mkdocs.yml`
- `tools/build_ingredients.py`
- `tools/build_knowledge_docs.py`
- `tools/import_eu_additives_reference.py`
- `EU_ADDITIVES_0_6_10_SOURCE_SYNC_REPORT.md`
