# Directive UE sur le miel — CELEX 02001L0110

La directive 2001/110/CE définit le produit réglementaire « miel ». Cette source
sert à reconnaître les dénominations du produit, puis la base éditoriale applique
séparément la classification alimentaire du projet : `VEGETARIAN`, donc non vegan.

## Sources locales

L’import utilise comme référence primaire les consolidations du 14.06.2026 :

- `CELEX_02001L0110-20260614_FR_TXT.pdf`
- `CELEX_02001L0110-20260614_NL_TXT.pdf`
- `CELEX_02001L0110-20260614_EN_TXT.pdf`
- `CELEX_02001L0110-20260614_DE_TXT.pdf`

Elles sont stockées dans `reference-input/eu-food-labelling/02-sector-product-standards/`.
La page [EUR-Lex consolidée](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02001L0110-20260614)
sert de contrôle de cohérence, sans remplacer les PDF locaux.

## Extraction

L’annexe I, page 8 de chaque PDF, contient la définition et les dénominations :
origine florale/nectar, miellat, rayons, morceaux de rayons, égouttage,
centrifugation/extraction, pressage et miel de boulangerie. L’outil
`tools/import_eu_honey_directive.py` extrait le texte UTF-8, vérifie chaque forme
contre son PDF, joint les langues à l’identifiant canonique `honey` et refuse les
collisions d’alias.

```text
python tools/import_eu_honey_directive.py --dry-run
python tools/import_eu_honey_directive.py --write
python tools/import_eu_honey_directive.py --dry-run
```

L’outil n’écrit que dans `knowledge/ingredients.json`,
`knowledge/ingredient_aliases_multilingual.json` et `knowledge/sources.json`.
`tools/build_ingredients.py` produit ensuite l’asset Android.

La consolidation consultée ne contient pas la dénomination « miel filtré » dans
l’annexe I : elle n’est donc pas ajoutée. Pollen, propolis, gelée royale, cire
d’abeille, arômes de miel et mentions marketing restent des concepts distincts.
