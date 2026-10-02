# Correctif 0.6.9.7 — inconnus visibles avec un verdict incertain

Date : 2026-09-26  
Branche : `master`

## Problème reproduit

Quand le verdict principal était `UNCERTAIN`, le formatter affichait les ingrédients incertains mais masquait la liste des ingrédients non identifiés. La condition de rendu n’autorisait cette liste que pour `INCONCLUSIVE`.

Les listes agrégées utilisaient aussi directement les inconnus bruts du moteur. Un token ayant un statut effectif `VEGAN` par correspondance contextuelle pouvait donc être présenté comme inconnu, bien que sa trace de diagnostic détaillée confirme la correspondance.

## Correction

- Le formatter commun affiche maintenant, pour `UNCERTAIN` comme pour `INCONCLUSIVE`, les sections distinctes « Ingrédients incertains » et « Ingrédients non identifiés » lorsque leurs données sont présentes.
- Une seule notice localisée est affichée après ces sections ; aucun résultat conditionnel VEGAN ou VEGETARIAN n’est rendu lorsque le moteur indique qu’un vrai inconnu reste bloquant.
- `AnalysisDiagnostics.visibleUnknownTokens` et `visibleUnknownIngredients` construisent la vue UI/diagnostic : un token dont tous les statuts effectifs sont `VEGAN` est retiré de cette vue, tout en restant disponible dans les diagnostics de token et sans modifier le résultat brut utilisé par le moteur.
- Un préfixe numérique OCR isolé devant un ingrédient effectivement reconnu reste affiché comme inconnu (`68`), sans présenter toute l’expression reconnue comme inconnue.
- Les rapports agrégés utilisent la même vue. Les traces restent séparées et ne participent à aucune de ces listes.

## Régression OCR française

`UncertainUnknownVisibility097Test` analyse le texte OCR fourni avec les assets de production. Il vérifie :

- verdict principal `UNCERTAIN` ;
- présence de `arôme naturel` parmi les incertains ;
- présence des vrais inconnus `68`, `fenugrec`, `cumin`, `Die`, `gomme de guai`, `benOate de sodium` et `extrait` ;
- absence de `purée de tomates mi-réduife`, `vinaigre d’alcool` et `graines d8 moutarde` de la vue agrégée ;
- absence de résultat conditionnel et raison `UNKNOWN_INGREDIENT_REMAINS`.

La base actuelle attribue déjà un statut effectif `VEGAN` à `amidon modifié`. Il est donc volontairement absent de la vue des inconnus selon la règle de cette version ; il n’a pas été reclassé pour satisfaire artificiellement l’exemple.

## Tests ajoutés ou adaptés

- `DiagnosticOutput093Test` vérifie qu’une correspondance contextuelle effectivement VEGAN reste disponible dans la trace détaillée tout en étant retirée des listes agrégées.
- `UncertainUnknownVisibility097Test` couvre le cas OCR réel et le blocage conditionnel.
- `VerdictExplanationInstrumentedTest` couvre le rendu français simultané des incertains et inconnus, la notice unique et l’absence de résultat conditionnel.
- `NoIngredientListLocalizationTest` vérifie la clé localisée ajoutée dans les quatre langues.

## Fichiers modifiés

- `app/build.gradle.kts`
- `app/src/main/java/com/example/isitvegan/VerdictExplanationFormatter.kt`
- `app/src/main/res/values*/strings.xml`
- `app/src/test/java/com/example/isitvegan/NoIngredientListLocalizationTest.kt`
- `app/src/test/java/com/example/isitvegan/UncertainUnknownVisibility097Test.kt`
- `app/src/androidTest/java/com/example/isitvegan/VerdictExplanationInstrumentedTest.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticModels.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticReport.kt`
- `mutation-core/src/test/kotlin/com/example/isitvegan/DiagnosticOutput093Test.kt`

## Version et validations

- `versionName` : `0.6.9.7`
- `versionCode` : `43`
- `:mutation-core:test` : réussi.
- `testDebugUnitTest` : réussi.
- `assembleDebug` : réussi.
- `assembleDebugAndroidTest` : réussi.
- `git diff --check` : réussi.
- Tests connectés : non exécutés si `adb` est indisponible.

`VerdictEngine`, `VeganAnalyzer`, les règles de verdict principal, les assets, les alias et `ingredients.json` ne sont pas modifiés. Aucun commit ni push n’a été effectué.
