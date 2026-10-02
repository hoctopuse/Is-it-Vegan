# Rapport d’explication des verdicts 0.6.9

Date : 25 septembre 2026  
Branche : `master`  
Commit de départ : `44cbc44` (`fix: preserve adjacent trace sections`)

## État initial

- Le worktree était propre (`git status --short` sans sortie).
- Les trois derniers commits étaient `44cbc44`, `3d7c3f4` et `53a6000`.
- Les baselines `:mutation-core:test`, `testDebugUnitTest` et `assembleDebug` passaient avant modification.
- Le cœur exposait déjà `verdictWithoutUncertain`, calculé par `VerdictEngine`, mais sans modèle regroupant les ingrédients exclus, leurs chemins, la raison du résultat et les garde-fous d’affichage.

## Version avant et après

| Champ | Avant | Après |
|---|---:|---:|
| `versionName` | `0.6.8.1` | `0.6.9` |
| `versionCode` | `35` | `36` |

Le `versionCode` a été incrémenté d’une unité, conformément à la convention observée dans l’historique Gradle. Aucun nouveau mécanisme de versionnement n’a été ajouté.

## Comportement existant conservé

- `AnalysisResult.verdict` et `veganAssessment` ne sont pas modifiés par l’explication.
- `VerdictEngine` conserve ses priorités et ses règles pour `VEGAN`, `VEGETARIAN`, `NON_VEGETARIAN`, `UNCERTAIN` et `INCONCLUSIVE`.
- Les ingrédients connus, les qualifications d’origine, les inconnus et les alias conservent leur traitement antérieur.
- Le parsing, la segmentation linguistique, ML Kit, la collecte des inconnus et la logique des traces n’ont pas été modifiés.
- Le calcul reste entièrement local, déterministe et sans dépendance externe.

## Nouveau modèle d’explication

`AnalysisDiagnostics.verdictExplanation` expose une vue structurée dérivée après l’analyse :

- `mainVerdict` et `mainVeganAssessment` ;
- `knownBlockingIngredients` avec identifiant, nom, statut, raison et chemin ;
- `uncertainIngredients`, avec une identité stable par occurrence et son chemin hiérarchique ;
- `conditionalVerdict` ;
- `conditionalReason` ;
- `vegetarianStatus` ;
- `tracesExcludedFromConditionalVerdict`.

Le diagnostic texte ajoute une section `EXPLICATION CONDITIONNELLE 0.6.9`. Le modèle ne relance ni parsing, ni matching. Il réutilise `AnalysisResult.verdictWithoutUncertain`, qui délègue déjà à `VerdictEngine` avec `excludeUncertain = true`.

## Règles du résultat conditionnel

Seules les correspondances dont le statut effectif est `UNCERTAIN` sont mises de côté. Les ingrédients inconnus restent dans l’entrée de décision et empêchent un résultat conditionnel vegan. Un résultat n’est affiché que si le reste vaut `VEGAN` ou `VEGETARIAN`.

Le résultat conditionnel est absent lorsque :

- aucune liste fiable n’a été analysée ;
- aucun ingrédient incertain n’est présent ;
- un inconnu reste dans la composition ;
- un ingrédient `NON_VEGAN` ou une origine explicitement non vegan reste bloquant ;
- le reste est vide ou inconclusif.

Les raisons correspondantes sont codées dans `ConditionalVerdictReason`. Un résultat conditionnel n’est jamais recopié dans le verdict principal.

## Cas VEGAN hors incertains

Pour une composition connue vegan complétée par un ou plusieurs ingrédients `UNCERTAIN`, le verdict principal reste `UNCERTAIN` et `conditionalVerdict` vaut `VEGAN`. L’interface affiche la formulation localisée « Vegan hors ingrédient incertain ».

## Cas VÉGÉTARIEN hors incertains

Si le reste contient un ingrédient reconnu `VEGETARIAN` et aucun élément `NON_VEGAN` ou inconnu, `conditionalVerdict` vaut `VEGETARIAN`. Ce statut reste informatif et l’interface affiche « Végétarien hors ingrédient incertain ».

## Cas avec ingrédient animal connu

Un ingrédient `NON_VEGAN` connu reste dans le calcul. Il apparaît dans `knownBlockingIngredients`, conserve le verdict principal `NON_VEGETARIAN` et supprime le résultat conditionnel. Un ingrédient compatible avec le végétarisme mais non vegan conserve le comportement existant `VEGETARIAN` et peut produire uniquement l’information conditionnelle végétarienne.

## Compositions imbriquées

Le chemin est reconstruit depuis `TokenDiagnostic.order` et `parentOrder`, sans reparsing. Une occurrence telle que `préparation végétale [eau, arôme]` est exposée comme `préparation végétale → arôme`. Plusieurs occurrences du même concept restent visibles même lorsque leur chemin textuel est identique.

## Traces

Les sections « peut contenir » restent hors des tokens et hors des deux verdicts. Le modèle marque explicitement `tracesExcludedFromConditionalVerdict = true`. Les tests vérifient qu’une trace de lait ou de gélatine ne devient ni bloqueur connu, ni cause du résultat conditionnel.

## Langues supportées

