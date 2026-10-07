# Intégration ciblée UE 1308/2013 — viandes par espèce

Date : 2026-10-07. Révision initiale vérifiée : `fc74f08aa87e22c0fe38cf896a1a323c64cbf7ea`. L’état initial comportait six rapports/CSV v0.7 non suivis ; ils ont été préservés. L’import a utilisé uniquement les décisions `CONSERVER` du rapport ciblé et leurs lignes correspondantes du CSV de proposition.

## Résultat

Import réussi pour les cinq concepts autorisés : `bovine_meat`, `pork_meat`, `sheep_meat`, `goat_meat`, `horse_meat`. Chaque concept a le statut ingredient `NON_VEGAN`, une provenance vers `eu-agricultural-products-regulation-1308-2013-20260818`, ainsi qu’un mapping multilingue sourcé donnant fichier PDF, langue, page, rubrique, offset, forme exacte, ID de revue et SHA-256 du PDF. Aucun enregistrement de source n’a été ajouté : la source CELEX 02013R1308, consolidation du 18-08-2026, était déjà déclarée.

Les alias importés sont uniquement les 17 formes suivantes :

| Langue | Concept → forme exacte |
|---|---|
| FR (3) | `bovine_meat` → `Viandes des animaux de l'espèce bovine` ; `pork_meat` → `Viandes des animaux de l'espèce porcine domestique` ; `horse_meat` → `Viandes de cheval` |
| NL (5) | `bovine_meat` → `Vlees van runderen` ; `pork_meat` → `Vlees van varkens` ; `sheep_meat` → `schapenvlees` ; `goat_meat` → `geitenvlees` ; `horse_meat` → `Vlees van paarden` |
| EN (6) | `bovine_meat` → `Meat of bovine animals` ; `pork_meat` → `Meat of domestic swine` ; `sheep_meat` → `Sheepmeat` ; `goat_meat` → `goatmeat` ; `horse_meat` → `Meat of horses`, `Horsemeat` |
| DE (3) | `bovine_meat` → `Fleisch von Rindern` ; `goat_meat` → `Ziegenfleisch` ; `horse_meat` → `Fleisch von Pferden` |

Les langues absentes des décisions sont restées absentes. Le nom canonique de chaque concept est l’une des formes anglaises attestées de la liste ; il n’ajoute donc aucune forme de matching extérieure aux preuves. Le lot ajoute 12 alias canoniques aux listes `aliases` (les cinq autres formes sont les noms canoniques) et 17 entrées d’alias/mapping langue.

Les formes courtes historiques restent sous `meat`. Cela comprend notamment `viande bovine`, `Rundvlees`, `beef`, `Rindfleisch`, `viande de porc`, `varkensvlees`, `pork`, `pigmeat`, `Schweinefleisch` et les collectifs ovins/caprins. `pigmeat` reste reconnu exactement par son propriétaire `meat`. Aucun alias n’a été réaffecté. Les concepts veau, volaille, sang, saucisse, jambon/poitrine et graisse animale n’ont pas été intégrés.

## Fichiers modifiés

- `knowledge/ingredients.json` — cinq concepts `NON_VEGAN` ajoutés.
- `knowledge/ingredient_aliases_multilingual.json` — 17 entrées d’alias et 17 mappings de provenance.
- `tools/import_eu_agricultural_products_regulation.py` — ajout du mode borné `--meat-species` au même adaptateur 1308 existant ; preuve PDF par SHA-256, page et offset, contrôles de collision/preservation et contrôle idempotent.
- `app/src/main/assets/ingredients.json` et `app/src/main/assets/ingredient_aliases_multilingual.json` — sorties reconstruites depuis les sources éditoriales.
- `docs/generated/multilingual-ingredient-mapping.md` et `docs/generated/base-connaissances-data.md` — documentation générée mise à jour.
- `app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt` — assertions du compte de mappings et test des 17 formes, du matching et du verdict détaillé.
- Ce rapport d’intégration.

Aucun enum, schéma de connaissance ou moteur de verdict n’a changé. Aucun commit n’a été créé.

## Validations exécutées

- État initial : `git status --short --branch`, `git rev-parse HEAD`, `git diff --stat` — branche `master`, HEAD ci-dessus ; les rapports v0.7 préexistants étaient non suivis.
- `python tools/build_ingredients.py --check` avant l’import — réussite, actifs valides et à jour.
- `python tools/build_multilingual_ingredient_mapping.py --check` et `--report` avant l’import — réussite, 0 collision, actifs synchronisés.
- `python tools/import_eu_agricultural_products_regulation.py --meat-species --dry-run` avant écriture — réussite : 5 concepts, 12 alias canoniques additionnels, 17 alias de langue, 17 mappings, 0 nouvelle source ; les 17 formes correspondent aux offsets déclarés dans les PDF locaux empreintés.
- `python tools/import_eu_agricultural_products_regulation.py --meat-species --write` — écriture limitée aux deux JSON de connaissance.
- `python tools/build_ingredients.py` et `python tools/build_multilingual_ingredient_mapping.py --write` — actifs et mapping documentaire régénérés.
- `python tools/build_knowledge_docs.py --check` a d’abord détecté le document canonique périmé ; `python tools/build_knowledge_docs.py --write`, puis `--check` — réussite.
- `python -m py_compile tools/import_eu_agricultural_products_regulation.py` — réussite.
- `python tools/import_eu_agricultural_products_regulation.py --meat-species --check` — réussite, `changes=0`, lot présent et provenance conforme.
- `python tools/build_ingredients.py --check` et `python tools/build_multilingual_ingredient_mapping.py --check` après génération — réussite.
- `.\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.AgriculturalProductsRegulationImportTest` — `BUILD SUCCESSFUL`.
- Vérification ciblée JSON — 5 concepts, tous `NON_VEGAN` ; mappings par langue FR 3, NL 5, EN 6, DE 3.
- `git diff --check` — aucune erreur d’espacement dans les fichiers suivis modifiés.

La suite Gradle complète et les tests de mutation n’ont pas été lancés. L’importeur n’ajoute qu’un nouveau mode explicite au script réglementaire existant ; son ancien mode `--animal-enrichment` reste indépendant et son contrôle est encore couvert par le test ciblé.

## Limites et risques résiduels

Les décisions et le matcher avertissent que des alias de viande peuvent apparaître dans des descriptions, saveurs ou produits végétaux. Les formes importées sont les formulations longues qualifiées retenues ; cette granularité réduit les collisions connues sans prouver qu’aucun libellé commercial ne sera ambigu. Le test valide leur résolution déterministe et leur verdict, pas l’interprétation sémantique de toutes les étiquettes.

La source locale utilisée est la consolidation 2026-08-18 déclarée dans le registre. La provenance et les empreintes ont été contrôlées par l’importeur ; les pages n’ont pas fait l’objet d’un nouveau rendu visuel dans cette intégration. La licence/réutilisation n’a pas été réévaluée ; aucun nouveau statut ou attribut de licence n’a été inventé. Le chevauchement bovin/veau reste ouvert et `veal_meat` est volontairement exclu, comme demandé.
