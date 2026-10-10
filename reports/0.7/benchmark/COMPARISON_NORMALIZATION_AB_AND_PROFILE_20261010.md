# Comparaison Android A/B de l’optimisation de normalisation — Nokia G42 — 10 octobre 2026

## Résultat

Les deux passages A puis les deux passages B ont réussi avec le même test instrumenté. Les temps warm sont cohérents à l’intérieur de chaque variante. Sur l’analyse propre, les médianes des deux passages A sont de 10,652 s et 10,579 s; celles de B sont de 1,071 s et 1,075 s. Le parsing isolé passe de 5,657/5,623 s à 0,124/0,124 s. Les écarts sont très supérieurs à la dispersion observée, et le couple inverse B2/A2 reproduit le résultat sous des températures de départ proches. **Le gain Android est établi pour ce harness et ce texte : environ 89,9 % de réduction pour l’analyse complète et 97,8 % pour le parsing isolé.** Il s’agit d’une comparaison du patch à deux fichiers, pas d’une attribution à une seule des deux optimisations.

Le profil Simpleperf B n’a pas été lancé. Le script validé contient un hash d’APK A codé en dur et s’arrêterait au précontrôle avec l’APK B. Comme le script et le harness ne devaient pas être modifiés et que l’instruction interdit une orchestration improvisée, aucune méthode CPU valide n’est disponible dans cette passe. On ne peut donc pas encore nommer le hotspot fonctionnel suivant.

## Révisions et parité

- A, référence : `.benchmark-worktrees/normalize-origin-reference-3ec32d3`, HEAD détaché `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`.
- B, optimisée : `.benchmark-worktrees/normalize-origin-3ec32d3`, même HEAD détaché avec uniquement le patch de production préexistant dans `TextNormalizer.kt` et `OriginQualifierRules.kt` (21 ajouts, 9 suppressions). L’ajout des trois regex immuables et la sortie des normalisations invariantes des boucles sont décrits dans `OPTIMIZATION_NORMALIZATION_ORIGIN_20261009.md`.
- Dépôt principal : `master`, HEAD `528a9dbf26a917a0d008d0f05f4b429129d1d49a`. Les worktrees et changements préexistants ont été préservés. Aucun code source, asset, test, protocole ou script n’a été modifié dans cette passe.
- Test A/B identique octet par octet : `RealLabelMultilingualBenchmarkInstrumentedTest.kt`, SHA-256 `065A1C1DC3E9B5644315BF166923E03F5D1E4DBD26B2AFD8B4ADA1AC79D57B1F`.
- SHA-256 des assets identiques dans A et B : `ingredients.json` `78DCCB4B02DBFC14921FCF40C62228FBF5DCDB8294B63A1C2DCDAE05FD1A3825`; `ingredient_aliases_multilingual.json` `3952C31957690A2D998DC2A2BAE740E01D434824FE31EA01FD555D1AA52992E2`; `origin_qualifier_rules.json` `77E6E6F436F11666F659EA4BD70F45B02CEBE89AE940B9077567891286F9D40E`.
- Les APK app A/B ont le package `com.example.isitvegan`, version `0.6.13.7`, versionCode 59 et certificat identique `b2cf0b744229e5fd5d6f0c49e53c96ce8fc878af20190c71618f8d349f590230`. SHA-256 A `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`; B `415F23B484B9F208497636C7E18B66C91435E571936C98D0518FDBCC91C1C0CD`.
- Les APK androidTest sont identiques A/B, package `com.example.isitvegan.test`, certificat identique, SHA-256 `A193F93005C34F7C8391AFAA3E2637D268CE92A29FB229BD88F253921BD9191A`. Le manifest fusionné déclare `com.example.isitvegan.test/androidx.test.runner.AndroidJUnitRunner` ciblant `com.example.isitvegan`. Inspection ZIP/aapt : `5087.jpg` est absent de l’APK de test.
- Le test vise exactement `RealLabelMultilingualBenchmarkInstrumentedTest#profilesRealLabelTextAndDeterministicOcrVariants`. Il mesure cinq chaînes (bloc anglais corrigé, `Glucose-fructose syna`, `WHEAT lour`, `CTitric aid`, `Favouring`), avec cinq échauffements et dix échantillons warm, puis parsing et extraction. Les assertions sont exécutées hors intervalle chronométré. Aucune image et aucun ML Kit ne sont utilisés.

## Appareil, ordre et conditions

