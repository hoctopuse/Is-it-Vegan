# Correction 0.6.9.4 — absence de liste d’ingrédients

Date : 2026-09-26  
Branche : `master`  
Version : `0.6.9.4` / `versionCode 40`

## Problème initial

Les analyses sans liste d’ingrédients exploitable pouvaient exposer des
informations de présence déclarée comme si elles constituaient un verdict ou
des listes d’ingrédients analysées. Cela rendait ambiguë la différence entre
une absence de liste, une analyse inconclusive et un verdict réellement
calculé.

## Correction

L’état `NO_INGREDIENT_LIST` est désormais prioritaire dans les diagnostics :

- le verdict principal reste non évalué (`null`) ;
- l’écran affiche l’absence de liste, l’absence de conclusion vegan fiable et
  un état explicite d’analyse non évaluée ;
- les listes vegan, végétariennes, non vegan, incertaines, inconnues,
  bloqueurs et origines sont indiquées comme non calculées dans le rapport ;
- une présence déclarée éventuelle reste affichée séparément sans être utilisée
  comme verdict ;
- les traces restent séparées, exclues des ingrédients et exclues du verdict.

Les listes normales conservent leurs verdicts VEGAN, NON VEGAN, INCERTAIN,
INCONCLUS et VEGETARIAN informatif. `VerdictEngine`, `VeganAnalyzer`, la
segmentation, les règles métier et les traitements OCR précédents n’ont pas été
modifiés.

## Localisation

La nouvelle indication d’analyse non évaluée est présente dans les ressources
FR, NL, EN et DE. Les quatre fichiers contiennent les mêmes 72 clés.

## Tests

`NoIngredientList094Test` couvre le texte vide, l’absence de titre, le texte
marketing ou nutritionnel, une section de traces seule, une présence déclarée
sans liste et la régression des verdicts d’une vraie liste.
`NoIngredientListLocalizationTest` vérifie les clés communes dans les quatre
ensembles de ressources.

## Fichiers concernés

- `app/build.gradle.kts` ;
- `VerdictExplanationFormatter.kt` ;
- les quatre fichiers `strings.xml` ;
- `DiagnosticModels.kt` et `DiagnosticReport.kt` ;
- les deux nouveaux tests ciblés.

## Validations

- tests JVM ciblés : succès ;
- `testDebugUnitTest` : succès ;
- `assembleDebug` : succès ;
- `assembleDebugAndroidTest` : succès ;
- `git diff --check` : succès.

Les tests connectés n’ont pas été exécutés : `adb` n’est pas disponible dans
l’environnement.

## Limites conservées

La consolidation multilingue, la sélection avancée de langue, les scores OCR,
la fusion de blocs et les corrections OCR générales restent hors périmètre et
sont reportées à la 0.7.

Aucun commit ni push n’a été effectué. Aucun asset, alias ou `ingredients.json`
n’a été modifié.
