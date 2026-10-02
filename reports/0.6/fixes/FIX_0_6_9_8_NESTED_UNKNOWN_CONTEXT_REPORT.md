# Correctif 0.6.9.8 — contexte des inconnus imbriqués

Date : 2026-09-26  
Branche : `master`  
Version : `0.6.9.8` / `versionCode 44`

## Défaut reproduit et cause

Le formatter ne pouvait afficher le parent `sirop de grenade — 4,4 %` lorsque seul son enfant inconnu était présent dans `visibleUnknownTokens`. L’entrée OCR était auparavant parsée comme un unique token feuille pour une parenthèse contenant un seul enfant quantifié ; aucun `parentOrder` ne permettait donc de reconstruire le contexte.

## Correction

Le formatter construit maintenant un arbre de présentation à partir des tokens diagnostiques et de leurs `parentOrder`. Les ancêtres nécessaires sont ajoutés uniquement à cette représentation visuelle. Un parent contextuel conserve son libellé et son pourcentage, tandis que l’enfant réellement inconnu reste le seul élément d’inconnu métier.

La liste brute `AnalysisResult.unknown`, `visibleUnknownIngredients`, `IngredientDiagnosticGroups.unknownIngredients` et `DecisionDiagnostic.unknownIngredients` ne reçoit aucun parent contextuel supplémentaire. Le verdict, `unknownPreventsVegan`, `UNKNOWN_INGREDIENT_REMAINS` et le résultat conditionnel restent inchangés.

Le parseur reçoit uniquement l’ajustement structurel nécessaire pour reconnaître une parenthèse à enfant unique lorsque son contenu contient une quantité, après les protections existantes des qualificatifs. Aucun statut, matching, alias, trace ou règle de verdict n’est modifié.

## Comportement avant / après

Avant : le rendu pouvait omettre `• sirop de grenade — 4,4 %` et perdre le chemin parent → enfant.  
Après : le rendu affiche le parent contextuel puis `└─ concentré de grenade — 50 %`, sans séparateur `|` et sans ajouter le parent aux agrégats d’inconnus.

Les occurrences répétées restent distinctes et dans leur ordre d’analyse. Les parents et enfants réellement représentés dans les diagnostics ne sont pas fusionnés par leur texte.

## Tests ajoutés ou adaptés

- `IngredientTreeParserTest.singlePercentageChildKeepsParentAndChildDiagnostics` vérifie les tokens parent/enfant, `parentOrder`, les pourcentages et l’absence du parent dans les inconnus agrégés.
- `VerdictExplanationInstrumentedTest.unknownIngredientsUseBulletsAndKeepNestedOccurrenceContext` vérifie le parent contextuel, l’enfant inconnu, les agrégats structurés, les occurrences répétées, l’absence de `|` et la conservation du verdict.
- Les tests 0.6.9.7 de filtrage des faux inconnus contextualisés restent inchangés et passent.

## Validations

- `./gradlew :mutation-core:test` — succès.
- `./gradlew testDebugUnitTest` — succès.
- `./gradlew assembleDebug` — succès.
- `./gradlew assembleDebugAndroidTest` — succès.
- `./gradlew connectedDebugAndroidTest` — succès sur `Nokia G42 5G - 15`.
- `git diff --check` — succès.

## Périmètre et version

`VerdictEngine`, `VeganAnalyzer`, les règles de verdict, `ingredients.json`, les alias, les assets, ML Kit, le pipeline OCR et les ressources de localisation ne sont pas modifiés. Aucune propagation de statut depuis les enfants d’un composite n’a été introduite.

La version est passée de `0.6.9.7` / `43` à `0.6.9.8` / `44`.

## Git

Aucun commit ni push n’a été effectué. Le rapport est ajouté aux changements locaux de cette évolution.
