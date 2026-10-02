# Notes d’origines possibles — 0.6.11.2

## Décision

**GO, sous réserve des validations ci-dessous.** Cette passe enrichit uniquement
l’explication d’occurrences déjà reconnues `UNCERTAIN`. Elle ne change ni statut,
ni règle de verdict, ni traitement des traces.

## Modèle ajouté

`knowledge/ingredients.json` accepte `possibleOriginNote` seulement pour un concept
`UNCERTAIN`. L’objet conserve des codes d’origines (`PLANT`, `ANIMAL`, `EGG`,
`SYNTHETIC`, `MICROBIAL`, `MARINE`), une condition de variabilité, les identifiants
de sources et un niveau de confiance. `tools/build_ingredients.py` valide ce schéma,
vérifie les références de sources et le transmet dans l’asset. Le runtime le charge
dans `PossibleOriginNote`, puis `VerdictExplanationFormatter` le rend avec les
ressources FR/NL/EN/DE.

La note n’est attachée qu’à une occurrence qui reste effectivement `UNCERTAIN`.
Une règle locale qui résout l’occurrence, par exemple `lécithines (soja)`, garde donc
priorité et supprime la note générique de cette occurrence. Un texte OCR non reconnu
n’obtient aucune note.

## Additifs enrichis

| Concept | Numéro E | Origines possibles codées | Condition |
|---|---|---|---|
| `e322` | E322 | végétale, œuf, animale | matière première et procédé |
| `e422` | E422 | végétale, synthétique, animale | procédé |
| `e470a` | E470a | végétale, animale | matière première et procédé |
| `e470b` | E470b | végétale, animale | matière première et procédé |
| `e471` | E471 | végétale, animale | matière première et procédé |
| `e572` | E572 | végétale, animale, synthétique | matière première et procédé |
| `e627` | E627 | végétale, fermentaire, animale, marine | procédé |
| `e631` | E631 | végétale, fermentaire, animale, marine | procédé |
| `e635` | E635 | végétale, fermentaire, animale, marine | procédé |
| `e640` | E640 | synthétique, animale | procédé |

Les 306 autres concepts `UNCERTAIN`, dont les 299 autres numéros E incertains, restent
sans note : une origine précise ou une condition de variation n’est pas suffisamment
documentée dans cette passe.

## Sources et limites

Les notes utilisent les identifiants existants `federation-vegane-e-additives` et
`vegan-easy-food-additives`. E627, E631, E635 et E640 ajoutent le recoupement
secondaire `les-additifs-alimentaires-vegetarien`. Le règlement UE nº 231/2012 reste
une source réglementaire d’identité, mais ne suffit pas à conclure l’origine d’une
formulation commerciale variable.

Ces sources expliquent des possibilités d’origine ; elles ne justifient aucune
reclassification. Les sources peuvent aussi différer sur la fréquence de certaines
filières : le rendu emploie donc systématiquement « origines possibles » et une
condition, sans affirmer une origine pour le produit analysé.

## Statuts et impact

Les décomptes sont inchangés avant et après : 119 `VEGAN`, 10 `VEGETARIAN`, 7
`NON_VEGAN` et 316 `UNCERTAIN`, dont 309 numéros E. E470b/E572 et E960a/E960b restent
des concepts distincts ; E345/E345(i) restent exclus. Aucun statut éditorial et aucune
classification n’ont été modifiés par cette passe.

La note complète l’explication d’un verdict incertain ; elle ne participe pas au
calcul de `VerdictEngine` ou de `VeganAnalyzer`. Les traces restent affichées et
exclues séparément.

## Traductions

Les quatre ensembles de ressources ajoutent « Origines possibles », les six catégories
d’origine, les conditions « selon la matière première et le procédé », « selon le
procédé » et « selon le fabricant », ainsi que le rappel qu’un tel élément empêche de
confirmer le produit vegan. Les tests contrôlent que les clés existent dans FR, NL, EN
et DE et que le rendu instrumenté conserve le même niveau de prudence.

## Validations

Exécutées avec succès :

- les trois contrôles `python tools/build_ingredients.py --check`,
  `python tools/build_origin_rules.py --check` et
  `python tools/build_knowledge_docs.py --check`, avant puis après génération ;
- `python tools/build_ingredients.py`, `python tools/build_origin_rules.py` et
  `python tools/build_knowledge_docs.py` ; une seconde exécution reste identique ;
- `./gradlew :mutation-core:test` ;
- `./gradlew testDebugUnitTest`, y compris `PossibleOriginNoteTest` et les contrôles
  de ressources ;
- `./gradlew assembleDebug` et `./gradlew assembleDebugAndroidTest` ;
- `./gradlew connectedDebugAndroidTest` sur Nokia G42 5G, Android 15 ;
- `git diff --check`.

## Limites

La note n’est pas une certification du fabricant. Elle n’explique pas les concepts
sans données assez précises et ne doit pas être généralisée à partir d’un nom ressemblant
dans l’OCR. Une prochaine passe nécessite une preuve par additif et, si nécessaire,
par procédé ou fabricant.

## Fichiers modifiés

- `app/build.gradle.kts`
- `app/src/androidTest/java/com/example/isitvegan/VerdictExplanationInstrumentedTest.kt`
- `app/src/main/assets/ingredients.json` (généré)
- `app/src/main/java/com/example/isitvegan/VerdictExplanationFormatter.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-en/strings.xml`
- `app/src/main/res/values-nl/strings.xml`
- `app/src/main/res/values-de/strings.xml`
- `app/src/test/java/com/example/isitvegan/EuAdditivesReference06910Test.kt`
- `app/src/test/java/com/example/isitvegan/PossibleOriginNoteTest.kt`
- `app/src/test/java/com/example/isitvegan/UiLanguageTest.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/ingredients.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticModels.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/VeganAnalyzer.kt` (chargement JSON uniquement)
- `tools/build_ingredients.py`
- `knowledge/ingredients.json`
- `knowledge/sources.json` (modification portée par 0.6.11.1 conservée)
- `docs/additifs.md`, `docs/base-connaissances.md`, `docs/changelog.md`
- `docs/generated/base-connaissances-data.md` (généré)
- `ADDITIVE_ORIGIN_CLASSIFICATION_0_6_11_REPORT.md`
- `ADDITIVE_ORIGIN_CLASSIFICATION_0_6_11_1_REPORT.md`
- `ADDITIVE_ORIGIN_NOTES_0_6_11_2_REPORT.md`