ADB a listé uniquement `AQ5003H044Q32600084` à l’état `device`; `getprop` confirme Nokia G42 5G sous Android 15 (`Nokia/Shadow_00EEA/SDW:15/AQ3A.241114.001/00WW_3_25H:user/release-keys`). Batterie à 100 % et USB alimenté/AC non alimenté pendant les passages; statut thermique 0 à chaque relevé. Température peau ci-dessous = lecture courante du HAL `dumpsys thermalservice`; la lecture batterie de `dumpsys battery` est également indiquée.

| Passage | Ordre | Batterie début → fin | Peau début → fin | Notes |
|---|---|---:|---:|---|
| A1 | A | 21,8 → 26,6 °C | 22,678 → 29,862 °C | 100 %, USB vrai, AC faux |
| B1 | A puis B | 24,0 → 24,6 °C | 24,687 → 26,743 °C | B a commencé 2,2 °C plus chaud en batterie que A1 |
| B2 | B puis A | 24,6 → 25,1 °C | 26,238 → 27,477 °C | 100 %, USB vrai, AC faux |
| A2 | B puis A | 25,1 → 27,8 °C | 27,201 → 30,539 °C | Départ à 0,5 °C batterie et 0,963 °C peau de B2 |

Les temps A1/A2 concordent à moins de 0,7 % pour les opérations principales; B1/B2 concordent également étroitement. Le couple B2/A2 a des départs thermiques proches et reproduit un écart d’environ 10× en analyse. Le couple A1/B1 a une différence initiale de 2,2 °C; il est moins contrôlé thermiquement. Aucun throttling signalé (Thermal Status 0). Aucun deuxième couple supplémentaire n’a été lancé.

## Compilation, installation et tests réellement exécutés

Dans chacun des worktrees A et B :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Les deux assemblages ont réussi (tâches `UP-TO-DATE`; A 26 s, B 2 s). APK app et test ont été contrôlées avec `aapt dump badging`, `apksigner verify --print-certs`, SHA-256 et inspection ZIP. Les compilations ont eu lieu sur le Lenovo et ne sont pas des mesures Android.

Ordre exécuté : A1 → B1 → B2 → A2. Installation explicite de chaque APK app puis de l’APK androidTest avec `adb -s AQ5003H044Q32600084 install -r`. Aucun `connectedDebugAndroidTest`, `adb uninstall`, `pm uninstall`, `pm clear` ou nettoyage équivalent n’a été lancé. Après chaque installation, `pm path` et `sha256sum` ont vérifié le package APK app installé. Le test a été exécuté directement ainsi, avec la classe/méthode ciblée :

```powershell
adb -s AQ5003H044Q32600084 shell am instrument -w -r `
  -e class com.example.isitvegan.RealLabelMultilingualBenchmarkInstrumentedTest#profilesRealLabelTextAndDeterministicOcrVariants `
  com.example.isitvegan.test/androidx.test.runner.AndroidJUnitRunner
```

Résultat JUnit AndroidJUnitRunner : A1 `Time: 981,622`, `OK (1 test)`; B1 `101,581`, `OK (1 test)`; B2 `101,356`, `OK (1 test)`; A2 `975,084`, `OK (1 test)`. Le runner direct n’écrit pas de XML JUnit séparé; ses sorties complètes et les logs filtrés contenant les marqueurs de corpus et de timing sont conservés pour chaque passage.

## Mesures warm

Toutes les valeurs sont en secondes. `A1/B1` et `A2/B2` sont les deux comparaisons appariées; réduction calculée à partir des médianes de chaque paire. Les bruts en nanosecondes, min, médiane basse, médiane arithmétique des deux valeurs centrales et max sont dans `comparison-summary.json`.

| Scénario / opération | A1 médiane | B1 médiane | A2 médiane | B2 médiane | Réduction moyenne appariée |
|---|---:|---:|---:|---:|---:|
| Analyse complète, texte corrigé | 10,651817 | 1,071226 | 10,578593 | 1,075168 | 89,89 % |
| `Glucose-fructose syna` | 10,663763 | 1,087321 | 10,593079 | 1,075664 | 89,82 % |
| `WHEAT lour` | 10,647929 | 1,064424 | 10,574535 | 1,067374 | 89,95 % |
| `CTitric aid` | 10,692496 | 1,076787 | 10,612638 | 1,072049 | 89,91 % |
| `Favouring` | 10,617639 | 1,069513 | 10,556366 | 1,064881 | 89,92 % |
| Chargement connaissance répété (`knowledge-from-json`) | 1,059985 | 0,382473 | 1,054125 | 0,376315 | 64,11 % |
| Construction répétée du matcher | 1,137913 | 0,319311 | 1,133690 | 0,325196 | 71,63 % |
| Parsing seul, texte propre | 5,657424 | 0,123615 | 5,622978 | 0,123876 | 97,81 % |
| Extraction de section | 0,050802 | 0,049207 | 0,049075 | 0,049042 | 1,63 % |

Chaque ligne warm contient dix valeurs après cinq échauffements. Les scénarios complets A sont dans une plage de médianes 10,56–10,69 s; ceux de B dans 1,06–1,09 s. Le parsing isolé est le changement relatif le plus important, tandis que l’extraction de section reste inchangée à l’échelle de sa dispersion.

Les premières invocations (après lecture/construction des données dans le processus instrumenté; pas un démarrage à froid Android) : analyse propre A1/A2 11,140/11,018 s, B1/B2 1,445/1,456 s; parsing A1/A2 5,665/5,613 s, B1/B2 0,122/0,128 s; construction matcher A1/A2 1,304/1,231 s, B1/B2 0,463/0,454 s. Lecture froide de l’asset : A1/A2 0,081/0,059 s, B1/B2 0,081/0,044 s. Le nombre de concepts et mappings est identique dans chaque test : 488 concepts, 2 093 mappings, 1 707 groupes d’alias; assets 233 391 / 997 318 / 14 906 octets.

## Simpleperf et prochain hotspot

Aucune capture Simpleperf n’a été lancée. Le script existant `post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-reviewed-20261009-105911/collect-synchronized-simpleperf-appmode.ps1` valide d’abord un hash applicatif codé en dur `6EB2…` (APK A), tandis que B installée est `415F…`. Il attend également le test UI `RealLabelAnalysisUiCaptureTest#oneEditedRealLabelAnalysisClickEmitsCaptureMarkers`. Le script rejette donc B au contrôle de hash avant READY; le contourner exigerait de modifier le script ou d’improviser une orchestration, tous deux exclus ici. Aucun `perf.data`, rapport self/children ou profil B n’existe dans ce passage.

