# Simpleperf ciblé — UI étiquette réelle post-import — 8 octobre 2026

## Verdict

**Capture non exploitable pour identifier des fonctions métier coûteuses.** Le test a émis les marqueurs avant et après l’unique clic, et son attente a trouvé un nœud de verdict dans l’arbre Compose. Cependant, l’assertion que ce nœud soit affiché à l’écran a échoué, donc aucun marqueur `RESULT_DISPLAYED` n’existe. Le profil contient 5 479 échantillons, mais seulement cinq dans les deux threads `DefaultDispatch`, tous sur `CoroutineScheduler.Worker.park/tryPark/run`; aucune fonction métier du moteur ne ressort. Selon le protocole demandé, aucun nouvel essai n’a été lancé.

## Révision et appareil

- Commit/worktree : `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`, `.benchmark-worktrees/post-meat-3ec32d3`.
- Appareil : Nokia G42 5G, `AQ5003H044Q32600084`, Android 15, ADB `device`.
- Cible : `com.example.isitvegan`, versionName `0.6.13.7`, versionCode 59, debug/debuggable.
- APK de l’application laissée en place : SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`; aucune reconstruction ni réinstallation de cette APK. Vérifiée après le test avec `pm path` et `dumpsys package`.
- APK runner androidTest : SHA-256 `15003D9D73F8C3FF02688B170D72D579948E386751F25DAA8B4E6F92FBB7DF9E`; installée séparément pour l’instrumentation.
- Simpleperf appareil : `1.build.00WW_3_25H`, `cpu-clock` disponible; app debuggable; callgraph DWARF via `-g`.
- APK de test inspectée : aucun `5087.jpg`.

## Test et exécution

Test ajouté uniquement dans le worktree post-import : `app/src/androidTest/java/com/example/isitvegan/RealLabelAnalysisUiCaptureTest.kt`, méthode `oneEditedRealLabelAnalysisClickEmitsCaptureMarkers`. Il reprend le parcours Compose OCR éditable, insère le bloc anglais corrigé du benchmark, ferme le clavier, attend un bouton activé, puis exécute exactement un `performClick()`. Il journalise `BEFORE_CLICK`, `AFTER_CLICK` et `RESULT_DISPLAYED`.

La première assertion de visibilité échouait lorsque le résultat était hors écran. Le test source a ensuite été ajusté pour faire `performScrollTo()` avant `assertIsDisplayed()`; cette version corrigée compile localement mais **n’a pas été exécutée sur l’appareil**. L’APK runner profilée correspond à la source avant ce seul ajustement.

Commandes réellement exécutées :

```powershell
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\gradlew.bat :app:compileDebugAndroidTestKotlin --console=plain
.\gradlew.bat :app:assembleDebugAndroidTest --console=plain
adb -s AQ5003H044Q32600084 install -r <chemin-absolu-vers-app-debug-androidTest.apk>
adb -s AQ5003H044Q32600084 shell simpleperf record --app com.example.isitvegan -o /data/local/tmp/simpleperf-targeted-ui-20261008-211039.data -e cpu-clock -f 4000 -g --duration 90
adb -s AQ5003H044Q32600084 shell am instrument -w -e class com.example.isitvegan.RealLabelAnalysisUiCaptureTest#oneEditedRealLabelAnalysisClickEmitsCaptureMarkers com.example.isitvegan.test/androidx.test.runner.AndroidJUnitRunner
```

La compilation des sources de test, l’assemblage androidTest et la recompilation après correction ont réussi. Le test instrumenté a été exécuté seul, une fois; il a échoué à `assertIsDisplayed` (un échec JUnit). Le runner `am instrument` renvoie `0` même si sa sortie liste `FAILURES!!!`, donc ce code retour n’est pas un test réussi. Aucune suite, test de régression connu ou benchmark moteur n’a été lancé.

Les marqueurs conservés dans `analysis-markers.txt` : `BEFORE_CLICK` à `21:13:38.528`, `AFTER_CLICK` à `21:13:38.594`. La requête de présence du verdict dans l’arbre Compose a passé, puis `assertIsDisplayed()` a échoué parce que la cible n’était pas visible dans le viewport. Le marqueur final est absent. La sortie instrumentée intégrale est `instrumentation-output.txt`.

## Profil CPU

Commande de capture bornée à 90 s; Simpleperf s’est arrêtée après `16.9393 s` avec `5 479` échantillons et `0` perdus. `Event count` rapporté : `1 369 750 000`. Le processus et le test ont été lancés dans l’ordre capture puis instrumentation; les marqueurs de clic sont dans la période d’enregistrement. La capture ne comporte toutefois pas de marqueur de résultat validé.

Répartition des échantillons : MainActivity 3 159 (57,66 %), RenderThread 1 432 (26,14 %), DefaultExecutor 330 (6,02 %), ConscryptStatsL 278 (5,07 %), Profile Saver 150 (2,74 %); `DefaultDispatch` TID 14374 3 (0,05 %) et TID 14373 2 (0,04 %). Les cinq samples DefaultDispatch se résolvent sur `CoroutineScheduler.Worker.park`, `tryPark` et `run`, pas sur le parser ni le matcher.

Top self : `artQuickToInterpreterBridge` 3,10 %, `art::StackVisitor::WalkStack` 1,72 %, `art::interpreter::ExecuteSwitchImplCpp` 1,51 %, symbole kernel non résolu 1,42 %, `artQuickGenericJniTrampoline` 1,28 %. Les piles inclusives sont dominées par ART/interpréteur; aucune frame `VeganAnalyzer`, `IngredientTreeParser`, `IngredientMatcher`, `LabelSectionExtractor`, `DiagnosticReport` ou fonction app-owned équivalente n’apparaît dans les rapports. Les percentages sont des proportions d’échantillons, non des durées CPU exactes. Un avertissement indique que les adresses de symboles kernel sont restreintes.

Le build debug expose des symboles de bibliothèques JIT/Compose et de coroutines, mais le profil ici reste dominé par ART; les frames métier nécessaires ne sont pas attribuables. Simpleperf mesure le CPU échantillonné, pas les attentes ni le temps mural.

## Conditions et artefacts

Avant/après : batterie 100 %, AC alimenté, USB non alimenté, température batterie 23,8 °C puis 24,1 °C; Thermal Status 0 avant/après. Pas d’image, pas de ML Kit, aucune interaction manuelle, aucune modification de production.

Dossier d’artefacts du worktree : `app/build/benchmark-artifacts/simpleperf-targeted-20261008-211039/`.

- `perf.data` — brut 5 987 976 octets, SHA-256 `46000AF3B52EF4DB69AAF1644E0764B72604F327E24A9505A7CA2B1FB6D4A5AC`.
- `simpleperf-self.txt`, `simpleperf-children.txt`, `thread-samples.txt`, `defaultdispatch-callgraph.txt`, `hotspots-helper.log`.
- `instrumentation-output.txt`, `analysis-markers.txt`, commandes, préflight, logs Gradle, batterie/thermique.

Le helper `simpleperf_hotspots.sh` de la skill Android Performance a produit `self`/`children` via Simpleperf côté appareil. Les sorties de cette tentative confirment que le worker n’a pas été capturé en exécution utile; aucune nouvelle capture n’a été faite.

## Conclusion et suite

Cette tentative ne répond pas à « quelles fonctions métier consomment le plus de CPU ? ». Elle ne contredit pas la mesure Perfetto antérieure; les profils ne sont pas additionnés ni numériquement combinés. Le plus petit prochain essai est d’exécuter le test corrigé qui fait défiler et valide visiblement le verdict, avec Simpleperf démarré avant le runner et les trois marqueurs exigés. Si les frames Kotlin/Java app-owned restent absentes, utiliser le profileur CPU géré d’Android Studio (mode échantillonnage Kotlin/Java ou méthode) : il est plus adapté pour associer ART/JIT aux méthodes gérées. Cette autre collecte n’a pas été lancée.

## Tentatives synchronisées — 8 octobre 2026 — aucune capture CPU exploitable

### Écart de processus confirmé

Les artefacts Simpleperf antérieurs prouvent que le profil précédent a échantillonné le PID `14231`, alors que les marqueurs du test instrumenté portaient le PID `23703`. Lors de la première orchestration synchronisée, le PID package avant le runner était `24218`, puis le marqueur `READY` annonçait `24863`; `pidof com.example.isitvegan` ne montrait alors que `24863`. Le changement de PID est donc observé. La cause exacte de l’arrêt antérieur à 16,94 s n’est pas prouvée, même si le remplacement du processus auquel `--app` était attaché est une hypothèse cohérente.

### Première tentative synchronisée : attachement PID refusé

La compilation et l’assemblage du runner androidTest ont réussi; seul le runner a été installé, et le hash de l’APK application est resté `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`. Le test a émis `READY` pour le PID `24863` avec un run ID unique. La commande `simpleperf record -p 24863 ... --start_profiling_fd 1` a échoué immédiatement : `failed to open perf event file for event_type cpu-clock: Permission denied`. Aucun accusé `STARTED`, aucun signal de clic, aucun marqueur `BEFORE_CLICK` ou `RESULT_DISPLAYED`, et aucun `perf.data`. Le test a reçu le signal d’abandon propre à ce run et a échoué explicitement avant le clic. La sortie du shell a ensuite signalé que le PID Simpleperf supposé n’existait pas; c’était une conséquence de l’échec de permission.

### Seconde orchestration : marqueur READY périmé

Une stratégie distincte utilisant `--app` après READY était préparée, mais le poller a sélectionné un ancien marqueur READY encore présent dans logcat (`run=d988ec…`, PID `24863`). À cet instant `pidof` annonçait le PID courant `25187`, donc le contrôle a refusé de démarrer Simpleperf et n’a pas envoyé le signal de clic. Le nouveau test a ensuite émis son propre READY (`run=d2e738…`, PID `25315`); le test a été libéré par son signal d’abandon unique, avant le clic. Il n’y a pas eu de commande de capture `--app` ni de fichier perf dans cette seconde orchestration.

Le script `collect-synchronized-simpleperf-appmode.ps1` a depuis été corrigé pour prendre un snapshot des run IDs présents avant de lancer le runner et ignorer les marqueurs READY antérieurs. Cette correction a seulement été vérifiée syntaxiquement; elle n’a pas été exécutée sur le téléphone. Aucune troisième tentative n’a été démarrée.

### Résultat et limites

Les deux exécutions du test instrumenté ont échoué volontairement au garde d’attente du profiler; aucun clic d’analyse n’a été effectué. Aucune valeur de sample, fonction, percentage ou pile Simpleperf ne peut donc être rapportée pour ces runs. Les artefacts sous `app/build/benchmark-artifacts/simpleperf-synchronized-20261008-213407/` conservent les commandes, script, logs, READY/PID, sorties JUnit, erreurs d’attachement et signaux d’abandon. Aucun Simpleperf n’est resté actif. L’APK application version `0.6.13.7` n’a pas été reconstruite ni réinstallée; seul le runner androidTest a été réinstallé. Aucun code de production, donnée métier, image, test de régression connu, commit ou push n’a été touché.

**Conclusion :** le mismatch PID précédent est démontré; le profilage synchronisé n’a pas capturé l’analyse. La première méthode d’attachement PID est interdite par les permissions Android, et la seconde n’a pas dépassé le filtre READY périmé. Il faut une prochaine exécution avec le script `--app` corrigé, qui filtre les run IDs préexistants et attache après READY; cette nouvelle tentative n’est pas lancée ici.
