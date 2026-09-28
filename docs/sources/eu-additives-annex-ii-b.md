# Extraction de l’annexe II-B — additifs UE

L’annexe II, partie B, du règlement (CE) n° 1333/2008 énumère les additifs alimentaires autorisés dans l’Union. Elle sert ici à reconnaître un numéro E ou une dénomination réglementaire ; elle ne démontre ni l’origine d’une matière première ni sa compatibilité vegan.

## Corpus et langues

L’extraction utilise CELEX `02008R1333`, consolidé au **18.08.2026**, dans les quatre PDF conservés sous `reference-input/eu-food-labelling/03-food-additives/` : français, néerlandais, anglais et allemand. Chaque édition fournit la dénomination officielle dans sa langue ; l’anglais facilite le contrôle transversal, sans être une traduction de remplacement.

Le résultat versionné est `EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv`. Les rapports d’acquisition et de complétion associés expliquent la provenance locale et les vérifications déjà effectuées.

## Extraction et jointure

`tools/extract_eu_additives_reference.py` extrait les clés de l’annexe et joint les quatre listes par **numéro E exact**. Les suffixes restent des clés distinctes : `E160b(i)`, `E322a`, `E960a` et `E960b` ne sont jamais fusionnés avec un numéro voisin. Une continuation de structure dans le PDF français omet le préfixe de `E960b` ; l’extracteur ne la rattache que lorsqu’elle suit `E960a` et que les trois autres PDF portent exactement `E960b`.

`E345` et `E345(i)` restent deux clés séparées, mais sont exclues de la base applicative : les PDF ne démontrent pas une correspondance non ambiguë. `E322a` est importé avec FR, NL et EN seulement : l’édition allemande ne fournit pas de dénomination officielle, donc aucune n’est inventée.

Les comptes attendus sont : 340 clés réglementaires, 338 lignes importables, 337 lignes complètes dans les quatre langues, 1 ligne partielle (`E322a`) et 2 lignes ambiguës exclues (`E345`, `E345(i)`).

## Import éditorial et génération

`tools/import_eu_additives_reference.py --write` synchronise les concepts, statuts, raisons, sources et numéros E vers `knowledge/ingredients.json`. Les dénominations FR/NL/EN/DE sont ajoutées à `knowledge/ingredient_aliases_multilingual.json`, puis `tools/build_ingredients.py` produit les deux assets Android. Les nouveaux concepts demeurent `UNCERTAIN` : une autorisation UE ne vaut jamais classification vegan.

```powershell
python tools/extract_eu_additives_reference.py --output reference-input/eu-food-labelling/03-food-additives/EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv
python tools/import_eu_additives_reference.py --write
python tools/build_ingredients.py
python tools/build_knowledge_docs.py
python tools/build_origin_rules.py
python tools/build_ingredients.py --check
python tools/build_origin_rules.py --check
python tools/build_knowledge_docs.py --check
```

L’import et les trois générateurs sont idempotents : une seconde exécution laisse les fichiers inchangés. Les tests JVM vérifient la chaîne `knowledge → asset → runtime`, les 338 lignes, les exclusions, les formes `E330` et `E 330` (jamais `330` seul), les suffixes exacts et la collision réglementaire documentée entre `E470b` et `E572`.
