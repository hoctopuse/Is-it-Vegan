# Import CELEX 02001L0113 — 0.6.13.3

## Décision

GO sous réserve des validations listées à la fin de ce rapport. L’import est
additif : il part de 455 concepts et 1 834 mappings, et aboutit à 459 concepts
et 1 864 mappings sans modifier les concepts historiques.

## Sources et extraction

Source primaire : les quatre consolidations locales du 14.06.2026, dans
`reference-input/eu-food-labelling/02-sector-product-standards/` :

- `CELEX_02001L0113-20260614_FR_TXT.pdf`
- `CELEX_02001L0113-20260614_NL_TXT.pdf`
- `CELEX_02001L0113-20260614_EN_TXT.pdf`
- `CELEX_02001L0113-20260614_DE_TXT.pdf`

CELEX est `02001L0113`. Les définitions et dénominations utilisées sont dans
l’annexe I, pages 6–7 de chaque PDF. EUR-Lex est un contrôle externe de la
consolidation, pas la source de l’extraction.

`tools/import_eu_jams_directive.py` extrait le texte UTF-8 avec `pypdf`,
normalise Unicode et les césures pour rechercher chaque chaîne dans son PDF,
joint les quatre langues de façon déterministe, refuse les collisions et refuse
l’écriture si les 455 concepts ou les 1 834 mappings précédents ne sont plus
présents.

## Concepts et classifications

| Concept | Statut | Justification de la classification |
|---|---|---|
| `fruit_jam` | `VEGAN` | L’annexe I définit la confiture par sucres, pulpe et/ou purée de fruits, eau. |
| `fruit_jelly` | `VEGAN` | L’annexe I définit la gelée par sucres, jus et/ou extraits aqueux de fruits. |
| `citrus_marmalade` | `VEGAN` | L’annexe I définit la marmelade d’agrumes par eau, sucres et produits d’agrumes. |
| `sweetened_chestnut_puree` | `VEGAN` | L’annexe I définit la crème de marrons sucrée par eau, sucre et purée de châtaignes. |

Ces classifications ne couvrent que les catégories réglementaires. Une
préparation composée non définie, un produit marketing, un arôme ou un « goût
de fruit » reste évalué par sa liste d’ingrédients réelle ; ils ne sont pas
assimilés à ces concepts.

## Alias réglementaires ajoutés

30 alias sont ajoutés, tous vérifiés directement dans leur PDF local.

| Concept | FR | NL | EN | DE |
|---|---:|---:|---:|---:|
| `fruit_jam` | 2 | 4 | 2 | 2 |
| `fruit_jelly` | 2 | 2 | 2 | 2 |
| `citrus_marmalade` | 2 | 2 | 2 | 2 |
| `sweetened_chestnut_puree` | 1 | 1 | 1 | 1 |
| **Total** | **7** | **9** | **7** | **7** |

Chaque alias est également présent dans `mappings` avec le groupe
`fruit-jams-directive`, la relation `REGULATORY_ALIAS`, la source CELEX et la
confiance `REVIEWED`.

Les concepts restent volontairement distincts de `fruit_juice`, `fruit_puree`,
`fruit_nectar`, des fruits frais et séchés, des fruits à coque, des arômes et
des préparations de fruits. Les mots génériques `marmelade` et `Marmelade` ne
sont pas importés seuls : la directive les admet aussi comme appellation de
confiture dans certains territoires tout en définissant une marmelade d’agrumes
distincte. Les formes sans ambiguïté sont importées.

## Idempotence et parité

- premier `--write` : `changes=1` ;
- second passage `--check` : `changes=0` ;
- `tools/build_ingredients.py` produit les assets Android depuis `knowledge/` ;
- le test vérifie l’égalité octet à octet entre le lexique éditorial et l’asset ;
- le générateur du mapping reconstruit la page publiée depuis cette même source.

## Limites

La directive ne prouve pas qu’un produit commercial portant un nom voisin suit
la recette réglementaire, ni qu’une recette hors champ est vegan. Elle ne sert
pas à classer les arômes, nectars, préparations ou produits composés. Les
variantes non présentes textuellement dans les quatre PDF ne sont pas ajoutées.

## Validations

- `./gradlew :mutation-core:test` : succès ;
- `./gradlew testDebugUnitTest` : succès, y compris `JamsDirectiveImportTest` ;
- `./gradlew assembleDebug` : succès ;
- `./gradlew assembleDebugAndroidTest` : succès ;
- `python tools/import_eu_jams_directive.py --check` : `changes=0` ;
- `python tools/build_ingredients.py --check` : asset à jour ;
- `python tools/build_multilingual_ingredient_mapping.py --check` : document à jour ;
- `git diff --check` : succès (avertissements LF/CRLF seulement).
