# Revue 0.6.9.7 — inconnus visibles avec verdict `UNCERTAIN`

Date : 2026-09-26  
Branche : `master`

## Décision finale : NO-GO

La correction UI du défaut principal est correctement câblée et les validations ciblées passent. Une incohérence importante demeure toutefois dans les diagnostics structurés : leurs listes agrégées d’inconnus gardent les inconnus bruts, y compris une correspondance contextuelle effectivement `VEGAN`. La demande exige que ces éléments ne soient exposés ni dans l’UI ni dans les diagnostics agrégés.

## Comportement vérifié

Le moteur conserve `AnalysisResult.unknown`, utilisé par `VerdictEngine` et par `toVerdictExplanation()` pour calculer `UNKNOWN_INGREDIENT_REMAINS`. Cette liste brute conserve les preuves de matching détaillées et continue de bloquer un résultat conditionnel.

`AnalysisDiagnostics.visibleUnknownTokens` construit une vue distincte pour l’UI et le rapport texte. Un token n’est retiré que lorsque `effectiveStatuses` est non vide et que tous les statuts sont `VEGAN`. Un préfixe OCR numérique devant une expression reconnue est réduit au seul nombre ; le cas réel conserve donc `68` sans présenter toute l’expression de tomate comme inconnue.

`VerdictExplanationFormatter` utilise cette vue pour `UNCERTAIN` et `INCONCLUSIVE`, affiche la section d’incertains et, lorsqu’elle existe, la section d’inconnus, puis une unique notice localisée. Les résultats conditionnels sont toujours rendus uniquement depuis `verdictExplanation.conditionalVerdict`, qui reste `null` lorsque le moteur conserve un vrai inconnu.

## Invariants

| Invariant | État | Preuve |
|---|---|---|
| Verdict principal inchangé | Conforme | Aucun diff dans `VerdictEngine.kt` ou `VeganAnalyzer.kt`; la raison conditionnelle lit encore `result.unknown` dans `DiagnosticModels.kt`. |
| Filtre : tous les statuts effectifs doivent être VEGAN | Conforme | `DiagnosticModels.kt`, `effectiveStatuses.isNotEmpty() && all { VEGAN }`. Les statuts vides, `UNCERTAIN`, `VEGETARIAN` ou `NON_VEGAN` restent visibles. |
| Préfixe `68` conservé | Conforme | `DiagnosticModels.kt` isole le préfixe numérique ; `UncertainUnknownVisibility097Test` l’assert. |
| OCR réel : verdict, incertain et bloqueur | Conforme | `UncertainUnknownVisibility097Test` vérifie `UNCERTAIN`, `arôme naturel`, les inconnus attendus, `conditionalVerdict == null` et `UNKNOWN_INGREDIENT_REMAINS`. |
| UI `UNCERTAIN` : deux sections et une notice | Conforme | `VerdictExplanationFormatter.kt`; `VerdictExplanationInstrumentedTest.uncertainVerdictShowsUnknownIngredientsAndOneSharedBlockingNotice`. |
| INCONCLUSIVE conservé | Conforme | La condition de rendu couvre toujours `INCONCLUSIVE`; le test instrumenté 0.6.9.6 conserve les puces, occurrences et contexte imbriqué. |
| Traces séparées | Conforme | `renderTraces` est séparé du rendu détaillé ; le test de traces existant vérifie son rendu localisé. |
| Ressources FR/NL/EN/DE | Conforme | Parité exacte vérifiée localement : 0 clé manquante et 0 clé supplémentaire dans chaque fichier. |
| Exports avec `|` inchangés | Conforme | `DiagnosticReport.structuredList()` reste le formatter des exports ; le changement porte uniquement sur la ligne agrégée « Inconnus ». |
| Inconnus agrégés des modèles structurés | Non conforme | `IngredientDiagnosticGroups.unknownIngredients` et `DecisionDiagnostic.unknownIngredients` sont encore construits depuis `AnalysisResult.unknown`, non depuis `visibleUnknownIngredients`. |

## Anomalies

### IMPORTANT — modèles structurés contradictoires

`AnalysisDiagnostics.visibleUnknownIngredients` retire correctement une correspondance effectivement vegan. Toutefois :

- `AnalysisDiagnostics.ingredientGroups` appelle `result.toIngredientDiagnosticGroups()`, qui assigne `unknownIngredients = unknown` ;
- `AnalysisDiagnostics.decision` appelle `result.toDecisionDiagnostic()`, qui assigne aussi `unknownIngredients = unknown`.

Le rapport texte masque ensuite ce problème en affichant directement `diagnostics.visibleUnknownIngredients`, mais un consommateur des modèles structurés reçoit encore l’ancienne liste brute. Le test `DiagnosticOutput093Test` teste la nouvelle propriété et le rapport, sans assertion sur `ingredientGroups.unknownIngredients` ni `decision.unknownIngredients`.

Correction requise avant commit : faire converger les agrégats structurés vers une même vue explicitement nommée, tout en conservant `AnalysisResult.unknown` pour le moteur, le calcul conditionnel et la trace détaillée.

### MINEUR — couverture UI du texte OCR réel

Le test JVM réel vérifie les données du texte OCR complet. Le test instrumenté vérifie le rendu `UNCERTAIN` avec un cas synthétique. Leur combinaison couvre le chemin, mais il n’exécute pas le formatter sur le texte OCR réel. C’est une amélioration de couverture, pas un défaut démontré.

## Limites hors scope

- Aucun statut n’est propagé depuis les enfants d’un composite.
- Aucune faute OCR (`benOate`, `cloUs`, etc.) n’est corrigée.
- La base actuelle classe `amidon modifié` comme effectivement `VEGAN`; il reste donc volontairement absent de la vue visible.
- Aucune modification de ML Kit, recadrage, assets, `ingredients.json`, alias, `VerdictEngine` ou `VeganAnalyzer` n’est détectée.

## Validations consultées ou exécutées

- Rapport de livraison : `:mutation-core:test`, `testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest` et `git diff --check` annoncés réussis.
- Revue : `:mutation-core:test --tests com.example.isitvegan.DiagnosticOutput093Test` exécuté avec succès.
- Revue : `testDebugUnitTest --tests com.example.isitvegan.UncertainUnknownVisibility097Test` exécuté avec succès.
- `git diff --check` exécuté avec succès.
- Tests connectés non exécutés : `adb` n’est pas disponible.

## État Git final

Les changements 0.6.9.7 attendus restent non commités. Le seul ajout de cette revue est ce rapport. Aucun code, test existant, donnée, asset, Gradle, index, commit ou push n’a été modifié durant la revue.
