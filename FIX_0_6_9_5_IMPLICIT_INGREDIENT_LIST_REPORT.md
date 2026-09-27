# Correctif 0.6.9.5 — listes d’ingrédients implicites

Date : 2026-09-26  
Branche : `master`

## Problème corrigé

Une composition OCR réelle pouvait ne pas être analysée lorsqu’aucun titre textuel tel que « Ingrédients » n’était présent. Le marqueur visuel de langue n’est pas utilisé : il n’est pas disponible dans le texte OCR.

## Correction implémentée

`LabelSectionExtractor` accepte désormais une liste sans titre seulement si les indices suivants convergent :

- au moins trois séparateurs de premier niveau ;
- au moins quatre éléments produits par le parseur existant ;
- au moins deux termes alimentaires dans un vocabulaire borné servant uniquement à l’extraction ;
- et au moins un indice supplémentaire : pourcentage, parenthèses ou crochets équilibrés, ou séquence dense d’au moins quatre séparateurs.

Les textes nutritionnels, les messages marketing, les textes courts ou ambigus et les sections limitées aux traces restent `NO_INGREDIENT_LIST`. La composition est arrêtée avant les traces déjà détectées. Les traces ne sont donc ni tokenisées ni utilisées pour le verdict.

Le modèle `LabelSections` expose `implicitIngredientList`, avec les indices retenus et une confiance `HIGH`. Le rapport de diagnostic affiche « détectée sans titre explicite », sans prétendre avoir reconnu une icône ou un marqueur graphique absent.

## Comportements conservés

- `VerdictEngine` et `VeganAnalyzer` ne sont pas modifiés.
- Les règles de verdict, les traces, la normalisation bio, la reconstruction prudente des retours à la ligne et le séparateur structuré `|` restent inchangés.
- `ingredients.json`, les alias et les assets ne sont pas modifiés.
- Les listes avec titre explicite continuent d’emprunter leur chemin existant.

## Tests ajoutés

`ImplicitIngredientList095Test` couvre :

- la composition réelle de céréales sans titre et la séparation de « Peut contenir : du lait » ;
- une liste simple sans titre satisfaisant tous les critères ;
- des faux positifs nutritionnel, marketing, trace seule et texte ambigu ;
- une liste explicite avec les régressions bio et retour à la ligne.

## Documentation mise à jour

`docs/sections.md`, `docs/verdict.md` et `docs/diagnostic.md` décrivent la liste implicite, les critères cumulés et les informations diagnostiques.

## Limites conservées

Cette version ne lit pas les icônes de langue, ne consolide pas plusieurs langues, ne corrige pas les fautes OCR générales et ne déduit pas d’ingrédients à partir d’un seul mot alimentaire ou d’une virgule. Ces sujets restent hors du périmètre de la 0.6.9.5.

## Version

- `versionName` : `0.6.9.5`
- `versionCode` : `41`

## Validations

- `:mutation-core:test` : réussi.
- `testDebugUnitTest` : réussi.
- `assembleDebug` : réussi.
- `assembleDebugAndroidTest` : réussi.
- `git diff --check` : réussi.
- Tests connectés : non exécutés ; `adb` n’est pas disponible dans l’environnement.

## Fichiers modifiés

- `app/build.gradle.kts`
- `mutation-core/src/main/kotlin/com/example/isitvegan/LabelSectionExtractor.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticReport.kt`
- `mutation-core/src/test/kotlin/com/example/isitvegan/ImplicitIngredientList095Test.kt`
- `docs/sections.md`
- `docs/verdict.md`
- `docs/diagnostic.md`
- `FIX_0_6_9_5_IMPLICIT_INGREDIENT_LIST_REPORT.md`

## Git

Aucun commit ni push n’a été effectué.
