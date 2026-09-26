# Rapport de correction de l’explication des verdicts 0.6.9

Date : 25 septembre 2026  
Branche : `master`  
Version conservée : `0.6.9` / `versionCode 36`

## Problèmes repris de la seconde passe

La seconde revue avait identifié quatre écarts :

1. des occurrences incertaines identiques pouvaient être fusionnées par
   `distinctBy { ingredientId to path }` ;
2. les parcours Compose n’affichaient pas les ingrédients incertains lorsque le
   verdict principal était `NON_VEGETARIAN` ;
3. le parcours de résultat et les traces contenaient encore des formulations
   françaises codées en dur ;
4. la documentation affirmait à tort que les propriétés diagnostiques ne
   rappelaient jamais le moteur de verdict.

## Conservation des occurrences

`VerdictIngredientReference` expose désormais un `occurrenceId` stable construit
à partir de l’ordre du token et de l’index de sa correspondance. La déduplication
par identifiant d’ingrédient et chemin textuel a été supprimée.

Chaque token analysé produit donc sa propre référence, dans l’ordre d’apparition.
Deux occurrences `arôme`, deux compositions `préparation [arôme]` portant le
même chemin textuel, et deux occurrences placées sous des chemins différents
restent toutes distinctes. Le rapport de diagnostic inclut aussi cette identité
d’occurrence. Le calcul du verdict n’a pas été modifié.

## Affichage NON VEGAN avec incertains

`OcrFirstScreen` et le composable historique de `MainActivity` délèguent leur
rendu de résultat à `VerdictExplanationFormatter`. Le formatter utilise
`AnalysisDiagnostics.verdictExplanation` pour présenter séparément :

- le verdict principal ;
- les causes connues incompatibles avec un verdict vegan ;
- toutes les occurrences incertaines ;
- les ingrédients non identifiés ;
- le résultat conditionnel lorsqu’il existe ;
- les traces et leur exclusion des calculs.

Pour `gélatine + arôme`, le verdict principal reste `NON_VEGETARIAN`, la
gélatine et l’arôme sont visibles dans deux catégories distinctes, et aucun
résultat « Vegan hors ingrédient incertain » n’est affiché. Plusieurs bloqueurs
et plusieurs incertains sont également conservés. Une origine explicitement non
vegan reste bloquante sans transformer le verdict détaillé existant.

## Localisation

Les verdicts, titres de listes, textes d’inconclusion, résultats conditionnels,
libellés de compatibilité et mentions de traces du parcours de résultat utilisent
des ressources Android FR, EN, NL et DE. Les quatre fichiers contiennent les
mêmes 71 clés.

Les raisons techniques issues de la base restent disponibles dans le modèle et
le rapport diagnostique. Comme elles ne disposent pas de traductions dans la
base existante, le formatter multilingue ne les injecte pas dans l’interface.
Le nom et le chemin de l’ingrédient restent naturellement ceux lus sur
l’étiquette.

Les traces sont affichées séparément avec un titre et une mention d’exclusion
localisés. Leur texte brut reste celui de l’étiquette ; elles ne deviennent ni
ingrédients déclarés ni causes d’un verdict.

## Correction documentaire

`docs/diagnostic.md` et `docs/architecture.md` indiquent désormais :

- qu’il n’existe pas de second moteur divergent ;
- que `verdictExplanation` consulte la propriété calculée
  `verdictWithoutUncertain` ;
- que cette propriété peut rappeler `VerdictEngine.evaluate` en excluant
  uniquement les statuts `UNCERTAIN` ;
- que `AnalysisResult.verdict` reste la référence métier ;
- que le résultat conditionnel est une vue informative distincte.

`docs/verdict.md`, `docs/langues.md` et `docs/tests.md` décrivent aussi le
formatter localisé partagé et les nouvelles garanties de test.

## Tests ajoutés ou adaptés

Tests JVM structurés :

