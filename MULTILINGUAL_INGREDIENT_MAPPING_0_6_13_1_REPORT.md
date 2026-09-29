# Multilingual ingredient mapping 0.6.13.1

## Decision

GO pending the final Gradle invocation already running. The generated mapping is derived from the sole editorial lexicon `knowledge/ingredient_aliases_multilingual.json`; no second JSON source was created.

## Audit

- 393 canonical concepts audited: all concepts with multilingual aliases, `honey`, `milk`, `cream`, and all concepts with `possibleOriginNote`.
- Alias forms: FR 445, NL 474, EN 396, DE 426; historical IT 34 and ES 33 remain supported and visible as extra aliases.
- No collisions, no `?` placeholders, and source/asset byte parity is true.
- One deliberate alert remains: `cereals` / NL / `granen` is a historical alias for an absent generic concept, therefore non-classifiable and without source. It is documented, not silently mapped.

## Generated model

`tools/build_multilingual_ingredient_mapping.py` creates `docs/generated/multilingual-ingredient-mapping.md`. Each record exposes surface form, language, concept id, mapping group, relation, normalized form, inherited canonical sources, and confidence. Relations distinguish reviewed common labels, regulatory aliases, and OCR variants. `honey`, `milk`, and `cream` retain distinct groups and excluded contexts.

## Verification

The tool verifies canonical availability, supported language, normalized-form collisions, placeholders, source/asset parity and generated-document idempotence. `MultilingualIngredientMappingTest` loads production assets and verifies runtime matching for honey, preserved milk, cream powder, E470b and E960a, plus source-to-asset parity.

## Files

- `app/build.gradle.kts`
- `docs/base-connaissances.md`, `docs/changelog.md`
- `docs/generated/multilingual-ingredient-mapping.md`
- `tools/build_multilingual_ingredient_mapping.py`
- `app/src/test/java/com/example/isitvegan/MultilingualIngredientMappingTest.kt`
- `MULTILINGUAL_INGREDIENT_MAPPING_0_6_13_1_REPORT.md`

## Correction editoriale

Le fichier `knowledge/ingredient_aliases_multilingual.json` est maintenant effectivement modifie. Il contient 1 808 enregistrements `mappings` structures, synchronises avec chaque forme de `aliases` et `ocrVariants`; l asset genere contient les memes 1 808 enregistrements.

| Concept | FR | NL | EN | DE | Total |
|---|---:|---:|---:|---:|---:|
| honey | 10 | 20 | 11 | 12 | 53 |
| milk | 22 | 19 | 23 | 21 | 85 |
| cream | 1 | 1 | 0 | 2 | 4 |
| Directive lait | 23 | 20 | 23 | 23 | 89 |

Les 1 651 autres mappings couvrent les alias reglementaires et historiques, y compris les additifs UE importes. Avant cette correction, le champ `mappings` etait absent (0); apres, il est de 1 808. La parite knowledge -> asset est verifiee octet par octet et le generateur est idempotent.
