# Correctif 0.6.9.7 — cohérence des inconnus structurés

Date : 2026-09-26  
Branche : `master`

## Cause

La vue `AnalysisDiagnostics.visibleUnknownTokens` / `visibleUnknownIngredients` filtrait correctement les correspondances effectivement VEGAN, mais `IngredientDiagnosticGroups.unknownIngredients` et `DecisionDiagnostic.unknownIngredients` étaient encore alimentés par `AnalysisResult.unknown` directement.

## Correction

Les deux fonctions d’agrégation acceptent désormais la liste visible en paramètre. Les accesseurs `AnalysisDiagnostics.ingredientGroups` et `AnalysisDiagnostics.decision` leur transmettent `visibleUnknownIngredients`. Le raccord dans `VeganAnalyzer.kt` est limité à ces deux accesseurs de modèle ; aucun pipeline d’analyse, calcul de statut ou moteur de verdict n’a été modifié.

La séparation reste explicite :

- `AnalysisResult.unknown` reste brut, inchangé, utilisé par le moteur et le calcul `UNKNOWN_INGREDIENT_REMAINS` ;
- les tokens détaillés gardent leur texte et leur statut effectif ;
- `visibleUnknownTokens` conserve les vrais inconnus et les occurrences, en retirant uniquement les tokens non vides dont tous les statuts effectifs sont `VEGAN` ;
- `visibleUnknownIngredients`, `IngredientDiagnosticGroups`, `DecisionDiagnostic` et le rapport texte partagent la vue agrégée filtrée.

Le préfixe numérique `68` reste extrait comme inconnue visible lorsque le reste du token est effectivement reconnu. `amidon modifié` reste absent des agrégats car la base actuelle lui attribue déjà un statut effectif `VEGAN`; aucune reclassification n’a été ajoutée.

## Tests ajoutés ou adaptés

`DiagnosticOutput093Test` vérifie désormais, pour une correspondance contextuelle VEGAN :

- la trace détaillée du token et son statut effectif restent présents ;
- `AnalysisResult.unknown` brut reste inchangé ;
- `visibleUnknownIngredients`, `ingredientGroups.unknownIngredients`, `decision.unknownIngredients` et le rapport agrégé n’exposent pas la correspondance.

`UncertainUnknownVisibility097Test` vérifie sur le texte OCR réel :

- `UNCERTAIN`, `arôme naturel`, les vrais inconnus attendus et les trois agrégats structurés ;
- l’absence de purée de tomates, vinaigre d’alcool, graines d8 moutarde et amidon modifié dans tous les agrégats visibles ;
- `conditionalVerdict == null` et `UNKNOWN_INGREDIENT_REMAINS`.

## Version et périmètre

- `versionName` : `0.6.9.7` inchangé.
- `versionCode` : `43` inchangé.
- `VerdictEngine`, les règles du verdict principal, les données brutes, les assets, ML Kit, OCR et les ressources UI restent inchangés.
- Aucune propagation de statut depuis les enfants d’un composite n’a été introduite.

## Fichiers modifiés

- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticModels.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/VeganAnalyzer.kt` — uniquement les deux accesseurs d’agrégats structurés
- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticReport.kt` (déjà modifié dans la baseline, conservé)
- `mutation-core/src/test/kotlin/com/example/isitvegan/DiagnosticOutput093Test.kt`
- `app/src/test/java/com/example/isitvegan/UncertainUnknownVisibility097Test.kt`
- `FIX_0_6_9_7_STRUCTURED_UNKNOWN_CONSISTENCY_REPORT.md`

## Validations

- `:mutation-core:test` : réussi.
- `testDebugUnitTest` : réussi.
- `assembleDebug` : réussi lors de la baseline 0.6.9.7 ; aucune modification UI/Gradle n’a été faite par ce correctif.
- `assembleDebugAndroidTest` : réussi lors de la baseline 0.6.9.7.
- `git diff --check` : exécuté avec succès.
- Tests connectés : non exécutés ; `adb` n’est pas disponible.

## Git

Les changements 0.6.9.x préexistants restent non commités. Aucun commit, push ou modification de l’index Git n’a été effectué.
