# Revue finale — cohérence structurée des inconnus v0.6.9.7

Date : 2026-09-26  
Branche : `master`  
Version vérifiée : `0.6.9.7` / `versionCode 43`

## Décision finale : GO

Le correctif complémentaire aligne les agrégats structurés et le rapport texte sur la même vue d'inconnus visible, sans remplacer la liste brute utilisée par le moteur et la traçabilité détaillée.

## Invariants vérifiés

| Invariant | Preuve |
|---|---|
| Les groupes structurés reçoivent la vue visible. | `mutation-core/src/main/kotlin/com/example/isitvegan/VeganAnalyzer.kt` appelle `toIngredientDiagnosticGroups(visibleUnknownIngredients)`. |
| La décision structurée reçoit la même vue visible. | Le même fichier appelle `toDecisionDiagnostic(visibleUnknownIngredients)`. |
| Le rapport texte est aligné. | `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticReport.kt` utilise `diagnostics.visibleUnknownIngredients`. |
| `AnalysisResult.unknown` reste brut pour les décisions métier. | `DiagnosticModels.kt` conserve `unknown` pour `unknownPreventsVegan` et pour `UNKNOWN_INGREDIENT_REMAINS` dans l'explication conditionnelle. |
| Le filtre ne masque que les tokens avec des statuts effectifs non vides, tous `VEGAN`. | `DiagnosticModels.kt`, `visibleUnknownTokens`. Un statut absent, `UNCERTAIN`, `VEGETARIAN`, `NON_VEGAN` ou un vrai inconnu reste donc visible. |
| Le fragment OCR `68` est conservé sans réintroduire la phrase reconnue. | La règle ciblée extrait seulement le préfixe numérique d'un token autrement reconnu ; `UncertainUnknownVisibility097Test` l'assertionne. |
| Le cas OCR réel reste `UNCERTAIN`, avec `arôme naturel` incertain, inconnus réels visibles et résultat conditionnel absent. | `app/src/test/java/com/example/isitvegan/UncertainUnknownVisibility097Test.kt`. Le test vérifie aussi `UNKNOWN_INGREDIENT_REMAINS`. |
| Les faux inconnus contextualisés sont absents de toutes les vues agrégées. | Le même test couvre `purée de tomates mi-réduife`, `vinaigre d'alcool`, `graines d8 moutarde` et `amidon modifié`; `DiagnosticOutput093Test` couvre aussi le rapport texte et la conservation de la donnée brute. |
| Ordre, occurrences et hiérarchie restent disponibles pour l'affichage. | `visibleUnknownTokens` itère les tokens sans tri ni déduplication ; les vues d'UI fondées sur les tokens conservent les occurrences et chemins. La liste de chaînes agrégée conserve son contrat existant de déduplication normalisée. |
| Le changement dans `VeganAnalyzer.kt` se limite aux deux accesseurs d'agrégats. | Diff ciblé : les seules lignes fonctionnelles modifiées concernent `ingredientGroups` et `decision`. |
| Le moteur et les règles métier restent hors périmètre. | Aucun diff dans `VerdictEngine`, `ingredients.json`, alias, assets, OCR/ML Kit, recadrage ou ressources de cette correction complémentaire. |

## Anomalies démontrées

Aucune anomalie bloquante, importante ou mineure n'a été démontrée.

## Couverture et validations

- Exécuté : `./gradlew :mutation-core:test --tests com.example.isitvegan.DiagnosticOutput093Test` — succès.
- Exécuté : `./gradlew testDebugUnitTest --tests com.example.isitvegan.UncertainUnknownVisibility097Test` — succès.
- Consulté : les rapports de mise en œuvre et de première revue indiquent que la suite `:mutation-core:test`, les tests unitaires app et les assemblages debug/debugAndroidTest ont réussi après le correctif.
- `git diff --check` — succès.
- Les tests connectés ne sont pas requis pour lever un doute sur ce correctif de modèles ; aucun test connecté n'a été exécuté pendant cette revue.

## Limites hors scope

La propagation d'un statut depuis les enfants d'un composite, la correction OCR et l'enrichissement de la base d'ingrédients restent hors scope. Aucune de ces logiques n'a été modifiée.

## État Git final

Le worktree contient les changements non commités attendus de l'évolution 0.6.9.7, ses rapports, et ce rapport de revue. Aucun fichier généré ou personnel inattendu n'a été relevé.

Pendant cette revue, aucun code, test existant, donnée, asset, Gradle, index Git, commit ou push n'a été modifié. Seul ce rapport demandé a été créé.
