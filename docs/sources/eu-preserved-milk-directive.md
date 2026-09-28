# Directive UE sur les laits conservés — CELEX 02001L0114

La directive 2001/114/CE définit des laits de conserve partiellement ou totalement déshydratés. Elle sert à reconnaître leurs dénominations réglementaires, alors que le statut alimentaire reste une décision éditoriale du projet : les produits laitiers concernés sont `VEGETARIAN` et incompatibles avec un verdict vegan.

## Sources locales

L’import prend comme référence primaire les quatre consolidations locales du 14.06.2026, dans `reference-input/eu-food-labelling/02-sector-product-standards/` :

- `CELEX_02001L0114-20260614_FR_TXT.pdf`
- `CELEX_02001L0114-20260614_NL_TXT.pdf`
- `CELEX_02001L0114-20260614_EN_TXT.pdf`
- `CELEX_02001L0114-20260614_DE_TXT.pdf`

La [version consolidée EUR-Lex](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02001L0114-20260614) sert uniquement de contrôle externe de CELEX, du titre et de la consolidation ; elle ne remplace pas les PDF locaux.

## Extraction

L’annexe I (pages 4 et 5 des quatre PDF) définit les laits partiellement déshydratés et totalement déshydratés : lait concentré non sucré ou sucré, lait évaporé, poudres de lait entières, partiellement écrémées ou écrémées. L’annexe II (page 7) ajoute des dénominations particulières, notamment `evaporated milk`, `koffiemelk` et les variantes demi-écrémées.

`tools/import_eu_preserved_milk_directive.py` utilise `pypdf`, normalise Unicode et les césures d’extraction de façon contrôlée, vérifie chaque chaîne contre le PDF de sa langue et refuse les collisions d’alias. Il joint les quatre langues de façon déterministe aux concepts existants `milk` et, pour les désignations de l’annexe II explicitement rattachées à la crème en poudre, `cream`.

```text
python tools/import_eu_preserved_milk_directive.py --dry-run
python tools/import_eu_preserved_milk_directive.py --write
python tools/import_eu_preserved_milk_directive.py --dry-run
```

Le mode `--write` ne modifie que `knowledge/ingredients.json`, `knowledge/ingredient_aliases_multilingual.json` et `knowledge/sources.json`. `tools/build_ingredients.py` génère ensuite les assets Android.

## Limites de reconnaissance

Une désignation réglementaire de lait est différente d’un mot marketing. Les arômes ou goûts de lait, les boissons végétales, les substituts tels que le lait de soja et les simples formulations `contient du lait` ne sont pas ajoutés comme alias du concept `milk`. La déclaration de présence réelle conserve son traitement existant distinct. La directive ne sert pas à importer crème, beurre, fromage, yaourt, lactosérum, caséine, lactose isolé, protéines de lait, lait infantile ou desserts lactés lorsque ces familles ne sont pas une dénomination importée par ce flux.