- deux ingrédients incertains identiques conservés et ordonnés ;
- deux occurrences identiques sous un même chemin conservées ;
- occurrences sous des chemins différents conservées ;
- plusieurs bloqueurs et plusieurs incertains séparés ;
- origine explicitement non vegan avec autre incertain ;
- verdict principal inchangé et résultat conditionnel absent en présence d’un
  bloqueur ;
- égalité des clés de ressources FR, EN, NL et DE ;
- utilisation du formatter partagé par les deux parcours.

Tests Android :

- rendu conditionnel vegan et végétarien dans les quatre langues ;
- rendu NON VEGAN avec cause connue et ingrédient incertain dans les quatre
  langues ;
- titre et mention d’exclusion des traces dans les quatre langues ;
- affichage Compose de `gélatine + E471` sans résultat conditionnel.

Les tests 0.6.9 existants continuent de couvrir le chemin imbriqué, les traces
exclues, les inconnus, les textes vides, les résultats conditionnels vegan et
végétarien, ainsi que l’identité du verdict principal avant et après
enrichissement.

## Validations exécutées

Baseline avant correction :

| Commande | Résultat |
|---|---|
| `.\gradlew :mutation-core:test` | succès |
| `.\gradlew testDebugUnitTest` | succès |

Validation finale :

| Commande | Résultat |
|---|---|
| `.\gradlew :mutation-core:test` | succès |
| `.\gradlew testDebugUnitTest` | succès |
| `.\gradlew assembleDebug` | succès |
| `.\gradlew assembleDebugAndroidTest` | succès |
| test instrumenté ciblé du formatter et du cas NON VEGAN + incertain | succès |
| `.\gradlew connectedDebugAndroidTest` | succès, 34 tests, 0 échec, Nokia G42 5G / Android 15 |
| `git diff --check` | succès |

## Verdict principal et moteur

`VerdictEngine.kt` n’a pas changé. Les règles, priorités et résultats du verdict
principal restent identiques. L’enrichissement lit les tokens et le résultat
existants ; il ne participe pas à la décision. Les ingrédients `NON_VEGAN`
connus et les origines explicitement non vegan ne sont jamais exclus avec les
incertains.

## Fichiers concernés par cette correction

- Cœur et diagnostic :
  `DiagnosticModels.kt`, `DiagnosticReport.kt`.
- Rendu Android :
  `VerdictExplanationFormatter.kt`, `OcrFirstScreen.kt`,
  `MainActivity.kt`.
- Ressources :
  `values/strings.xml`, `values-en/strings.xml`,
  `values-nl/strings.xml`, `values-de/strings.xml`.
- Tests :
  `VerdictExplanation069Test.kt`, `UiLanguageTest.kt`,
  `VerdictExplanationInstrumentedTest.kt`,
  `MainScreenInstrumentedTest.kt`.
- Documentation :
  `docs/architecture.md`, `docs/diagnostic.md`, `docs/langues.md`,
  `docs/tests.md`, `docs/verdict.md`,
  `VERDICT_EXPLANATION_0_6_9_REPORT.md` et le présent rapport.

Les autres fichiers déjà modifiés dans le worktree appartiennent à l’évolution
0.6.9 initiale et n’ont pas été élargis par cette correction.

## Limites restantes

- Une composition ne contenant que des ingrédients incertains ne produit toujours
  aucun résultat conditionnel, puisque le reste ne permet aucune conclusion.
- Les raisons internes de la base ne sont pas traduites ; elles restent dans le
  diagnostic et ne sont pas affichées dans le résultat multilingue.
- Les chemins reflètent exclusivement la hiérarchie produite par le parseur
  existant.

Aucun point restant ne constitue un NO-GO pour le commit de l’évolution 0.6.9.

## Confirmations

- `VerdictEngine` est inchangé.
- Les verdicts principaux sont inchangés.
- `ingredients.json`, les alias, ML Kit, le pipeline OCR et les assets sont
  inchangés.
- Aucun texte utilisateur ajouté au parcours de résultat n’est codé en dur dans
  les composants Compose.
- Aucun commit n’a été effectué.
- Aucun push n’a été effectué.

Décision avant commit : **GO**.
