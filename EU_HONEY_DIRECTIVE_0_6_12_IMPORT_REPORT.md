# Import de la directive UE sur le miel — 0.6.12

## Décision

**GO, après validations.** Le concept existant `honey` est enrichi sans créer de
concepts pour chaque présentation réglementaire. Il reste `VEGETARIAN`, incompatible
avec un verdict `VEGAN`.

## Sources primaires et contrôle

PDF locaux utilisés, tous consolidés le 14.06.2026 :

- `CELEX_02001L0110-20260614_FR_TXT.pdf`
- `CELEX_02001L0110-20260614_NL_TXT.pdf`
- `CELEX_02001L0110-20260614_EN_TXT.pdf`
- `CELEX_02001L0110-20260614_DE_TXT.pdf`

Ils se trouvent dans `reference-input/eu-food-labelling/02-sector-product-standards/`.
La cohérence a été contrôlée avec EUR-Lex pour CELEX `02001L0110`, sans substituer
la page distante aux PDF locaux. La source éditoriale
`eu-honey-directive-2001-110-20260614` enregistre CELEX, institution, URL,
consolidation, langues, chemins locaux, rôle et limites.

## Extraction

L’annexe I, « Dénominations, descriptions et définitions des produits », est à la
page 8 des quatre PDF. Son point 1 définit le miel comme une substance produite,
transformée, déposée, déshydratée, stockée et mûrie par les abeilles *Apis mellifera*.
Les points 2 et 3 définissent les variantes de dénomination.

`tools/import_eu_honey_directive.py` utilise `pypdf`, normalise le texte Unicode et
les césures, vérifie chaque terme contre son PDF local, détecte les collisions et
joint les langues vers `honey`. `--dry-run` ne modifie rien ; `--write` ne touche que
les trois fichiers `knowledge/`. Après écriture, un second `--dry-run` retourne
`changes=0`.

## Concept et classification

`honey` existait déjà ; aucun doublon n’a été créé. Sa justification devient :
« Produit et transformé par les abeilles ; origine animale au sens du projet,
végétarien mais non vegan. » Son statut reste `VEGETARIAN` et sa provenance ajoute la
directive UE, sans supprimer Vegan Society ni Vegetarian Society.

Cette classification est distincte de l’identité réglementaire : la directive établit
le miel réglementaire, tandis que le projet évalue son origine animale. Un produit
composé reste analysé par ses ingrédients déclarés ; une occurrence de miel y bloque
un verdict vegan.

## Alias joints

| Langue | Alias réglementaires ajoutés |
|---|---|
| FR | miel ; miel de fleurs ; miel de nectars ; miel de miellat ; miel en rayons ; miel avec morceaux de rayons ; miel égoutté ; miel centrifugé ; miel pressé ; miel destiné à l’industrie |
| NL | honing/honig ; bloemenhoning/bloemenhonig ; nectarhoning/nectarhonig ; honingdauwhoning/honingdauwhonig ; raathoning/raathonig ; brokhoning/brokhonig ; raatbrokken in honing/honig ; lekhoning/lekhonig ; slingerhoning/slingerhonig ; pershoning/pershonig ; bakkershoning |
| EN | honey ; blossom honey ; nectar honey ; honeydew honey ; comb honey ; chunk honey ; cut comb in honey ; drained honey ; extracted honey ; pressed honey ; Baker’s honey |
| DE | Honig ; Blütenhonig ; Nektarhonig ; Honigtauhonig ; Wabenhonig ; Scheibenhonig ; Honig mit Wabenteilen ; Wabenstücke in Honig ; Tropfhonig ; Schleuderhonig ; Presshonig ; Backhonig |

Les 53 chaînes vérifiées sont conservées à la fois dans le lexique multilingue avec
leur langue et comme alias canoniques de matching. L’écriture évite tout doublon.

## Non-créations et ambiguïtés

Les variantes de production et présentation sont des alias du même produit et ne
forment pas de concepts distincts. « Miel filtré » ne figure pas dans l’annexe I de
la consolidation examinée et n’est pas créé. Pollen, propolis, gelée royale, cire
d’abeille, arôme/goût de miel et mentions marketing restent distincts. Le matcher
écarte explicitement les formulations d’arôme ou de goût de miel de l’identité
`honey`.

## Décomptes

| Mesure | Avant | Après |
|---|---:|---:|
| Concepts | 452 | 452 |
| Concepts `VEGAN` | 119 | 119 |
| Concepts `VEGETARIAN` | 10 | 10 |
| Concepts `NON_VEGAN` | 7 | 7 |
| Concepts `UNCERTAIN` | 316 | 316 |
| Concept miel | 1 | 1 |
| Alias réglementaires joints | 0 | 53 |

## Validations

Réussites : `--dry-run` avant/après `--write` (après écriture : `changes=0`), les
trois générateurs et leurs contrôles `--check`, `:mutation-core:test`,
`testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest` et `git diff --check`.
Les tests connectés ne sont pas relancés dans cette passe ; la validation JVM et les
assemblages Android ne sont pas bloqués.

## Fichiers modifiés

- `knowledge/ingredients.json`, `knowledge/ingredient_aliases_multilingual.json`, `knowledge/sources.json`
- `app/src/main/assets/ingredients.json` et `app/src/main/assets/ingredient_aliases_multilingual.json` (générés)
- `tools/import_eu_honey_directive.py`
- `mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt`
- `app/src/test/java/com/example/isitvegan/HoneyDirectiveImportTest.kt`
- `docs/additifs.md`, `docs/base-connaissances.md`, `docs/changelog.md`, `docs/generated/base-connaissances-data.md`
- `docs/sources/eu-honey-directive.md`, `mkdocs.yml`
- `app/build.gradle.kts`, `EU_HONEY_DIRECTIVE_0_6_12_IMPORT_REPORT.md`