Le précédent profil d’octobre identifiait `TextNormalizer.normalize` et `OriginQualifierRuleSet.extractAttached` avant ce patch; il ne peut pas classer les fonctions dans B après une réduction aussi forte du temps moteur. Les phases Android mesurées font ressortir `IngredientKnowledge.fromJson` (~0,379 s warm) et la construction `IngredientMatcher` (~0,322 s warm) comme phases encore notables, mais ce sont des durées de phase, pas une preuve CPU ou des fonctions dominantes dans l’analyse interactive. Le prochain candidat est donc une capture synchronisée valide sur B, après adaptation autorisée du précontrôle de hash; il faut ensuite classer les piles du worker avant de proposer une optimisation. Aucun changement d’optimisation supplémentaire n’est recommandé sur la seule base de ces durées.

## Artefacts principaux

Les logs, installations, sorties AndroidJUnitRunner, relevés thermiques et hashes sont conservés dans :

- A : `.benchmark-worktrees/normalize-origin-reference-3ec32d3/app/build/benchmark-artifacts/normalization-ab-direct-20261010/`.
- B : `.benchmark-worktrees/normalize-origin-3ec32d3/app/build/benchmark-artifacts/normalization-ab-direct-20261010/`.
- Résumé des quatre runs avec toutes les valeurs brutes : A `comparison-summary.json`, SHA-256 `047C3F936893E8DC2A7FB4A7C6F7B4FDC2572B556F9E64888648F10F4ECED266`.
- Manifest SHA-256 des artefacts A/B : A `artifact-sha256.txt` des 82 artefacts A/B, SHA-256 `9EBA2137FFB7B2ADF1B1909BE6BEAD87D4BA2ADBC11AC8E89DF762BAEA3738B4`.
- Précontrôle Simpleperf B bloqué : B `simpleperf-preflight-block.txt`.
- Fichiers runner : `A-instrumentation.txt`, `A2-instrumentation.txt`, `B-instrumentation.txt`, `B2-instrumentation.txt`; logcats correspondants `*-logcat-live.txt`; builds `A-build.log` et `B-build.log`.

L’APK B a été réinstallée après A2 avec `adb install -r`; `pm path`, versionCode/versionName et SHA-256 installé ont été revérifiés. B est laissée installée pour Sébastien : `com.example.isitvegan`, `0.6.13.7` (59), SHA-256 `415F23B484B9F208497636C7E18B66C91435E571936C98D0518FDBCC91C1C0CD`, build `.benchmark-worktrees/normalize-origin-3ec32d3/app/build/outputs/apk/debug/app-debug.apk`.

Aucun changement métier, asset, harness ou script n’a été effectué; aucun test de régression connu, profil ou suite complète n’a été exécuté; aucun commit ou push n’a été créé.
