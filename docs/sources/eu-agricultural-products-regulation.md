# Règlement UE sur les produits agricoles — CELEX 02013R1308

Le règlement (UE) n° 1308/2013 organise les marchés des produits agricoles. L’import utilise les quatre consolidations locales du 18.08.2026 (`FR`, `NL`, `EN`, `DE`) dans `reference-input/eu-food-labelling/02-sector-product-standards/`.

Les parties XV à XX de l’annexe I documentent viandes bovines et porcines, viandes ovines et caprines, lait, œufs et volaille. Les parties IX et X couvrent fruits, légumes et produits transformés. L’annexe VII documente notamment les matières grasses tartinables.

`tools/import_eu_agricultural_products_regulation.py` vérifie chaque forme contre son PDF, joint les quatre langues, refuse collisions et pertes historiques, et n’écrit qu’avec `--write`. Une catégorie réglementaire variable reste `UNCERTAIN`.

L’article 1 exclut explicitement les produits de la pêche et de l’aquaculture. Cette source ne crée donc aucun concept de poisson, crustacé ou mollusque.