Les nouvelles formulations sont fournies par les ressources Android en français, anglais, néerlandais et allemand. `UiLanguage.DE` sélectionne les ressources allemandes et donne la priorité au bloc allemand à qualité égale ; cela ne change aucune règle de verdict ou de classification.

## Fichiers modifiés

- Cœur et diagnostic : `DiagnosticModels.kt`, `DiagnosticReport.kt`, `VeganAnalyzer.kt`, `UiLanguage.kt`, `LabelLanguageSegmenter.kt`.
- Android : `VerdictExplanationFormatter.kt`, `OcrFirstScreen.kt`, `MainActivity.kt`, `OcrProcessor.kt`.
- Ressources : `values/strings.xml`, `values-en/strings.xml`, `values-nl/strings.xml`, `values-de/strings.xml`.
- Version : `app/build.gradle.kts`.
- Tests : `VerdictExplanation069Test.kt`, `UiLanguageTest.kt`, `VerdictExplanationInstrumentedTest.kt`, `MainScreenInstrumentedTest.kt`.
- Documentation : `docs/architecture.md`, `docs/diagnostic.md`, `docs/langues.md`, `docs/tests.md`, `docs/verdict.md` et le présent rapport.

`ingredients.json`, les alias, les assets OCR et les autres assets applicatifs n’ont pas été modifiés.

## Tests ajoutés ou adaptés

Les tests JVM structurés couvrent :

- `UNCERTAIN` avec reste `VEGAN` ;
- `UNCERTAIN` avec reste `VEGETARIAN` ;
- ingrédient `NON_VEGAN` connu avec ingrédient incertain ;
- plusieurs ingrédients incertains ;
- composition imbriquée et chemin complet ;
- traces présentes mais exclues ;
- identité du verdict entre analyse simple, diagnostic et explication ;
- absence de résultat avec bloqueur connu, inconnu, texte vide ou composition inexploitable ;
- export du modèle dans `DiagnosticReport`.

Les tests Android vérifient l’affichage Compose du verdict principal et du résultat conditionnel, ainsi que les quatre localisations. Le premier lancement global a révélé que le nouveau test Compose pouvait cliquer avant le chargement asynchrone de la base ; le test attend désormais explicitement que le bouton soit activé. La classe ciblée puis la campagne complète passent.

## Résultats des validations

Baseline avant modification :

| Commande | Résultat |
|---|---|
| `.\gradlew :mutation-core:test` | succès |
| `.\gradlew testDebugUnitTest` | succès |
| `.\gradlew assembleDebug` | succès |

État final :

| Commande | Résultat |
|---|---|
| `.\gradlew :mutation-core:test` | succès |
| `.\gradlew testDebugUnitTest` | succès |
| `.\gradlew assembleDebug` | succès |
| `.\gradlew assembleDebugAndroidTest` | succès |
| test Compose 0.6.9 ciblé | succès |
| classe `MainScreenInstrumentedTest` | succès |
| `.\gradlew connectedDebugAndroidTest` | succès, 30 tests sur Nokia G42 5G / Android 15 |

## Confirmation du verdict principal

Le verdict principal existant n’a pas été modifié. Les tests comparent explicitement `IngredientAnalysisService.analyze(...).verdict`, `analyzeWithDiagnostics(...).result.verdict` et `verdictExplanation.mainVerdict`. Le résultat conditionnel reste un champ explicatif séparé.

## Limites restantes

- Un reste composé uniquement d’ingrédients incertains devient vide après exclusion et ne produit donc aucun résultat conditionnel.
- Un texte réellement inconnu reste `INCONCLUSIVE` hors incertains ; il n’est jamais assimilé à un ingrédient vegan ou végétarien.
- Les raisons des ingrédients restent conservées dans le diagnostic structuré et le rapport. Elles proviennent de la base existante et ne sont pas injectées dans le rendu multilingue lorsqu’elles ne disposent pas de traduction.
- Le chemin repose sur la hiérarchie de tokens existante et n’ajoute aucun nouveau niveau si le parseur n’en a pas produit.

## Recommandations pour la prochaine étape OCR multilingue

1. Ajouter des fixtures d’étiquettes FR/NL/EN/DE contenant le même ingrédient incertain à plusieurs profondeurs.
2. Vérifier le rendu localisé après changement de bloc OCR sans relancer ML Kit.
3. Conserver les identifiants de bloc et les chemins de tokens dans les futurs tests OCR afin de distinguer erreur de sélection et erreur d’explication.
4. Ajouter des cas allemands et néerlandais avec traces adjacentes et ingrédient incertain dans la composition principale.
5. Maintenir la séparation actuelle entre texte OCR, parsing, matching, verdict principal et explication conditionnelle.

## Confirmations finales

- Aucun commit effectué.
- Aucun push effectué.
- Aucun asset ni `ingredients.json` modifié.
- Aucune règle de verdict connue modifiée sans test ; `VerdictEngine` est inchangé.
- Aucun texte utilisateur 0.6.9 non localisé ajouté.

## État final de Git

`git diff --check` réussit. Le worktree contient uniquement les modifications et nouveaux fichiers décrits dans ce rapport : 19 fichiers suivis modifiés et 5 chemins non suivis (`VERDICT_EXPLANATION_0_6_9_REPORT.md`, le formatter Android, les tests JVM/instrumentés et `values-de/`). Aucun fichier n’est indexé, commité ou poussé.
