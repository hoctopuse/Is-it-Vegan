# Référence de performance v0.7 avant phase 5 — Nokia G42 5G

## Référence et périmètre

Campagne du 2 octobre 2026, sur `master`, `HEAD` complet `9d98825d22caa9d5a8ac65406c65e424dd1ac195` (`test(v0.7): finalize clean-worktree phase 4 validation`). Le dépôt mesuré est `C:\Users\Sebastien\StudioProjects\Is-it-Vegan`, worktree principal de `https://github.com/hoctopuse/Is-it-Vegan.git`. Le chemin demandé `C:\Users\seb\AndroidStudioProjects\IsitVegan` n'existe pas dans cet environnement : les quatre commandes Git initiales sur ce chemin ont échoué avant toute mesure. Le dépôt disponible contient `21454d9` dans les cinq derniers commits et le protocole demandé. `git status --short --branch` avant le test : `## master...origin/master`, sans fichier modifié ou non suivi ; même état après le test, avant la rédaction de ce rapport. Aucun fichier de production, donnée, asset, version ou migration n'a été changé. Aucun commit ni push.

Appareil `AQ5003H044Q32600084`, `Nokia G42 5G`, Android `15`, état ADB `device`. Batterie avant : 100 %, USB alimenté, statut Android `4` (ne charge pas), 25,2 °C. Après : 100 %, USB alimenté, statut `4`, 28,0 °C. Le service thermique indiquait le statut `0` avant et après, et aucun dispositif de refroidissement actif ; température de peau HAL de 26,0 à 31,4 °C. Le téléphone a donc chauffé pendant le passage, sans limitation thermique déclarée. Les mesures sont sensibles à la fréquence CPU, au JIT, au GC et à la charge de fond ; un écart de quelques millisecondes ne prouve pas un gain.

Le chargeur de connaissance Android lit `ingredients.json`, `ingredient_aliases_multilingual.json` et `origin_qualifier_rules.json` dans les assets de l'APK. Le test utilise ces mêmes trois assets via `Context.assets` et n'exécute aucun appel réseau. Le chemin d'analyse mesuré dépend donc seulement de données locales d'après le code inspecté. Le manifeste principal ne demande pas `INTERNET`, mais le manifeste **fusionné** de l'APK contient cette permission, apportée par une dépendance ; la campagne n'a pas désactivé le réseau du téléphone et ne prouve pas expérimentalement le fonctionnement hors ligne de toute l'application. Les URL présentes dans les règles sont des références documentaires dans l'asset, pas des requêtes du test.

## Protocole exécuté

Test inchangé `app/src/androidTest/java/com/example/isitvegan/Phase4MatcherProfileInstrumentedTest.kt` tel que versionné à ce `HEAD`, méthode `profilesCurrentMatcherOnDeviceWithoutChangingAnalysisPath`, APK `debug`, runner `androidx.test.runner.AndroidJUnitRunner`. JBR d'Android Studio : `C:\Program Files\Android\Android Studio\jbr`. Un seul lancement ciblé via `-Pandroid.testInstrumentationRunnerArguments.class=...`, car `--tests` n'est pas accepté par la tâche connectée. Le test effectue 5 échauffements puis 10 répétitions séquentielles par opération ; il trie les dix durées et publie minimum, valeur d'indice 4 étiquetée « median », et maximum, arrondis à la microseconde. Cette « médiane » est la médiane basse, pas la moyenne des deux valeurs centrales. Les dix échantillons individuels ne sont pas journalisés : les triplets ci-dessous sont toutes les valeurs brutes conservées par le runner, sans reconstitution de mesures absentes.

Le protocole distingue lecture des trois assets, `MiniJson.parse` de chacun, `IngredientKnowledge.fromJson` (qui **inclut** son propre parsing JSON, le chargement du lexique et des règles), construction du matcher, parsing d'une composition imbriquée, recherches du matcher, analyses complètes simples, imbriquées, multilingues, avec traces, texte long et diagnostics. Il ne chronomètre pas `IngredientTreeParser.parse` sur une composition simple isolée. Pour la comparaison après phase 5, un ajout temporaire limité à `androidTest` pourrait mesurer ce scénario avec la même méthode et les mêmes chaînes ; il n'est pas implémenté dans cette campagne.

Comptes du corpus mesuré : **479 concepts** dans `ingredients.json` et **1 996 mappings** dans `ingredient_aliases_multilingual.json`, comptés directement dans les assets locaux ; **2 490 entrées d'alias** et **2 485 alias normalisés distincts**, publiés par `MatcherProfileCollector` sur l'appareil. Les 1 668 groupes d'alias du lexique sont distincts des 1 996 mappings.

## Résultats directs sur Nokia

Toutes les durées sont en **µs**. Chaque ligne représente `n=10`, après `warmup=5`. Les valeurs sont recopiées du logcat conservé pour **ce passage du 2 octobre 2026**.

