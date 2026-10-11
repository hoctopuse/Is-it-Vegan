# Comparaison Android de l’optimisation de normalisation — résultats partiels, 10 octobre 2026

## Conclusion

Le passage A (code de référence) a réussi sur le Nokia : le test instrumenté a passé ses assertions et a produit toutes les séries prévues. **Aucune comparaison A/B n’est disponible** : après le test A, le package applicatif a disparu de `pm path`; les événements du gestionnaire de paquets montrent aussi des appels `installer_clear_app_data` pendant les transactions d’installation. La consigne exigeait un remplacement sans effacement de données. Je me suis arrêté avant d’installer ou d’exécuter B. B n’est donc pas installée sur le Nokia.

Les temps A sont un point de données seulement; ils ne disent rien du gain du patch. Le travail ne permet pas encore de décider si l’optimisation mérite d’être conservée pour ses performances.

## Commits, worktrees et changements

- A, référence : `.benchmark-worktrees/normalize-origin-reference-3ec32d3`, HEAD détaché `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`.
- B, optimisée : `.benchmark-worktrees/normalize-origin-3ec32d3`, même HEAD avec le patch non commité décrit dans `OPTIMIZATION_NORMALIZATION_ORIGIN_20261009.md`.
- Dépôt principal initial et final : `master`, HEAD `528a9dbf26a917a0d008d0f05f4b429129d1d49a`; pas de diff suivi. `.benchmark-worktrees/` est non suivi et contient les worktrees antérieurs. Ce rapport est le seul nouveau rapport ajouté hors de ce répertoire.
- Le diff de production B contre A se limite à `TextNormalizer.kt` et `OriginQualifierRules.kt` (21 ajouts, 9 suppressions). Aucune modification de production n’a été faite pendant la comparaison.
- Le même fichier instrumenté a été copié dans A et B depuis le worktree post-import, sans adaptation. SHA-256 des trois copies : `065A1C1DC3E9B5644315BF166923E03F5D1E4DBD26B2AFD8B4ADA1AC79D57B1F`.
- Les trois assets moteur concordent entre A et B : `ingredients.json` `78DCCB4B02DBFC14921FCF40C62228FBF5DCDB8294B63A1C2DCDAE05FD1A3825`; `ingredient_aliases_multilingual.json` `3952C31957690A2D998DC2A2BAE740E01D434824FE31EA01FD555D1AA52992E2`; `origin_qualifier_rules.json` `77E6E6F436F11666F659EA4BD70F45B02CEBE89AE940B9077567891286F9D40E`.

## Appareil, APK et conditions

ADB a identifié le seul appareil `AQ5003H044Q32600084` comme `device`, Nokia G42 5G, Android 15. Avant A, batterie 100 %, USB alimenté, AC non alimenté; `dumpsys battery` indiquait 21,8 °C et Thermal Status était 0. Après A : batterie 100 %, USB alimenté, AC non alimenté, 26,6 °C et Thermal Status 0. `dumpsys thermalservice` a rapporté 28,3 °C pour sa température batterie mise en cache, inchangée entre les relevés; les deux sources ne donnent donc pas la même température. Il n’y a pas eu de second passage dans ces conditions.

