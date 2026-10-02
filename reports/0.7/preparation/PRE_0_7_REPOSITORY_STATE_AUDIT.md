# Audit de l'état du dépôt avant préparation de la version 0.7

Date de l'audit : 2026-09-30  
Dépôt : `Is-it-Vegan`  
Portée : inspection locale en lecture seule. Aucun test Gradle, commit, push, reset ou nettoyage destructif n'a été lancé.

## 1. État Git

- Branche courante : `master`.
- Suivi distant : `master...origin/master` ; HEAD et `origin/master` pointent sur `2229018`.
- HEAD : `feat(knowledge): complete EU food knowledge import cycle`.
- Arbre de travail : propre (`git status --short` ne retourne aucune ligne).
- Fichiers modifiés : aucun.
- Fichiers non suivis : aucun.
- Fichiers ajoutés récemment : les lots réglementaires et documentaires listés aux sections 3, 5 et 6 ont été ajoutés ou complétés dans les commits du 27 au 30 septembre 2026.

## 2. Version détectée

- Fichier : `app/build.gradle.kts`.
- `versionCode = 59`.
- `versionName = "0.6.13.7"`.
- La version de préparation est donc la dernière version 0.6.13.x détectée, et non une version 0.7.

## 3. Lots d'import présents

Les importeurs et leurs rapports présents couvrent les lots suivants :

| Lot | Importeur | Rapport principal | Présence vérifiée |
|---|---|---|---|
| Additifs UE | `tools/import_eu_additives_reference.py` | `EU_ADDITIVES_REFERENCE_0_6_9_10_IMPORT_REPORT.md`, rapports 0.6.10 | Oui |
| Mapping multilingue | `tools/build_multilingual_ingredient_mapping.py` | `MULTILINGUAL_INGREDIENT_MAPPING_0_6_13_1_REPORT.md` | Oui |
| Jus, purées et nectars | `tools/import_eu_fruit_juice_directive.py` | `EU_FRUIT_JUICE_DIRECTIVE_0_6_13_2_IMPORT_REPORT.md` | Oui |
| Confitures | `tools/import_eu_jams_directive.py` | `EU_JAMS_DIRECTIVE_0_6_13_3_IMPORT_REPORT.md` | Oui |
| Produits agricoles | `tools/import_eu_agricultural_products_regulation.py` | `EU_AGRICULTURAL_PRODUCTS_REGULATION_0_6_13_4_IMPORT_REPORT.md` | Oui |
| Cacao et chocolat | `tools/import_eu_cocoa_chocolate_directive.py` | `EU_COCOA_CHOCOLATE_DIRECTIVE_0_6_13_5_IMPORT_REPORT.md` | Oui |
| Lait conservé | `tools/import_eu_preserved_milk_directive.py` | `EU_PRESERVED_MILK_DIRECTIVE_0_6_13_IMPORT_REPORT.md` | Oui |
| Miel | `tools/import_eu_honey_directive.py` | `EU_HONEY_DIRECTIVE_0_6_12_IMPORT_REPORT.md` | Oui |
| Arômes | `tools/import_eu_flavourings_regulation.py` | `EU_FLAVOURINGS_REGULATION_0_6_13_7_IMPORT_REPORT.md` | Oui |
| FIC 1169/2011 | `tools/extract_fic_reference.py` | `EU_FIC_1169_2011_DOCUMENTATION_AND_KNOWLEDGE_AUDIT.md` | Oui |

Les générateurs présents comprennent aussi `build_ingredients.py`, `build_knowledge_docs.py`, `build_origin_rules.py` et `extract_eu_additives_reference.py`.

## 4. Fichiers applicatifs modifiés

L'état courant ne contient aucune modification non commitée. Les fichiers applicatifs touchés par les lots récents sont :

- `app/build.gradle.kts` ;
- `app/src/main/assets/ingredients.json` ;
- `app/src/main/assets/ingredient_aliases_multilingual.json` ;
- `mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt` ;
- `app/src/main/java/com/example/isitvegan/VerdictExplanationFormatter.kt` et les ressources de chaînes associées pour les lots antérieurs ;
- les tests ciblés d'import dans `app/src/test/java/com/example/isitvegan/` ;
- `app/src/androidTest/java/com/example/isitvegan/VerdictExplanationInstrumentedTest.kt` et `RealLabelsInstrumentedTest.kt` pour les lots antérieurs.

Les deux fichiers de données contiennent 479 entrées et les mêmes identifiants. Leur comparaison brute diffère de format : la source éditoriale utilise `sources` (liste), tandis que l'asset utilise `source` (chaîne) et ajoute `eNumber` vide lorsque nécessaire. Après cette normalisation, les entrées sont équivalentes.

