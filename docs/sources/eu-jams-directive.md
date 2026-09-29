# Directive UE sur les confitures â€” CELEX 02001L0113

La directive 2001/113/CE définit les catégories réglementaires de confitures,
gelées, marmelades d’agrumes et crème de marrons sucrée. Elle alimente la
reconnaissance multilingue des concepts `fruit_jam`, `fruit_jelly`,
`citrus_marmalade` et `sweetened_chestnut_puree`. Ces quatre définitions de
l’annexe I ne contiennent que des matières végétales, de l’eau et des sucres :
elles sont donc classées `VEGAN` dans cette base.

Cette conclusion est limitée aux catégories précisément définies. Un produit
commercial, une préparation de fruits, un arôme, un goût de fruit ou une simple
mention marketing ne devient pas vegan par rapprochement textuel.

## Sources locales et extraction

L’import utilise les consolidations locales du 14.06.2026, stockées dans
`reference-input/eu-food-labelling/02-sector-product-standards/` :

- `CELEX_02001L0113-20260614_FR_TXT.pdf`
- `CELEX_02001L0113-20260614_NL_TXT.pdf`
- `CELEX_02001L0113-20260614_EN_TXT.pdf`
- `CELEX_02001L0113-20260614_DE_TXT.pdf`

L’annexe I, pages 6 à 7, est extraite avec `pypdf`. Chaque dénomination est
normalisée pour vérifier sa présence dans son PDF de langue, puis jointe de façon
déterministe au concept approprié. La page [EUR-Lex consolidée](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02001L0113-20260614)
sert uniquement de contrôle externe.

`tools/import_eu_jams_directive.py` refuse une langue absente, un placeholder,
une collision d’alias ou une tentative de reconstruction destructive. Il vérifie
également la présence des 455 concepts et des 1 834 mappings antérieurs avant
toute écriture.

```text
python tools/import_eu_jams_directive.py --dry-run
python tools/import_eu_jams_directive.py --write
python tools/import_eu_jams_directive.py --check
python tools/build_ingredients.py
python tools/build_multilingual_ingredient_mapping.py --write
```

Les formes génériques nationales « marmelade » et « Marmelade » ne sont pas
importées seules : la directive les admet aussi comme désignation de confiture
dans certains territoires, tout en définissant distinctement la marmelade
d’agrumes. Les formes explicites `marmelade d’agrumes`, `citrus marmalade` et
leurs équivalents sont conservées sans cette ambiguïté.
