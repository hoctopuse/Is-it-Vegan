# Rapport de documentation UE des additifs — 0.6.9.10

Date : 28 septembre 2026  
Branche : `master`  
Périmètre : sources documentaires et documentation des additifs uniquement

## Réorganisation des sources

Les fichiers ont été déplacés sans renommage :

| Ancien chemin | Nouveau chemin |
|---|---|
| `reference-input/eu-additives/CELEX_02008R1333-20260818_FR_TXT.pdf` | `reference-input/eu-food-labelling/03-food-additives/CELEX_02008R1333-20260818_FR_TXT.pdf` |
| `reference-input/eu-additives/CELEX_02008R1333-20260818_NL_TXT.pdf` | `reference-input/eu-food-labelling/03-food-additives/CELEX_02008R1333-20260818_NL_TXT.pdf` |
| `reference-input/eu-additives/CELEX_02008R1333-20260818_EN_TXT.pdf` | `reference-input/eu-food-labelling/03-food-additives/CELEX_02008R1333-20260818_EN_TXT.pdf` |
| `reference-input/eu-additives/CELEX_02008R1333-20260818_DE_TXT.pdf` | `reference-input/eu-food-labelling/03-food-additives/CELEX_02008R1333-20260818_DE_TXT.pdf` |
| `EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv` | `reference-input/eu-food-labelling/03-food-additives/EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv` |
| `reference-input/eu-additives/SOURCE_MANIFEST.md` | `reference-input/eu-food-labelling/03-food-additives/SOURCE_MANIFEST.md` |

`SOURCE_MANIFEST.md` documente le rôle linguistique de chaque PDF, CELEX `02008R1333`, la consolidation du 18.08.2026, le rôle du CSV et le maintien des deux rapports à la racine. Les dossiers futurs `00`, `01` et `02` n’ont pas été modifiés.

## Organisation documentaire

Une page dédiée a été créée et ajoutée à la navigation MkDocs :

- `docs/additifs.md` présente les sources CELEX `02008R1333` consolidées au 18.08.2026, les quatre PDF locaux, le CSV, les rapports, la couverture 338/340, E322a, E345/E345(i), E470b/E572, la différence entre reconnaissance et compatibilité vegan, et la prochaine étape de classification documentée.
- `mkdocs.yml` ajoute la page au même niveau que la base de connaissances et met à jour la description du site vers 0.6.9.10.
- `docs/base-connaissances.md` renvoie vers la page dédiée depuis la section des sources.
- `docs/changelog.md` précise les exceptions E322a, E345/E345(i), la couverture partielle et la collision E470b/E572, sans modifier la version applicative.
- `tools/import_eu_additives_reference.py`, `tools/extract_eu_additives_reference.py`, leur README, le test JVM ciblé et les rapports utilisent le nouveau chemin du CSV/PDF.

Aucun doublon de page additifs n’existait dans `docs/`. Les rapports de provenance restent à la racine et sont liés depuis la page dédiée :

- [rapport d’acquisition](EU_ADDITIVES_OFFICIAL_MULTILINGUAL_ACQUISITION_REPORT.md) ;
- [rapport de complétion](EU_ADDITIVES_REFERENCE_0_6_9_10_COMPLETION_REPORT.md).

La page lie désormais les quatre PDF locaux, le CSV de référence et les deux rapports via des URL absolues vers le dépôt GitHub public. Ces fichiers restent des sources du dépôt et ne deviennent pas des assets Android.

La configuration MkDocs déclare explicitement `generated/base-connaissances-data.md` dans `not_in_nav`, car ce fichier est un fragment inclus par `base-connaissances.md` et ne constitue pas une page de navigation autonome.

## Contenu documenté

La documentation indique explicitement que :

- 338 des 340 lignes réglementaires sont couvertes ;
- 250 nouvelles entrées sont `UNCERTAIN` ;
- la reconnaissance par la base ne prouve jamais une compatibilité vegan ;
- E322a n’a pas de dénomination DE officielle et aucun nom n’est inventé ;
- E345 et E345(i) restent hors base et distincts ;
- les alias communs E470b/E572 restent conservateurs : un libellé seul sélectionne actuellement E572, lui-même `UNCERTAIN`; les numéros E explicites distinguent E470b et E572 ;
- la suite attend une classification documentée et revue, sans promesse d’automatisation.

## Validations

- Vérification des liens Markdown locaux : **0 lien hors `docs/` restant** dans `docs/additifs.md` ; les sept références de provenance utilisent désormais des URL GitHub absolues.
- `git diff --check` : **succès**.
- `.\.venv-docs\Scripts\python.exe -m mkdocs build --strict` : **succès** ; les sept liens de provenance utilisent les nouveaux chemins GitHub absolus et le fragment généré est accepté via `not_in_nav`.
- `.\gradlew testDebugUnitTest --tests com.example.isitvegan.EuAdditivesReference06910Test` : **succès**.
- `.\gradlew testDebugUnitTest` : **succès**.

Cette étape n’a modifié que la documentation : aucun Kotlin, JSON, test, Gradle, version, asset, OCR ou moteur de verdict n’a été touché.

## Fichiers modifiés

- `mkdocs.yml`
- `docs/additifs.md`
- `docs/base-connaissances.md`
- `docs/changelog.md`
- `reference-input/eu-food-labelling/03-food-additives/SOURCE_MANIFEST.md`
- `tools/import_eu_additives_reference.py`
- `tools/extract_eu_additives_reference.py`
- `tools/README_EU_ADDITIVES_REFERENCE.md`
- `app/src/test/java/com/example/isitvegan/EuAdditivesReference06910Test.kt`
- `EU_ADDITIVES_OFFICIAL_MULTILINGUAL_ACQUISITION_REPORT.md`
- `EU_ADDITIVES_REFERENCE_0_6_9_10_COMPLETION_REPORT.md`
- `DOCUMENTATION_EU_ADDITIVES_0_6_9_10_REPORT.md`

Aucun commit ni push n’a été effectué.