| Opération journalisée | Min | Médiane basse | Max |
|---|---:|---:|---:|
| `asset-read-ingredients` | 3 912 | 4 022 | 4 188 |
| `asset-read-mappings` | 13 819 | 14 353 | 15 172 |
| `asset-read-rules` | 400 | 458 | 534 |
| `parse-ingredients` | 21 305 | 21 542 | 27 866 |
| `parse-mappings` | 93 677 | 98 584 | 102 762 |
| `parse-rules` | 1 338 | 1 373 | 1 438 |
| `deserialize-knowledge` | 1 005 244 | **1 018 438** | 1 058 928 |
| `build-matcher` | 1 090 748 | **1 106 376** | 1 110 962 |
| `match-exact` | 17 580 | 25 908 | 27 744 |
| `match-multilingual` | 15 556 | 15 814 | 17 908 |
| `match-enumber` | 15 449 | 16 095 | 19 369 |
| `match-contained` | 23 066 | 23 856 | 28 658 |
| `match-protected-context` | 16 199 | 17 480 | 21 026 |
| `match-no-match` | 17 119 | 17 599 | 23 138 |
| `parse-composition` (imbriquée) | 1 698 405 | **1 701 108** | 1 723 155 |
| `lexical-resolution` | 75 512 | 76 994 | 79 094 |
| `origin-rule` | 154 315 | 155 601 | 157 030 |
| `analysis-simple` | 1 974 176 | **1 986 847** | 2 074 977 |
| `analysis-nested` | 3 303 926 | **3 308 515** | 3 318 411 |
| `analysis-multilingual` | 2 668 650 | **2 675 562** | 2 692 383 |
| `analysis-traces` | 2 164 476 | 2 165 949 | 2 176 308 |
| `analysis-long` | 8 556 148 | 8 565 925 | 8 677 761 |
| `analysis-no-match` | 1 610 414 | 1 614 801 | 1 620 729 |
| `diagnostics` | 1 185 918 | 1 190 369 | 1 193 271 |
| `verdict` | 1 970 714 | 1 975 381 | 1 980 389 |

Profil du matcher publié dans le même logcat : `aliases=2490`, `normalizedAliases=2485`, `matchCalls=96`, `regexTests=239040`, `candidates=352`, `blocked=16`, `selected=304`. Sous-étapes de la construction profilée, en µs : `construction=1094154`, `normalizationPreparation=496849`, `aliasPreparation=585829`, `regexPreparation=58517`, `linkedAliasChecks=4151`. Ce sont des compteurs et une construction profilée distincte des dix répétitions de `build-matcher` ; les sous-étapes ne forment pas une décomposition additive complète. PSS du processus : `pssBefore=131826KB`, `pssAfter=145785KB` ; ce delta global dépend du GC et ne mesure pas la mémoire propre à la désérialisation ou au parser.

Résultat fonctionnel **direct** : JUnit rapporte **1 test, 0 échec, 0 erreur, 0 ignoré**, durée de la méthode 422,112 s ; Gradle rapporte `BUILD SUCCESSFUL in 7m 53s`. Les assertions de ce test vérifient l'égalité des résultats des matchers avec et sans collecteur pour six entrées (`eau`, E 471, contexte arôme, allemand, texte long, inconnu), ainsi que la présence d'alias et de tests regex dans le profil. Les analyses, verdicts et diagnostics sont exécutés et chronométrés mais leurs résultats métier individuels ne sont **pas** assertés par ce test. Les rapports de revalidation de phase 4 décrivent une couverture fonctionnelle plus large issue d'autres suites ; ces suites n'ont pas été relancées ici.

## Comparaison indicative au rapport de phase 4

`PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md` donne des mesures **JVM**, pas Android, pour la construction complète du matcher (médiane 33 127 µs), l'analyse simple (44 945 µs), imbriquée (55 372 µs) et multilingue (42 636 µs). Les médianes mesurées ici sur le Nokia sont respectivement 1 106 376, 1 986 847, 3 308 515 et 2 675 562 µs. Cette juxtaposition situe les ordres de grandeur, mais les plateformes, conditions et parfois les corpus diffèrent ; aucun ratio de gain ou de régression n'en découle. Le rapport de phase 4 indique explicitement que son test Android n'avait alors pas été exécuté. Sa réduction JVM de 99,2 % concerne seulement le prédicat de préparation des alias liés, et ne constitue ni une référence Android ni une mesure de phase 5. Aucune comparaison `21454d9^` contre `21454d9` n'a été effectuée.

## Valeurs à reprendre après phase 5

| Mesure et scénario identiques | Référence médiane basse Nokia (µs) |
|---|---:|
| `deserialize-knowledge`, mêmes 3 assets | 1 018 438 |
| `parse-composition`, même chaîne imbriquée | 1 701 108 |
| `build-matcher`, mêmes 479 concepts | 1 106 376 |
| `analysis-simple`, `eau, sucre` | 1 986 847 |
| `analysis-nested`, même composition imbriquée | 3 308 515 |
| `analysis-multilingual`, même étiquette anglaise | 2 675 562 |
| Résultat du test ciblé | 1/1 réussi |

