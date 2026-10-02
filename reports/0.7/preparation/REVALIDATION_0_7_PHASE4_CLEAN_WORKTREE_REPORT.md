# Revalidation finale v0.7 — phase 4 — clean worktree

## Décision

**GO_TO_PHASE5_WITH_WARNINGS**

La phase 4 est validée fonctionnellement. Les tests JVM ciblés, la suite `:mutation-core`, `testDebugUnitTest`, le test instrumenté ciblé et la suite instrumentée complète passent. Aucun changement de production ni différence métier n’a été observé.

Les avertissements restants sont l’absence de trois rapports demandés dans cette copie, les avertissements Gradle/JVM de l’environnement, et le délai important de la suite Android complète. Ils ne bloquent pas la validation.

## Référence et état initial

- Commit vérifié : `21454d9 perf(v0.7): optimize linked alias matcher preparation`.
- Worktree : `HEAD (no branch)`, détaché sur le commit de référence.
- `git status --short --branch` initial : `## HEAD (no branch)` ; aucun fichier modifié.
- `git worktree list` confirmait les deux worktrees sur `21454d9` : le dépôt principal et `Is-it-Vegan-phase4-clean`.
- Le code de production de la référence a été comparé à `HEAD` : aucune différence.
- Appareil : `Nokia G42 5G - Android 15`, serial `AQ5003H044Q32600084`.
- `adb devices` : appareil présent et en état `device`.

Rapports demandés présents et lus :

- `PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md`
- `PRE_0_7_PHASE2_PARITY_PERFORMANCE_REPORT.md`
- `PRE_0_7_PHASE3_INDEXED_CANDIDATE_EXPERIMENT_REPORT.md`
- `PRE_0_7_VERDICT_LOGIC_LOCK_REPORT.md`
- `PRE_0_7_MATCHING_OCR_DIAGNOSTIC_AUDIT.md`

Rapports demandés absents de cette copie :

- `REVIEW_0_7_PHASE4_SEQUENCE_CORRECTION_REPORT.md`
- `REVIEW_0_7_PHASE4_POST_IMPLEMENTATION_REVIEW_REPORT.md`
- `REVALIDATION_0_7_PHASE4_FINAL_REPORT.md` au début de la revalidation ; le présent fichier est le nouveau rapport final demandé.

Le contenu des rapports absents n’a pas été supposé ni reconstitué.

## Fichiers de tests

Un seul fichier a été complété localement :

- `mutation-core/src/test/kotlin/com/example/isitvegan/Phase4MatcherProfileTest.kt` : ajout du compte exact de 2 485 alias normalisés, d’un test explicite des six connecteurs et d’un test FR/NL/EN/DE avec casse et espaces normalisés.

Le test instrumenté existait déjà dans le commit de référence et n’a pas été modifié :

- `app/src/androidTest/java/com/example/isitvegan/Phase4MatcherProfileInstrumentedTest.kt`

Le diff final ne contient aucun fichier de production, asset, donnée éditoriale, importeur, générateur, fichier de version ou migration.

## Commandes et résultats JVM

Les commandes ont été exécutées séquentiellement avec le JBR local d’Android Studio (`JAVA_HOME=C:\Program Files\Android\Android Studio\jbr`) :

1. `.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.Phase4MatcherProfileTest --console=plain` — **SUCCÈS**, `BUILD SUCCESSFUL`.
2. `.\gradlew.bat :mutation-core:test --console=plain` — **SUCCÈS**, `BUILD SUCCESSFUL`.
3. `.\gradlew.bat testDebugUnitTest --console=plain` — **SUCCÈS**, `BUILD SUCCESSFUL`.
4. `git diff --check` — **SUCCÈS**.

`pypdf` n’a généré aucune erreur. Aucun script Python, importeur ou générateur n’a été exécuté.

## Couverture et parité

Le test JVM vérifie explicitement, pour `de`, `d`, `du`, `des`, `a` et `au` :

- une correspondance positive ;
- la frontière valide après le connecteur ;
- le rejet d’un suffixe invalide ;
- le rejet d’un faux préfixe absent de la liste d’alias ;
- le rejet d’un alias court qui n’est pas lui-même un alias lié.

La liste actuelle contient exactement **2 485 alias normalisés**. Le prédicat pairwise historique et `IngredientMatcher.longerLinkedAliasBases` produisent les mêmes ensembles complets. Les résultats du matcher profilé et du matcher de référence sont également comparés sur le corpus de phase 4.

Le corpus couvre FR, NL, EN et DE, la casse, les espaces normalisés, les E-numbers, les tirets, les parenthèses, les collisions E470b/E572, les arômes, goûts, extraits, lait végétal, inconnus et occurrences répétées. Les cas imbriqués et les textes longs sont inclus dans les mesures et la comparaison.

Les suites existantes couvrent les statuts `VEGAN`, `VEGETARIAN`, `NON_VEGAN` et `UNCERTAIN`, les inconnus, traces, compositions, parents/enfants, pourcentages, occurrences, diagnostics et verdicts. Le diff de production est vide par rapport au commit de référence; aucune différence métier n’a été observée.

`KnowledgeIndex` et `IndexedCandidateProvider` restent expérimentaux et hors du chemin de production. Aucun asset n’a été modifié et aucune migration de stockage n’a été engagée.

## Commandes et résultats Android

Lancement ciblé demandé :

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --tests com.example.isitvegan.Phase4MatcherProfileInstrumentedTest --console=plain
```

Cette syntaxe est refusée par Gradle (`Unknown command-line option '--tests'`) avant l’exécution. La classe existant bien, le lancement équivalent du runner a été utilisé :

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.Phase4MatcherProfileInstrumentedTest" `
  --console=plain
```

Résultat : **SUCCÈS**, Nokia G42 5G Android 15, `BUILD SUCCESSFUL`.

Suite instrumentée complète :

```powershell
.\gradlew.bat connectedDebugAndroidTest --console=plain
```

Résultat : **SUCCÈS**, Nokia G42 5G Android 15, `BUILD SUCCESSFUL`.

Le test instrumenté reste limité au profilage et à la non-régression du matcher : construction, recherche exacte, recherche multilingue, E-number, texte contenant plusieurs alias, contexte protégé, absence de correspondance, parsing imbriqué, analyse simple, analyse imbriquée, analyse multilingue, traces, texte long, diagnostics et verdict. Il n’utilise ni réseau ni service externe et ne modifie ni assets ni base.

## Avertissements et limites

- Les trois rapports absents sont une limite documentaire de la copie propre.
- Gradle a signalé l’appel déprécié à `sun.misc.Unsafe` via protobuf.
- Le packaging Android a signalé deux bibliothèques natives non stripables : `libandroidx.graphics.path.so` et `libmlkit_google_ocr_pipeline.so`.
- La suite Android complète est longue ; elle a néanmoins abouti avec succès.
- Les mesures de temps et de mémoire restent indicatives et dépendantes du JIT, du GC, de la charge du téléphone et du daemon Gradle.
- Aucune régression de production n’a été observée et aucune modification de production n’est nécessaire.

## Clôture

Le commit de référence est vérifié, les tests JVM et Android pertinents passent, la parité historique/optimisée est démontrée sur les 2 485 alias normalisés, et le périmètre métier reste inchangé. La phase 4 peut donc passer à la phase 5 avec les avertissements documentés ci-dessus.