Les deux APK applicatives debug ont le package `com.example.isitvegan`, versionName `0.6.13.7`, versionCode 59 et le même certificat SHA-256 `b2cf0b744229e5fd5d6f0c49e53c96ce8fc878af20190c71618f8d349f590230`. A : SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`. B : `415F23B484B9F208497636C7E18B66C91435E571936C98D0518FDBCC91C1C0CD`. Le runner `com.example.isitvegan.test`, signé par le même certificat, est identique : SHA-256 A/B `A193F93005C34F7C8391AFAA3E2637D268CE92A29FB229BD88F253921BD9191A`.

Avant le test, l’APK installée avait exactement le hash A. Après la tâche connectée, `pm path com.example.isitvegan` et `dumpsys package` ne trouvaient plus le package. Le journal d’événements contient deux appels `installer_clear_app_data` associés aux transactions d’installation (vers 14:29:32 et 14:30:01), puis l’arrêt du processus après la fin de l’instrumentation. Aucun `adb uninstall`, `pm uninstall` ou `pm clear` n’a été lancé manuellement. Je ne peux cependant pas garantir que les données de l’application ont été préservées; ce contrôle de garde-fou a échoué et a interrompu l’A/B.

## Passage A exécuté

Build ciblé, dans le worktree A :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Cette commande a aussi réussi dans B. Aucun APK de production n’a été construit par la tâche instrumentée elle-même; les deux tâches d’assemblage ciblées ont produit les variantes A et B. Les APK ont été vérifiées avec `aapt dump badging` et `apksigner verify --print-certs` avant les installations. L’APK A a été mise à jour avec `adb -s AQ5003H044Q32600084 install -r <A/app-debug.apk>`; la commande a répondu `Success` et le hash installé était A. Le runner connecté a ensuite exécuté uniquement :

```powershell
$env:ANDROID_SERIAL = 'AQ5003H044Q32600084'
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.RealLabelMultilingualBenchmarkInstrumentedTest#profilesRealLabelTextAndDeterministicOcrVariants' --console=plain
```

Résultat Gradle : `BUILD SUCCESSFUL in 16m 38s`. JUnit XML : 1 test, 0 échec, 0 erreur, durée du test 967,816 s. Toutes les assertions du harness ont passé, y compris le texte et la section anglaise, l’arbre d’ingrédients et les mappings attendus. Le passage utilise les cinq scénarios texte, sans ML Kit.

Le harness mesure `SystemClock.elapsedRealtimeNanos()`. « Cold » désigne ici la première opération du scénario dans le processus instrumenté, après le chargement de la connaissance; ce n’est pas un démarrage à froid d’Android. Les séries warm ont cinq échauffements puis dix mesures. La médiane basse est l’élément trié d’index 4; `median` est la moyenne des deux valeurs centrales, comme dans le code du harness. Valeurs en millisecondes :

| Scénario A | Première invocation | Min warm | Médiane basse | Médiane | Max warm |
|---|---:|---:|---:|---:|---:|
| Chargement assets | 72,934 | — | — | — | — |
| Chargement connaissance | 1 490,574 | — | — | — | — |
| Matcher | 1 278,834 | — | — | — | — |
| Construction matcher warm (10) | — | 1 116,075 | 1 123,097 | 1 123,106 | 1 134,824 |
| Analyse propre `clean-flavouring` | 11 026,173 | 10 493,871 | 10 511,903 | 10 512,276 | 10 542,589 |
| `Glucose-fructose syna` | 10 522,056 | 10 502,056 | 10 522,847 | 10 523,862 | 10 545,585 |
| `WHEAT lour` | 10 500,288 | 10 485,628 | 10 512,598 | 10 512,829 | 10 569,624 |
| `CTitric aid` | 10 546,674 | 10 532,465 | 10 543,053 | 10 544,504 | 10 556,849 |
| `Favouring` | 10 490,860 | 10 474,426 | 10 488,422 | 10 488,530 | 10 508,440 |
| Parsing seul, texte propre | 5 580,396 | 5 571,639 | 5 586,193 | 5 586,249 | 5 591,499 |
| Extraction de section propre | 48,359 | 48,526 | 48,936 | 49,053 | 49,606 |

Les valeurs brutes, notamment les dix valeurs de chaque série, sont dans `A1-metrics.json` et dans les lignes `REAL_LABEL_TIMING` de `A1-logcat.txt`. Les trois premières lignes de logcat ont été conservées pendant le passage dans `A1-initial-live-logcat-excerpt.txt`; elles avaient quitté le tampon circulaire avant la collecte finale du logcat. Aucune variation relative A/B ne peut être calculée.

## Artefacts

Dans `.benchmark-worktrees/normalize-origin-reference-3ec32d3/app/build/benchmark-artifacts/normalization-ab-20261010/` :

- builds A/B : `build.log` (BUILD SUCCESSFUL, 2m49s) et B `../normalize-origin-3ec32d3/app/build/benchmark-artifacts/normalization-ab-20261010/build.log` (BUILD SUCCESSFUL, 41s);
- passage A : `A1-connected-test.log`, `A1-JUnit.xml`, `A1-logcat.txt`, `A1-initial-live-logcat-excerpt.txt`, `A1-metrics.json`;
- conditions : `A1-before-battery.txt`, `A1-before-thermal.txt`, `A1-after-battery.txt`, `A1-after-thermal.txt`;
- identité et incident : `installed-before.apk`, `A1-installed-app-sha256.txt`, `A1-package-events.txt`, `A1-package-state-after.txt`, `pull-installed.log`.

SHA-256 principaux : `A1-connected-test.log` `F844729426CB2FD154BF885CB67F1C99770EE95268290075BA9E0D28ACD382F4`; `A1-JUnit.xml` `68E43CC550582A9F53C268686FF6ECF47E662AAF75507748937311C21EA93DA5`; `A1-logcat.txt` `F1624242008871DD7FD783BC76ACD2E72E6093F184A8EBCAF238DA9832F7F6F6`; `A1-metrics.json` `6F70B076A712AE8DBB0E89EDC22A82DA5DD09BD684FC365E8BE898646331BDB1`; `A1-package-events.txt` `D0D92739A8AC219B06AAE8DBC75BE7040EF2F37FFC514CDF65DDBF8CD3270653`; APK A copiée avant installation `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`.

## Suite de l’expérience

B est compilée sous `.benchmark-worktrees/normalize-origin-3ec32d3/app/build/outputs/apk/debug/app-debug.apk`, mais **elle n’est pas installée**. Le package applicatif est actuellement absent du Nokia. Les artefacts restent disponibles dans les worktrees A, B et post-import. Pour terminer une comparaison, il faut d’abord rétablir explicitement un mode d’installation/runner qui ne retire pas le package et dont la conservation des données est vérifiée; puis mesurer A et B avec les mêmes cinq échauffements et dix mesures. Le test A actuel ne doit pas être comparé à une exécution B faite par un chemin de lancement différent sans documenter cette différence. La mesure du 2 octobre (commit `9d98825…`, autre harness) n’est pas une référence A/B.

Aucun test JVM de profilage, suite complète, Simpleperf ou Perfetto n’a été lancé. Aucun commit ou push n’a été créé. Aucun fichier utilisateur sur l’appareil n’a été volontairement supprimé.
