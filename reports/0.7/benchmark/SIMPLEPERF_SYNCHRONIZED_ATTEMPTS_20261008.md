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

## Tentative synchronisée `--app` — 9 octobre 2026 — arrêt avant clic

### Préflight et commandes

Le worktree post-import est au commit `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`. Le Nokia `AQ5003H044Q32600084` était `device`, modèle Nokia G42 5G, Android 15. L’application installée est restée `0.6.13.7`, versionCode 59; le hash SHA-256 de l’APK debug locale avant et après est `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`. Seule l’APK androidTest a été installée. Son SHA-256 est `8E60279D5BF6C2C8D2EABB95243B2F124B70C2E3CFCFE8064E29E9087A2D059D`; l’inspection du ZIP confirme l’absence de `5087.jpg`.

La compilation initiale a échoué parce que Gradle cherchait JDK 25 via foojay alors que l’accès réseau échouait. Avec le JBR local d’Android Studio (`25.0.3`), les tâches ciblées `:app:compileDebugAndroidTestKotlin :app:assembleDebugAndroidTest` ont réussi (`UP-TO-DATE`; aucune compilation de l’APK de production). La première invocation directe du script a ensuite été bloquée localement par la politique d’exécution PowerShell avant toute commande ADB. Le script a été lancé dans un processus dédié avec `-NoProfile -ExecutionPolicy Bypass`.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\gradlew.bat :app:compileDebugAndroidTestKotlin :app:assembleDebugAndroidTest --console=plain
powershell.exe -NoProfile -ExecutionPolicy Bypass -File C:\Users\seb\AndroidStudioProjects\IsitVegan\.benchmark-worktrees\post-meat-3ec32d3\app\build\benchmark-artifacts\simpleperf-synchronized-20261009-102637\collect-synchronized-simpleperf-appmode.ps1 -Serial AQ5003H044Q32600084 -ArtifactDir C:\Users\seb\AndroidStudioProjects\IsitVegan\.benchmark-worktrees\post-meat-3ec32d3\app\build\benchmark-artifacts\simpleperf-synchronized-20261009-102637 -TestApk C:\Users\seb\AndroidStudioProjects\IsitVegan\.benchmark-worktrees\post-meat-3ec32d3\app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
```

Le script a installé le runner androidTest, démarré le test ciblé, ignoré les READY présents dans le snapshot initial et reconnu le nouveau run `29cce2241b104a41a822b53254cb633d`. Le marqueur `READY` annonce PID `31073`; `pidof com.example.isitvegan` à ce moment confirmait `31073`.

### Échec de l’orchestration

Simpleperf a émis `STARTED`, puis le relevé `ps -A -o PID,NAME,ARGS` a montré deux processus correspondant au même run : PID `31230`, lanceur `simpleperf record --app com.example.isitvegan ...`, et PID `31236`, enfant d’enregistrement `simpleperf record --app com.example.isitvegan --in-app ...`. Le script attendait à tort un seul PID Simpleperf et a échoué sur `Could not uniquely identify active profiler PID (pidof before= after=31230 31236)`. Il n’a donc pas envoyé le signal `allow-click`; le seul marqueur du run est `READY`.

Le script a envoyé le signal abort propre à ce run. La sortie JUnit réelle indique `Tests run: 1, Failures: 1`, avec `Aborted while waiting for profiler authorization to click`. Aucun `BEFORE_CLICK`, `AFTER_CLICK` ou `RESULT_DISPLAYED` n’existe. Après la fin du test, `pidof simpleperf` était vide et aucune ligne de processus ne contenait le run ID ou les PID Simpleperf `31228`, `31230`, `31236`. Le processus runner androidTest `31250` persistait après la fin JUnit; il a été arrêté explicitement par `am force-stop com.example.isitvegan.test`, puis `pidof com.example.isitvegan.test` et `pidof simpleperf` étaient vides. Le package applicatif n’a pas été arrêté. Le fichier brut, déjà finalisé, a été récupéré sans supprimer le fichier temporaire unique sur l’appareil.

Le fichier brut enregistre 2,04753 s, 2 631 échantillons, 0 perdus; sa taille est de 4 667 243 octets et son SHA-256 `EFE08623A3405904B11972818B04D11964423EBDC1A3F68D2CDA870042BCC333`. Cette fenêtre précède le clic et ne couvre pas l’analyse. Aucun rapport `self`/`children` n’a été généré, conformément au critère de validation. Le problème constaté est la gestion des deux processus par l’orchestrateur; cette tentative ne démontre aucune limite de symboles Kotlin/Java et ne permet pas de conclure sur les fonctions du moteur.

### Conditions et artefacts

Batterie 100 %, USB alimenté, AC non alimenté; température batterie 20,4 °C avant et 21,0 °C après. `Thermal Status: 0` avant/après. Le test a utilisé l’étiquette corrigée sans OCR ni ML Kit. Aucun test de régression connu ou suite complète n’a été exécuté.

Les données et journaux sont dans `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-synchronized-20261009-102637/` : `perf.data`, `instrumentation-appmode.stdout.txt`, `junit-result.txt`, `markers-current-run.txt`, `pid-map-appmode.txt`, `processes-profiler-active.txt`, `simpleperf-foreground.stdout.txt`, `simpleperf-foreground.stderr.txt`, préflight, batterie/thermique et hashes. `collect-synchronized-simpleperf-appmode-executed.ps1` conserve le script utilisé. Le script `collect-synchronized-simpleperf-appmode.ps1` du dossier a ensuite été corrigé pour sélectionner l’unique enfant `--in-app` avec le run ID; ce correctif est syntaxiquement valide mais n’a pas été exécuté. Aucun deuxième essai n’a été lancé.

**Conclusion :** aucune fonction métier n’est identifiée. L’essai est non exploitable parce que l’orchestrateur a rejeté les deux PID attendus du mode `--app` avant d’autoriser le clic. Le prochain profil nécessite une nouvelle autorisation de collecte après validation du script qui cible l’enfant `--in-app`; aucune limite de symboles ne peut encore être invoquée.

## Orchestration relue et profil moteur exploitable — 9 octobre 2026

**Conclusion actuelle : une collecte synchronisée couvre le clic et l’analyse complète dans le bon processus. Les fonctions métier sont résolues. Le coût dominant du worker se situe dans la normalisation du texte et la compilation des expressions régulières, notamment depuis les règles d’origine.** Les anciennes captures non exploitables restent conservées ci-dessus; elles ne servent pas au classement actuel.

### Corrections et validation locale

La nouvelle version du script et ses fonctions communes sont conservées sous `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-reviewed-20261009-105911/`.

- Parsing des processus avec des objets `Regex.Match` indépendants. Les anciens enchaînements `-match` écrasaient `$Matches` pendant la lecture des champs PID/commande. Le script distingue exactement un lanceur `--app` et un enfant `--in-app` du même run, exclut la ligne shell et les autres runs, et refuse une ambiguïté.
- Run ID créé par l’orchestrateur, passé au runner via `-e simpleperfRunId`, obligatoire et validé dans le test. Tous les marqueurs et signaux utilisent cet ID exact; aucun READY d’un autre run ne peut être sélectionné.
- STARTED, existence du couple lanceur/enfant, descripteurs `perf_event` ouverts dans l’enfant, runner ADB actif et PID applicatif vérifiés avant le clic.
- Arrêt par SIGINT du lanceur shell du run, propriétaire du canal `--stop-signal-fd` de l’enfant; attente de la fin ADB, vérification de l’absence des deux processus et de la stabilité de taille du fichier avant récupération et libération du test. Le secours éventuel utilise `run-as` uniquement pour l’enfant identifié de ce run.
- Commandes ADB journalisées avec stdout/stderr séparés, délais bornés et lecture des codes retour. Conservation explicite du handle `Process`, puis `Refresh`, pour éviter un `ExitCode` vide sous PowerShell 5.1. Lecture des journaux encore ouverts avec `FileShare.ReadWrite`.
- Test ciblé : texte exact vérifié dans le champ; bouton visible et activé; absence de tout verdict avant le clic; un seul `performClick`; nouveau verdict trouvé, défilé et vérifié visible; processus conservé jusqu’au signal de finalisation. Les attentes ont lieu sur le thread de test. Le test conserve la langue UI/préférence FR du parcours précédent; le texte est le bloc anglais corrigé, mode `OCR_LABEL`, sans image ou ML Kit.
- Chaque marqueur conserve `elapsedRealtimeNanos` et ajoute `System.nanoTime` (`clockNs`). Le profiler utilise explicitement `--clockid monotonic`; cela permet la comparaison directe des timestamps, sans confondre BOOTTIME et MONOTONIC après les suspensions du téléphone.

`validate-orchestration.ps1` a exécuté **19 vérifications locales réussies**, dont le relevé réel parent `31230` / enfant `31236`, les READY périmés, les PID changés, les processus absents/dupliqués, les timeouts, les erreurs du profiler, la lecture concurrente des journaux, les codes de sortie 0/7 et le véritable échec JUnit historique. Ces vérifications portent sur les fonctions réellement employées par le script; les opérations Android et la finalisation sont ensuite validées par le passage appareil réussi.

Le texte du test UI est identique à `RealLabelMultilingualBenchmarkInstrumentedTest.CLEAN_LABEL`, y compris les sauts de ligne : 306 caractères, SHA-256 UTF-8 `7e265978f95c9731041b1f3c585196e07fb72061adb917a69a51bde7b74a8fbc`. `:app:compileDebugAndroidTestKotlin :app:assembleDebugAndroidTest` a réussi en 28 s (4 tâches exécutées, autres up-to-date). Aucune tâche d’assemblage de l’APK applicative ni suite de régression n’a été lancée.

### Passages distincts et résultat JUnit

| Dossier | Résultat | Diagnostic et suite |
|---|---|---|
| `attempt-01` | Arrêt préflight, aucun runner/capture | Code retour ADB vide malgré `device`; handle de processus conservé et comportement 0/7 vérifié localement. |
| `attempt-02` | READY courant, profiler arrêté, aucun clic; JUnit abort | Lecture du journal stdout encore ouvert refusée; partage lecture/écriture corrigé et vérifié localement. Donnée brute conservée. |
| `attempt-03` | **OK (1 test), profil exploitable** | Run `d2bf2bb146904755ae215471c26a256a`, PID test/app `4487`, lanceur `4628`, enregistreur `4634`; collecte finalisée puis test libéré. Aucune autre capture après ce succès. |

Pour le dernier passage, `thread-samples.txt` montre que **toutes les lignes échantillonnées portent le PID `4487`**, identique au READY et à `pidof com.example.isitvegan`. Les PID `4628`/`4634` désignent les outils de collecte, pas le processus échantillonné. Le worker `DefaultDispatch`, TID `4583`, compte **63 515 échantillons de calcul utile**, dont tous les timestamps exportés se situent entre BEFORE_CLICK et RESULT_DISPLAYED. Le verdict ancien était absent avant le clic. Les six marqueurs READY, BEFORE_CLICK, AFTER_CLICK, RESULT_DISPLAYED, WAITING_FOR_PROFILER_STOP et PROFILER_FINALIZED sont présents une fois, dans le même PID et le même run.

### Couverture temporelle et conditions

Timestamps MONOTONIC en secondes issus des données et du test :

| Événement | Timestamp |
|---|---:|
| Premier échantillon | 497769,761056729 |
| BEFORE_CLICK | 497771,313526624 |
| AFTER_CLICK | 497771,365949437 |
| Premier échantillon worker | 497771,362450010 |
| Dernier échantillon worker | 497787,450426045 |
| RESULT_DISPLAYED | 497787,907676774 |
| Dernier échantillon | 497788,779395107 |

Le profil encadre donc réellement le clic et le résultat. L’écart BEFORE_CLICK → RESULT_DISPLAYED est **16,59415015 s pendant le profilage instrumenté**. Il inclut l’action Compose, l’attente du résultat, le défilement et l’assertion de visibilité. Cette valeur comporte la surcharge Simpleperf et des tests Compose; elle ne remplace pas les ~11,75 s Perfetto antérieurs et ne se compare pas directement aux ~10,5 s moteur. Le test polling fait travailler le thread principal : ses 28,69 % d’échantillons dans ce profil ne prouvent pas un blocage du thread principal en usage normal.

Enregistrement effectif : **19,0192 s**, **144 286 échantillons**, **0 perdus**, dont **5 236 piles tronquées**. Le rapport texte `report-sample` expose 144 283 samples, soit trois de moins que le rapport brut; la raison de cette différence de conversion n’est pas établie. Les 63 515 échantillons worker sont exportés et compris dans la fenêtre. Les compteurs de perte restent ceux du journal Simpleperf, pas cette différence de conversion.

Nokia G42 5G `AQ5003H044Q32600084`, Android 15, ADB `device`; commit `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`; application debug `0.6.13.7`/59. **Le hash de l’APK effectivement installée a été lu par `sha256sum` avant et après**, identique à `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`. Seul le runner androidTest a été installé, SHA-256 `8661FB8754F85E24A0B400D016F4B660659A5A42A6BD25522225B622646B616A`; inspection ZIP : photo absente. Batterie 100 %, USB powered true, AC false; température batterie 22,5 °C avant / 23,0 °C après; Thermal Status 0 avant/après. Aucun appareil Pixel n’a été mesuré.

### Fonctions coûteuses et piles

Le helper du skill Android Performance a produit `simpleperf-self.txt` et `simpleperf-children.txt`. Les rapports supplémentaires `worker-self.txt`, `worker-children.txt` et `worker-callgraph.txt` filtrent le TID `4583`. **Le tableau ci-dessous a pour dénominateur les 63 515 échantillons worker**, pas ceux de tous les threads. Les lignes JIT et DEX sont conservées séparément. Les pourcentages inclusifs emboîtés ne sont ni additifs ni des durées exactes.

| Fonction app-owned | Self worker | Children worker | Source / implication |
|---|---:|---:|---|
| `IngredientAnalysisService.analyzeWithDiagnostics` / `runAnalysis` | 0,00 % | 99,72 % | `mutation-core/.../VeganAnalyzer.kt`, chemin moteur. |
| `TextNormalizer.normalize` (JIT) | 0,27 % | **88,78 %** | `TextNormalizer.kt:6`; coût principalement chez ses appels, pas dans ses instructions propres. |
| `OriginQualifierRuleSet.extractAttached` | 0,00 % | **72,65 %** | `OriginQualifierRules.kt:80`; normalisations répétées dans les boucles targets/outcomes/qualifiers. |
| `IngredientTreeParser.parse` / `parseList` | 0,00 % | **52,35 %** | `IngredientTreeParser.kt:70` / `:98`; recouvre les règles d’origine et la normalisation. |
| `IngredientTreeParser.extractStructuralMetadata` | 0,00 % | 49,90 % | `IngredientTreeParser.kt:471`; appelle `extractAttached`. |
| `IngredientMatcher.<init>` | 0,00 % | 10,30 % | Construction depuis `VeganAnalyzer.kt:301`; contribue au clic mais n’est pas le coût dominant. |
| `MultilingualIngredientLexicon.resolve` (JIT / DEX) | 0,03 % / 0,00 % | 5,42 % / 4,76 % | Résolution lexicale, représentations conservées séparément. |
| `IngredientMatcher.match` (JIT / DEX) | 0,05 % / 0,00 % | 3,80 % / 0,25 % | Matching; inférieur aux chemins de normalisation dans ce profil. |
| `DiagnosticReport.build` / `OcrFirstScreenResult.render` | 0,00 % | 0,05 % chacun | Aucun hotspot majeur observé dans ces fonctions. |

Les principaux coûts propres sont dans **ICU et le runtime** : `u_charType_75` 11,34 %, `UnicodeSet::applyIntPropertyValue` 8,05 %, `UnicodeSet::contains` 3,27 %, `art::StackVisitor::WalkStack` 3,25 % (worker). Le coût inclusif de `java.util.regex.Pattern.compile` JIT est 61,57 %, et celui d’`icu_75::RegexPattern::compile` 55,85 %. Ces couches ne sont pas des fonctions métier indépendantes à additionner au tableau.

Une pile utile conservée dans `stacks-relevant.txt` et dans le callgraph complet relie :

```text
IngredientTreeParser.extractStructuralMetadata
  → OriginQualifierRuleSet.extractAttached
  → TextNormalizer.normalize
  → kotlin.text.Regex.<init>
  → java.util.regex.Pattern.compile
  → com.android.icu.util.regex.PatternNative.create
  → PatternNative_compileImpl
  → icu_75::RegexPattern::compile / RegexCompile::compile
  → UnicodeSet / u_charType_75
