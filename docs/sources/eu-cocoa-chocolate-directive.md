# Directive UE cacao et chocolat — CELEX 02000L0036

La directive 2000/36/CE définit les dénominations de vente et les caractéristiques des produits de cacao et de chocolat. L’import utilise les consolidations locales du 18.11.2013 en français, néerlandais, anglais et allemand, dans `reference-input/eu-food-labelling/02-sector-product-standards/`.

L’annexe I établit notamment le beurre de cacao, le cacao en poudre, le chocolat en poudre, le chocolat, le chocolat au lait, le chocolat blanc, le chocolat fourré et les pralines. `tools/import_eu_cocoa_chocolate_directive.py` vérifie chaque forme dans son PDF de même langue, refuse les collisions, protège le plancher historique et n’écrit `knowledge/` qu’avec `--write`.

La directive établit l’identité réglementaire, pas le caractère vegan automatique d’un produit commercial. Le cacao, le beurre de cacao et le chocolat en poudre sont importés `VEGAN` suivant leurs définitions. Le chocolat générique et les garnitures restent `UNCERTAIN`, tandis que les dénominations de chocolat au lait et de chocolat blanc sont `VEGETARIAN` car leur définition exige du lait ou des produits laitiers. Les traces restent gérées séparément par le moteur existant.

Les termes cacao, beurre de cacao et chocolat ne sont pas fusionnés. Les formes néerlandaise `cacaoboter` et allemande `Kakaobutter`, autrefois rattachées au cacao générique, sont conservées mais corrigées vers le concept réglementaire précis `cocoa_butter`.