Reprendre aussi les triplets min/médiane basse/max, `n=10`, les comptes du corpus, l'état batterie/thermique et les assertions fonctionnelles, sur le même appareil et avec le même runner. Aucun seuil de performance ni choix d'architecture n'est fixé ici.

## Artefacts et commandes

Artefacts locaux du passage : `app/build/pre_phase5_gradle.log` (sortie Gradle), `app/build/outputs/androidTest-results/connected/debug/TEST-Nokia G42 5G - 15.xml` (JUnit) et `app/build/outputs/androidTest-results/connected/debug/Nokia G42 5G - 15/logcat-com.example.isitvegan.Phase4MatcherProfileInstrumentedTest-profilesCurrentMatcherOnDeviceWithoutChangingAnalysisPath.txt` (lignes brutes `PHASE4_ANDROID_*`). Ces artefacts sous `build/` sont locaux et peuvent être supprimés par un nettoyage Gradle ; les triplets conservés dans ce rapport sont leur transcription durable. La sortie Gradle comporte un avertissement `sun.misc.Unsafe` provenant de protobuf, sans échec.

Commandes exécutées dans cet ordre logique ; les lectures indépendantes d'une même étape ont été lancées en parallèle, le test lourd seul :

| Commande exacte ou groupe de commandes exactes | Résultat |
|---|---|
| `git -C "C:\Users\seb\AndroidStudioProjects\IsitVegan" status --short --branch` ; `git -C "C:\Users\seb\AndroidStudioProjects\IsitVegan" log -5 --oneline` ; `git -C "C:\Users\seb\AndroidStudioProjects\IsitVegan" rev-parse HEAD` ; `git -C "C:\Users\seb\AndroidStudioProjects\IsitVegan" worktree list` | Chacune : échec `No such file or directory` ; aucune mutation. |
| `adb devices` ; `adb shell getprop ro.product.model` ; `adb shell getprop ro.build.version.release` | `device` ; Nokia G42 5G ; Android 15. |
| `git status --short --branch` ; `git log -5 --oneline` ; `git rev-parse HEAD` ; `git worktree list` ; `git remote -v` ; `git status --porcelain=v1` | Dépôt principal propre, `master`, SHA ci-dessus, historique attendu. |
| `rg --files -g "PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md" -g "Phase4MatcherProfileInstrumentedTest.kt" -g "AGENTS.md"` ; `Get-Content PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md` ; `Get-Content app/src/androidTest/java/com/example/isitvegan/Phase4MatcherProfileInstrumentedTest.kt` ; `Get-Content AGENTS.md` | Fichiers présents et protocole vérifié. |
| `Get-Content app/build.gradle.kts` ; `Get-Content gradle.properties` ; `Get-Content local.properties` ; `Get-Content app/src/main/AndroidManifest.xml` ; `Get-Content app/src/main/java/com/example/isitvegan/AndroidIngredientKnowledgeLoader.kt` ; `rg -n "INTERNET\|uses-permission" app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml` | Runner, JBR/SDK et assets locaux vérifiés ; permission `INTERNET` constatée dans le manifeste fusionné. |
| `$ingredients = Get-Content app/src/main/assets/ingredients.json -Raw -Encoding UTF8 \| ConvertFrom-Json; $lexicon = Get-Content app/src/main/assets/ingredient_aliases_multilingual.json -Raw -Encoding UTF8 \| ConvertFrom-Json; "concepts=$($ingredients.Count) mappings=$($lexicon.mappings.Count) aliasGroups=$($lexicon.aliases.Count)"` | `concepts=479 mappings=1996 aliasGroups=1668`. |
| `adb shell dumpsys battery` ; `adb shell dumpsys thermalservice` | Avant et après : valeurs détaillées ci-dessus. |
| `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; $env:Path = "$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.Phase4MatcherProfileInstrumentedTest" --console=plain *> app/build/pre_phase5_gradle.log; exit $LASTEXITCODE` | **Un seul lancement**, code de sortie 0, `BUILD SUCCESSFUL in 7m 53s`. |
| `Get-Content app/build/pre_phase5_gradle.log -Tail 20` ; `Select-String -Path $p -Pattern "PHASE4_ANDROID_(PROFILE\|MATCHER_STAGES\|TIMING\|MEMORY)"` (avec `$p` égal au logcat indiqué ci-dessus) ; `Get-Content $p -TotalCount 25` (avec `$p` égal au JUnit XML indiqué ci-dessus) | Mesures copiées ci-dessus ; JUnit 1/1 réussi. |
| `git status --short --branch` ; `git diff --check` | Arbre encore propre avant ce rapport ; aucune erreur de diff. |

Autres inspections en lecture seule effectuées : `rg` sur les classes `IngredientKnowledge`, `IngredientMatcher`, `IngredientTreeParser`, les assets et les rapports de revalidation ; `Get-Content` ciblé sur ces sources et rapports ; `Get-ChildItem` sur le JBR et les artefacts Android ; `Get-Date` pour les horaires. Aucune commande Python, importeur, générateur, mutation testing ou suite instrumentée complète n'a été lancée.