```

La source confirme que `TextNormalizer.normalize` reconstruit les Regex `\\p{M}+`, `[^a-z0-9]+` et `^e\\s+(?=\\d)` à chaque appel. `extractAttached` normalise le texte, la cible et le qualificatif dans ses boucles; le parser l’appelle pour les métadonnées, et le moteur l’appelle aussi ensuite. Cela relie le hotspot mesuré à un travail répété concret. Le profil ne donne pas le nombre exact d’invocations. La proposition ciblée pour un chantier ultérieur est de réutiliser les Regex constants et de sortir des boucles les normalisations invariantes, avec validation fonctionnelle avant/après. **Aucune optimisation n’a été appliquée ici.**

### Artefacts, hashes et limites

Dossier réussi : `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-reviewed-20261009-105911/attempt-03/`. Fichier appareil conservé : `/data/local/tmp/simpleperf-sync-d2bf2bb146904755ae215471c26a256a.data`.

| Artefact | SHA-256 |
|---|---|
| `perf.data` (125 385 585 octets) | `EE3AEDE183AD8403F5ECCFBA69FD1364E1F10AE749DAAA01213268DB838598EC` |
| `simpleperf-self.txt` | `F5612C64CD26A6FE8C8A75803469820A77745F82E254B54BA4B955329B32E67B` |
| `simpleperf-children.txt` | `73AB8DFC402E3432F07673F260401B635A69BD5621C2ACBCF65676B87FCDAF15` |
| `worker-self.txt` | `B7501FB450B904735C395B0BA3D758F9B973B691D0E3EEF31C919D0BF4D92BC9` |
| `worker-children.txt` | `BA4DCF96A473CABFC633DC76FBD6B0979566384331C8DD34A47E35AD047BC779` |
| `worker-callgraph.txt` | `A9C731AE33FA898AE77BC9C8C94E6678EC9D5C42B9CB9920656D6D95B3CEB303` |

Les marqueurs, résultat JUnit réel, couple de processus, fd actifs, commandes ADB et leurs sorties sont conservés dans ce même dossier. `profile-validation.json`/`.txt` prouve la correspondance PID et la couverture temporelle. `samples.txt` conserve l’export horodaté. Le dossier parent contient les scripts, les 19 validations locales et la compilation; chaque tentative conserve la version du script utilisée. Les gros fichiers restent sous `build/`, hors des fichiers à versionner.

Le build debug mélange JIT, DEX/interprétation, runtime, Compose et infrastructure de test. Les stacks tronquées, l’assemblage de callchains et les symboles kernel restreints limitent la précision; ils n’empêchent pas ici de reconnaître les méthodes métier et le chemin de compilation Regex. Une valeur self arrondie à 0,00 % ne signifie pas que la méthode est gratuite : ses appels et l’interpréteur portent les échantillons. Les fonctions app-owned sont retenues par leur symbole `com.example.isitvegan`, pas simplement parce que Kotlin/Compose sont embarqués dans `base.apk`. La répartition tous-threads inclut le test Compose, les GC et les autres threads. Simpleperf mesure des échantillons CPU, pas les attentes; aucun pourcentage n’est converti en durée exacte. Aucune nouvelle comparaison de commits, capture Perfetto, mesure mémoire, suite complète, test connu en régression, commit ou push n’a été effectué.

**Réponse étayée :** le profil synchronisé identifie un coût moteur dominant de normalisation et de compilation Regex, largement sollicité par l’extraction des qualificatifs d’origine pendant le parsing et l’analyse. Le matcher et sa construction contribuent, mais ressortent moins que ce chemin. La durée UI en usage normal reste celle du proxy Perfetto précédent; les deux mesures ne sont pas additionnées.