## 5. Fichiers documentaires modifiés

Les documents de référence et de traçabilité présents comprennent :

- les rapports d'import des lots 0.6.9.x, 0.6.10.x, 0.6.11.x et 0.6.13.x ;
- `EU_FIC_1169_2011_DOCUMENTATION_AND_KNOWLEDGE_AUDIT.md` ;
- les sources `docs/sources/eu-additives-annex-ii-b.md`, `eu-honey-directive.md`, `eu-preserved-milk-directive.md`, `eu-agricultural-products-regulation.md`, `eu-cocoa-chocolate-directive.md`, `eu-flavourings-regulation.md` et `eu-fic-1169-2011.md` ;
- `docs/generated/base-connaissances-data.md` et `docs/generated/multilingual-ingredient-mapping.md` ;
- `docs/base-connaissances.md`, `docs/additifs.md`, `docs/matching.md`, `docs/changelog.md` et `mkdocs.yml`.

Les rapports historiques correspondants sont conservés à la racine. Le relevé contient 36 fichiers de rapport ou de revue portant explicitement un identifiant 0.6.9, 0.6.10, 0.6.11 ou 0.6.13.

## 6. Sources PDF/TXT présentes

- 48 PDF suivis par Git :
  - 4 pour les additifs UE ;
  - 4 pour le FIC 1169/2011 dans `00-general-food-labelling` ;
  - 8 dans `01-core-labelling-terms` ;
  - 24 dans `02-sector-product-standards` ;
  - 4 dans `03-food-additives` ;
  - 8 dans `04-fortified-foods-and-supplements`.
- 11 fichiers TXT suivis par Git : 4 extractions FIC, 5 fixtures OCR Android, `mlkit-resolution.txt` et `requirements-docs.txt`.
- Les quatre TXT extraits FIC sont présents dans `reference-input/eu-food-labelling/00-general-food-labelling/extracted/` et sont référencés dans son manifest avec leurs hashes.
- Les sources FIC, additifs, miel, lait, jus, confitures, produits agricoles, cacao/chocolat et arômes sont référencées dans `knowledge/sources.json` ou leurs manifests/rapports associés.

## 7. Incohérences ou fichiers manquants

- Aucun fichier modifié ou non suivi n'est laissé dans l'arbre de travail.
- Les PDF FIC 1169/2011 sont présents à l'identique dans `00-general-food-labelling/` et `01-core-labelling-terms/` pour les quatre langues. Il s'agit de doublons binaires vérifiés par hash.
- `SOURCE_MANIFEST.md` est présent dans `00-general-food-labelling/` et `03-food-additives/`, mais absent de `01-core-labelling-terms/`, `02-sector-product-standards/` et `04-fortified-foods-and-supplements/`.
- Les sous-dossiers de sources réglementaires hors FIC contiennent des PDF, sans TXT extraits locaux correspondants. Aucun manque n'est constaté pour les fichiers explicitement utilisés par les importeurs ; la différence de format reste une asymétrie de traçabilité.
- `app/src/main/res/xml/backup_rules.xml` est le seul nom repéré comme contenant `backup` ; son emplacement et son rôle Android sont cohérents avec un fichier applicatif, pas avec un fichier temporaire.
- Aucune copie, sauvegarde, fichier temporaire ou doublon textuel n'a été détecté en dehors des doublons PDF FIC signalés ci-dessus.

## 8. Éléments à conserver avant toute modification

- L'état propre de `master` et l'alignement avec `origin/master` au commit `2229018`.
- `knowledge/ingredients.json` comme source éditoriale et `app/src/main/assets/ingredients.json` comme asset Android généré.
- `knowledge/ingredient_aliases_multilingual.json` et sa copie asset, actuellement identiques par hash.
- `knowledge/sources.json`, les rapports d'import, les sources `docs/sources/` et les manifests existants.
- Les PDF réglementaires locaux et les quatre TXT FIC extraits, avec leurs hashes documentés.
- Les alias multilingues, les classifications existantes et les rapports 0.6.9.x à 0.6.13.x.

## 9. Verdict

**READY_WITH_WARNINGS**

Le dépôt est propre, versionné en `0.6.13.7`, et les ajouts réglementaires demandés sont présents dans les données, les importeurs, les tests, les sources ou la documentation correspondante. La préparation de 0.7 peut être engagée du point de vue de l'état Git. Les avertissements vérifiables sont les doublons PDF FIC, l'absence de manifests dans trois sous-dossiers de sources et l'asymétrie de format entre la source JSON éditoriale et son asset, sémantiquement équivalent après normalisation.
