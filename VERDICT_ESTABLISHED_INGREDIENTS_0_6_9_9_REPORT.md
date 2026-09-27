# Rapport 0.6.9.9 — verdict informatif des ingrédients au statut établi

## Contrat fonctionnel

Lorsque le verdict principal vaut `UNCERTAIN` ou `INCONCLUSIVE`, l’explication structurée peut maintenant fournir `establishedIngredientVerdict`. Ce résultat est calculé à partir des ingrédients effectivement reconnus dans `AnalysisResult.matched`, après exclusion des statuts effectifs `UNCERTAIN`, avec `VerdictEngine` et une liste d’inconnus vide. Il n’est exposé que lorsqu’il produit `VEGAN` ou `VEGETARIAN`.

Le formatter partagé affiche alors, avant les listes existantes, une formulation localisée équivalente à « Selon les ingrédients au statut établi ». Une note précise que les éléments incertains ou non identifiés restent listés et empêchent toujours la confirmation globale. Les listes d’inconnus, les occurrences, les chemins imbriqués, les diagnostics et les explications existantes sont conservés.

Le verdict principal reste `AnalysisResult.verdict`, rendu en tête et inchangé. Aucun statut n’est écrit dans les nœuds `COMPOSITE`, aucune propagation métier enfant → parent n’est ajoutée, et `VerdictEngine`, `VeganAnalyzer`, les règles d’origine, les alias, les données et le pipeline OCR restent inchangés. Les traces restent dans `crossContactWarnings` et ne sont pas des tokens de `matched`.

## Cas couverts

| Cas | Résultat attendu et vérifié |
|---|---|
| VEGAN connus + inconnus | Principal `INCONCLUSIVE`, encart `VEGAN`, inconnus conservés. |
| VEGAN connus + `UNCERTAIN` | Principal `UNCERTAIN`, encart `VEGAN`. |
| VEGETARIAN connus + inconnus | Principal `INCONCLUSIVE`, encart `VEGETARIAN`. |
| `NON_VEGAN` connu + inconnus/incertains | Principal `NON_VEGETARIAN`, aucun encart redondant, causes et incertains conservés. |
| Aucune liste exploitable | Verdict informatif absent. |
| Traces seules ou combinées | Les traces n’influencent pas l’encart et restent affichées séparément. |
| Occurrences imbriquées et multiples | Les chemins et occurrences restent dans `VerdictIngredientReference`; le calcul utilise les classifications effectives déjà établies. |
| Deux parcours UI | `MainActivity` et `OcrFirstScreen` utilisent le même `VerdictExplanationFormatter`. |
| Langues UI | Les nouvelles clés existent et sont alignées en FR, EN, NL et DE. |

Les tests JVM ajoutés dans `VerdictExplanation069Test` couvrent les cas de statut établi, inconnus, incertains, non vegan, traces et compositions imbriquées. Les tests Android de rendu vérifient les libellés localisés et le parcours partagé.

## Preuve de conservation du verdict principal

`establishedIngredientVerdict` est un champ dérivé de `VerdictExplanation`. Il ne participe ni à `AnalysisResult.verdict`, ni à `AnalysisResult.veganAssessment`, ni au parcours d’analyse. Le formatter continue de sélectionner le libellé principal à partir de `result.verdict`; il ajoute seulement l’encart et sa note. Le test `explanationDoesNotChangeTheMainVerdict` demeure vert, ainsi que les tests de bloqueurs non vegan et de diagnostics 0.6.9 existants.

## Fichiers modifiés

- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticModels.kt` : ajout du résultat dérivé dans l’explication structurée.
- `mutation-core/src/test/kotlin/com/example/isitvegan/VerdictExplanation069Test.kt` : couverture JVM des cas établis.
- `app/src/main/java/com/example/isitvegan/VerdictExplanationFormatter.kt` : rendu partagé avant les listes et note conditionnelle.
- `app/src/main/res/values*/strings.xml` : trois clés localisées identiques dans les quatre ensembles.
- `app/src/androidTest/java/com/example/isitvegan/VerdictExplanationInstrumentedTest.kt` et `MainScreenInstrumentedTest.kt` : attentes de rendu 0.6.9.9.
- `app/src/test/java/com/example/isitvegan/UiLanguageTest.kt` : parité des nouvelles clés.
- `app/build.gradle.kts` : `versionName = "0.6.9.9"`, `versionCode = 45`.
- `README.md`, `docs/index.md`, `docs/verdict.md`, `docs/diagnostic.md`, `docs/changelog.md` : documentation et notes de version directement concernées.

## Validations

- `.\gradlew.bat :mutation-core:test --console=plain` : succès.
- `.\gradlew.bat testDebugUnitTest --console=plain` : succès.
- `.\gradlew.bat assembleDebug --console=plain` : succès.
- `.\gradlew.bat assembleDebugAndroidTest --console=plain` : succès.
- `adb devices` : appareil Nokia G42 5G disponible.
- `.\gradlew.bat connectedDebugAndroidTest --console=plain` : succès après mise à jour d’une attente UI existante devenue obsolète.
- Vérification statique des ressources : quatre fichiers, 80 clés chacun, parité exacte.
- `git diff --check` : succès.
- PIT : non lancé sur `master`, conformément à la consigne.

## Limites connues

L’encart reste volontairement informatif : il ne confirme jamais le produit entier et disparaît pour un verdict principal déjà complet (`VEGAN`, `VEGETARIAN` ou `NON_VEGETARIAN`). Un mélange de statuts établis contenant un bloqueur `NON_VEGAN` ne produit pas d’encart vegan/végétarien. Les nœuds composites restent syntaxiques et ne reçoivent aucun statut propagé. La consolidation multilingue, la validation croisée entre langues et la suggestion automatique de reprendre une photo plus large restent hors de cette version.

État Git final : branche `master`, aucun commit ni push effectué. Les modifications listées ci-dessus sont les seules modifications de travail attendues pour 0.6.9.9.
