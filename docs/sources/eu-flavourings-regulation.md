# Règlement UE sur les arômes — CELEX 02008R1334

Le règlement (CE) n° 1334/2008 définit les catégories d’arômes et certains ingrédients alimentaires possédant des propriétés aromatisantes. L’import utilise les quatre consolidations locales du 16.02.2026 en français, néerlandais, anglais et allemand, dans `reference-input/eu-food-labelling/01-core-labelling-terms/`.

`tools/import_eu_flavourings_regulation.py` vérifie chaque forme dans les pages 4–5 (article 3) du PDF de même langue. Il joint les langues de manière déterministe, refuse les placeholders et collisions, protège le plancher historique puis n’écrit dans `knowledge/` qu’avec `--write`.

Les catégories (`arôme`, substance ou préparation aromatisante, arôme thermique, de fumée, précurseur et autres catégories de l’article 3) restent `UNCERTAIN`. Le règlement prévoit des matières végétales, animales ou microbiologiques et ne prouve ni l’origine vegan d’une formulation ni la présence de l’aliment évoqué par un goût. `natural_flavouring` reste donc incertain ; « naturel » ne signifie pas vegan.

Le matcher protège localement les contextes d’arôme et de goût : une expression telle qu’`arôme chocolat`, `arôme fraise` ou `chocolate flavour` ne rattache pas le chocolat ou la fraise réels. `extrait de café` ne rattache pas automatiquement le café. Les traces restent séparées du verdict.

```text
python tools/import_eu_flavourings_regulation.py --dry-run
python tools/import_eu_flavourings_regulation.py --write
python tools/import_eu_flavourings_regulation.py --check
```
