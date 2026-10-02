# Relecture 0.6.9.9 — verdict informatif des ingrédients au statut établi

## Décision

**GO avant commit.** L’encart est une vue informative dérivée et ne peut pas modifier ni remplacer le verdict principal. Aucun problème bloquant ou important n’a été identifié.

## Calcul dérivé

La seule construction de `establishedIngredientVerdict` est dans `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticModels.kt:224-232` :

1. elle part exclusivement de `result.matched` ;
2. elle exclut chaque ingrédient au statut effectif `UNCERTAIN` ;
3. elle exige une liste non vide ;
4. elle n’est calculée que si le verdict principal vaut `UNCERTAIN` ou `INCONCLUSIVE` ;
5. elle réutilise `VerdictEngine.evaluate(establishedIngredients, emptyList())` ;
6. elle n’est exposée que pour `VEGAN` ou `VEGETARIAN`.

`result.matched` est rempli avec `effectiveIngredients` (`VeganAnalyzer.kt:353-362`), dont le statut provient de `resolution.effectiveStatus`. L’encart utilise donc bien la classification effective après qualification d’origine, jamais le statut brut, le texte OCR ou les champs de diagnostic. Les nœuds composites ne passent pas dans le matching (`VeganAnalyzer.kt:286-322`).

Les inconnus ne figurent pas dans `matched`; les traces sont construites séparément dans `crossContactWarnings` (`VeganAnalyzer.kt:433-438`) et les tests historiques vérifient qu’elles ne créent pas de correspondances. Les références de diagnostic, les chemins et les occurrences ne sont jamais lus par le calcul.

Les trois cas exigés sans statut établi sont couverts par la garde statique `establishedIngredients.isNotEmpty()` :

| Entrée analysable | `result.matched` après filtre | Encart |
|---|---:|---|
| uniquement inconnus | vide | absent |
| uniquement `UNCERTAIN` | vide | absent |
| inconnus + `UNCERTAIN` | vide | absent |

Il est donc impossible que l’absence de statut établi devienne `VEGAN` : `VerdictEngine.evaluate(..., emptyList())` n’est même pas appelée dans ce cas. À titre de défense supplémentaire, son résultat pour une liste vide serait `INCONCLUSIVE`, puis serait refusé par le filtre `VEGAN`/`VEGETARIAN`.

## Règles métier et rendu

- `AnalysisResult.verdict` et `VerdictEngine` ne font partie d’aucun fichier modifié ; le diff confirme leur intégrité.
- Le formatter rend d’abord `result.verdict` (`VerdictExplanationFormatter.kt:57-63`), puis l’encart (`65-72`) avant les listes de bloqueurs, incertains et inconnus (`74-98`).
- Avec un `NON_VEGAN` connu, le verdict principal est `NON_VEGETARIAN`; la condition de création de l’encart échoue. Les bloqueurs et incertains restent rendus.
- Les traces restent hors des deux calculs et sont rendues séparément après le résultat.
- Les tests existants conservent les occurrences identiques et les chemins imbriqués; le calcul ne réécrit aucun `TokenDiagnostic` ni `VerdictIngredientReference`.
- `MainActivity.kt:157-161` et `OcrFirstScreen.kt:284-285` appellent réellement le même `VerdictExplanationFormatter`.

La note est placée immédiatement après l’encart et indique, dans chaque langue, que les éléments incertains ou non identifiés restent listés et empêchent la confirmation globale. Aucun texte utilisateur nouveau n’est codé en dur dans le formatter.

## Langues et tests

Les clés `established_vegan`, `established_vegetarian` et `established_ingredients_notice` sont présentes dans `values`, `values-en`, `values-nl` et `values-de`. La vérification statique de cette revue confirme **80 clés identiques** dans chacun des quatre fichiers. `UiLanguageTest` protège aussi cette parité.

L’attente instrumentée modifiée dans `MainScreenInstrumentedTest.kt:117-127` reste une vérification utile : elle maintient l’assertion du verdict principal `INCERTAIN`, puis vérifie précisément le nouveau libellé informatif. Elle ne masque donc pas une régression du verdict principal. Le cas `NON_VEGETARIAN` voisin vérifie l’absence du même encart (`130-144`).

La couverture actuelle vérifie les cas vegan, végétarien, non vegan, inconnus avec vegan connu, incertains avec vegan connu, traces, chemins imbriqués, langues et les deux parcours. Le rapport de livraison atteste également les suites JVM, unitaires Android, assemblages et suite connectée; cette revue a relancé avec succès `:mutation-core:test --tests com.example.isitvegan.VerdictExplanation069Test`.

## Problèmes

| Gravité | Constat | Fichier / ligne | Effet |
|---|---|---|---|
| Bloquant | Aucun. | — | — |
| Important | Aucun. | — | — |
| Mineur | Les trois cas vides explicités dans cette revue (inconnus seuls, `UNCERTAIN` seuls, combinaison des deux) ne possèdent pas chacun une assertion dédiée sur `establishedIngredientVerdict`; `emptyOrUninterpretableInputHasNoConditionalResult` à la ligne 234 ne vérifie que l’ancien champ conditionnel. | `mutation-core/src/test/kotlin/com/example/isitvegan/VerdictExplanation069Test.kt:234` | La garde de production rend le comportement sûr et vérifiable statiquement; une future amélioration de couverture pourrait ajouter ces trois assertions sans modifier le comportement. |

## Contrôles et état Git

- `git diff --check` : succès.
- Diff examiné intégralement pour les sources, tests, ressources, version et documents 0.6.9.9.
- PIT non lancé sur `master`.
- État Git à la fin : changements 0.6.9.9 non indexés, plus `VERDICT_ESTABLISHED_INGREDIENTS_0_6_9_9_REPORT.md` et le présent rapport non suivis; aucun commit ni push.

Cette revue n’a modifié aucun fichier existant, aucun test, aucune donnée, ressource, configuration, index Git, commit ou push. Le seul fichier créé par cette mission est `REVIEW_0_6_9_9_ESTABLISHED_INGREDIENTS.md`.
