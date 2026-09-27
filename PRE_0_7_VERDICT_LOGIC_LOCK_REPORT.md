# Verrouillage pré-0.7 des règles de verdict

## Portée

Cette livraison est traitée comme une stabilisation **test-only**. La version applicative reste `0.6.9.9` avec `versionCode 45`; aucun changement de version 0.6.9.10 n’a été effectué faute de décision de publication.

Les tests verrouillent les invariants suivants :

- `AnalysisResult.verdict` reste la vérité principale et conserve sa priorité `NON_VEGETARIAN`, `UNCERTAIN`, `INCONCLUSIVE`, `VEGETARIAN`, `VEGAN`.
- Les inconnus empêchent toute confirmation vegan complète.
- Les ingrédients `UNCERTAIN` bloquent le verdict principal vegan, tout en conservant le résultat conditionnel existant lorsqu’aucun inconnu ni bloqueur ne subsiste.
- `establishedIngredientVerdict` n’existe que pour un verdict principal `UNCERTAIN` ou `INCONCLUSIVE`, à partir de statuts effectifs établis.
- Inconnus seuls, `UNCERTAIN` seuls, ou inconnus + `UNCERTAIN` sans ingrédient établi ne produisent aucun verdict établi.
- Les résultats établis `VEGAN` et `VEGETARIAN` avec inconnus restent informatifs et ne remplacent jamais le verdict principal.
- Un `NON_VEGAN` connu, une origine explicitement non vegan et leurs inconnus/incertains ne produisent pas d’encart établi trompeur.
- Les traces seules ou combinées restent hors du verdict principal, du conditionnel et de l’encart établi.
- Les occurrences multiples, chemins imbriqués et explications existantes restent conservés.
- Une absence de liste d’ingrédients produit une absence de verdict et d’encart.
- Les quatre ensembles de ressources FR/NL/EN/DE gardent la même structure ; la vérification confirme 80 clés dans chacun.

## Tests ajoutés

Dans `mutation-core/src/test/kotlin/com/example/isitvegan/VerdictExplanation069Test.kt`, les verrous complémentaires suivants ont été ajoutés :

- origine explicitement non vegan : `establishedIngredientVerdict == null`;
- traces seules : disponibilité `NO_INGREDIENT_LIST`, verdict principal nul et verdict établi nul;
- aucune liste exploitable : verdict établi nul en plus de la raison structurée existante.

Les autres cas de la matrice étaient déjà couverts dans la même suite : inconnus seuls, `UNCERTAIN` seuls, inconnus + `UNCERTAIN`, vegan/vegetarian établis avec inconnus, `NON_VEGAN`, traces avec éléments analysés, imbrication et occurrences multiples. La parité des libellés et des ressources est protégée par `UiLanguageTest` et les tests instrumentés de rendu.

## Validations

- `.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.VerdictExplanation069Test --console=plain` : succès.
- `.\gradlew.bat :mutation-core:test --console=plain` : succès.
- `.\gradlew.bat testDebugUnitTest --console=plain` : succès.
- `.\gradlew.bat assembleDebug --console=plain` : succès.
- `.\gradlew.bat assembleDebugAndroidTest --console=plain` : succès.
- `adb devices` : appareil disponible (`Nokia G42 5G - 15`).
- `.\gradlew.bat connectedDebugAndroidTest --console=plain` : succès.
- Parité des ressources : 80 clés identiques dans `values`, `values-en`, `values-nl` et `values-de`.
- `git diff --check` : succès.
- PIT : non lancé.

## Fichiers modifiés

Cette mission a modifié uniquement :

- `mutation-core/src/test/kotlin/com/example/isitvegan/VerdictExplanation069Test.kt` ;
- `PRE_0_7_VERDICT_LOGIC_LOCK_REPORT.md`.

Les sources de production, `VerdictEngine`, `VeganAnalyzer`, données, alias, OCR/ML Kit, assets, Gradle et version sont inchangés par cette mission. Aucun commit ni push n’a été effectué.

## Zones laissées à la v0.7

La consolidation multilingue, la validation inter-langues, l’évolution structurelle éventuelle des composites et toute nouvelle règle métier restent hors de ce verrouillage. PIT reste également hors de cette campagne test-only.
