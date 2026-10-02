# Revue finale — contexte des inconnus imbriqués v0.6.9.8

Date : 2026-09-26  
Branche : `master`  
Version vérifiée : `0.6.9.8` / `versionCode 44`

## Décision finale : GO

Le hotfix rétablit le contexte visuel du parent pour un enfant réellement inconnu, sans l'introduire dans les données métier ni modifier le verdict.

## Séparation contexte visuel / inconnus métier

`VerdictExplanationFormatter.renderUnknownIngredients` crée un arbre local de `UnknownDisplayNode` depuis `visibleUnknownTokens` et les liens `parentOrder`. Les ancêtres sont ajoutés uniquement à cet arbre de présentation : aucune écriture n'est faite dans `AnalysisResult` ni dans les diagnostics.

La preuve de séparation est la suivante :

| Donnée | Source / comportement vérifié |
|---|---|
| Inconnus bruts | `AnalysisResult.unknown` est construit dans l'analyse et reste utilisé pour `UNKNOWN_INGREDIENT_REMAINS`, `unknownPreventsVegan` et le calcul conditionnel. |
| Inconnus visibles | `visibleUnknownTokens` filtre les correspondances dont les statuts effectifs sont tous `VEGAN`; `visibleUnknownIngredients` en est l'agrégat. |
| Agrégats structurés | `IngredientDiagnosticGroups` et `DecisionDiagnostic` reçoivent `visibleUnknownIngredients` depuis `AnalysisDiagnostics`. |
| Contexte de présentation | Le parent est lu dans `diagnostics.tokens` et rendu seulement lorsqu'il mène à un descendant visible. Il n'est jamais ajouté aux listes précédentes. |

`IngredientTreeParserTest.singlePercentageChildKeepsParentAndChildDiagnostics` vérifie le lien parent/enfant, les quantités `4,4 %` et `50 %`, ainsi que l'absence du parent des inconnus visibles. `VerdictExplanationInstrumentedTest.unknownIngredientsUseBulletsAndKeepNestedOccurrenceContext` vérifie la présence du parent et de l'enfant dans le rendu, l'absence du parent des agrégats et la conservation du verdict. Le descendant inconnu reste l'unique élément bloquant ; les règles `UNKNOWN_INGREDIENT_REMAINS` et le résultat conditionnel ne sont pas modifiés.

## Règle de parsing et régressions

La seule règle nouvelle de `IngredientTreeParser.isCompositionParenthesis` considère comme composition un contenu parenthésé comportant une quantité, après les protections des désignations protégées, règles d'origine, préfixes `contient` et qualificatifs. Elle donne donc une occurrence parent et une occurrence enfant pour `sirop de grenade 4,4 % (concentré de grenade 50 %)`.

Les tests existants et ajoutés couvrent les listes multi-enfants, crochets, niveaux imbriqués, pourcentages décimaux et le maintien en feuille d'un qualificatif (`huile de colza (non hydrogénée)`). Les traces restent extraites hors de l'arbre d'ingrédients dans `tracesNeverEnterTheIngredientTree`. Aucun problème concret n'a été observé dans ces régressions.

## Rendu et invariants 0.6.9.7

Le test connecté vérifie le rendu en puces, le parent `• sirop de grenade — 4,4 %`, l'enfant `└─ concentré de grenade — 50 %`, l'absence de `|` dans l'UI et les deux occurrences distinctes de `cumin`. Les tests 0.6.9.7 conservent le filtrage des faux inconnus contextualisés dans les agrégats visibles et structurés, tout en maintenant les vrais inconnus et les traces dans leurs sections séparées.

## Périmètre

Le diff ne modifie ni `VerdictEngine`, ni `VeganAnalyzer`, ni les règles de verdict, `ingredients.json`, les alias, assets, ML Kit, le pipeline OCR, le recadrage ou les ressources de localisation. Aucune propagation de statut des enfants d'un composite n'a été ajoutée.

## Anomalies démontrées

Aucune anomalie bloquante, importante ou mineure n'a été démontrée.

## Validations exécutées

- `./gradlew :mutation-core:test` — succès.
- `./gradlew testDebugUnitTest` — succès.
- `./gradlew connectedDebugAndroidTest --console=plain` — succès sur `Nokia G42 5G - 15`.
- `git diff --check` — succès.

## État Git final

Le worktree contient les cinq modifications prévues par le hotfix et les rapports associés, dont ce rapport de revue. Aucun fichier généré ou personnel inattendu n'a été observé.

Pendant cette revue, aucun code, test existant, donnée, asset, index Git, commit ou push n'a été modifié. Seul ce rapport demandé a été créé.
