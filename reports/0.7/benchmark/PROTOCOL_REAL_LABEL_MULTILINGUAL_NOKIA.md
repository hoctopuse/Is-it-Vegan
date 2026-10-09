# Protocole — étiquette réelle multilingue sur Nokia G42

## Harness et périmètre

Test instrumenté : `RealLabelMultilingualBenchmarkInstrumentedTest`, APK `debug`, AndroidJUnitRunner. Il lit les assets de l’APK du commit exécuté (`ingredients.json`, `ingredient_aliases_multilingual.json`, `origin_qualifier_rules.json`) et utilise l’horloge monotone Android `SystemClock.elapsedRealtimeNanos()`. Aucun appel ML Kit ni accès réseau n’entre dans les durées texte.

Le texte propre et les variantes sont des chaînes constantes dans le test, avec les retours à la ligne du bloc fourni. Scénarios : texte propre avec `Flavouring`, puis une seule mutation par passage : `Glucose-fructose syna`, `WHEAT lour`, `CTitric aid`, et `Favouring`. Chaque analyse passe par `IngredientAnalysisService.analyzeWithDiagnostics(..., FULL_LABEL, EN)`, le chemin moteur qui produit les diagnostics. L’analyse retournée et ses invariants sont vérifiés en dehors de l’intervalle mesuré, après chaque appel. Pour `Vegetable oil (Palm)`, l’attente actuelle est le concept `vegetable_oil` protégé par la règle active qui ignore le contenu entre parenthèses; le test ne suppose pas un match `palm_oil`.

Pour chaque scénario, le premier appel mesuré signifie « première analyse dans ce processus instrumenté après lecture et chargement des assets ». Ce n’est pas un démarrage à froid du processus Android et cela ne vide aucun cache système. Il est suivi de 5 échauffements puis de 10 mesures. Le logcat contient les dix durées brutes en nanosecondes, le minimum, la médiane basse (5e valeur triée), la médiane arithmétique et le maximum. Aucun log, I/O ou assertion n’est exécuté dans l’intervalle chronométré.

Le premier accès aux assets et les premiers chargements `IngredientKnowledge.fromJson` et `IngredientMatcher` sont chacun mesurés une fois et étiquetés `cold`; les deux dernières opérations ont aussi leurs séries 5+10. Le test mesure également extraction de section et parsing de l’arbre sur le texte propre, isolément, avec première invocation puis séries 5+10. Ces composants ne représentent pas à eux seuls toute l’analyse : le pipeline complet reste la mesure de référence. Il n’existe pas de frontière publique permettant de chronométrer le matching contextuel, les règles d’origine et la construction du résultat séparément sans reproduire ou changer le chemin. Ces coûts restent donc compris dans l’analyse complète.

Le test journalise la taille UTF-8 des trois assets, le nombre de concepts, mappings et groupes d’alias du commit courant. Ces nombres sont descriptifs et ne sont pas des critères d’échec. Il ne mesure ni PSS ni allocations : aucun delta mémoire fiable et attribuable n’est disponible avec ce harness.

## Préparation et exécution PowerShell

Depuis la racine du dépôt, relever le commit et l’état. Ne poursuivre que si `adb devices -l` affiche le serial attendu avec l’état `device`; `unauthorized` ou l’absence du Nokia arrête l’exécution avant toute commande `shell` ou installation :

```powershell
git status --short --branch
git rev-parse HEAD
adb devices
adb devices -l
$serial = "AQ5003H044Q32600084"
$state = adb -s $serial get-state 2>&1
if ($LASTEXITCODE -ne 0 -or $state.Trim() -ne "device") { throw "Nokia non autorisé ou absent; arrêter sans installation/mesure" }
adb -s $serial shell getprop ro.product.model
adb -s $serial shell getprop ro.build.version.release
adb -s $serial shell dumpsys battery
adb -s $serial shell dumpsys thermalservice
```

Le serial précédemment associé au Nokia G42 est `AQ5003H044Q32600084`; il doit tout de même être revérifié avant chaque passage. Le worktree principal contient la photo non suivie sous `app/src/androidTest/assets/`; le lancement depuis ce worktree la ferait inclure dans les assets du test Android. Pour garder l’image hors de l’APK, préparer un worktree détaché temporaire à partir du commit à mesurer, qui n’hérite pas des fichiers non suivis, et y copier uniquement le test moteur :

```powershell
$root = (Get-Location).Path
$benchmarkRoot = Join-Path $root ".benchmark-worktrees"
$benchmarkPath = Join-Path $benchmarkRoot "real-label-3ec32d3"
if (Test-Path -LiteralPath $benchmarkPath) { throw "Le chemin existe déjà; choisissez un nouveau chemin" }
New-Item -ItemType Directory -Path $benchmarkRoot -Force | Out-Null
if ((git rev-parse HEAD).Trim() -ne "3ec32d3914ad071b8435b9155e2e7d6467aeebd1") { throw "HEAD différent du commit documenté" }
git worktree add --detach $benchmarkPath 3ec32d3914ad071b8435b9155e2e7d6467aeebd1
if ($LASTEXITCODE -ne 0) { throw "Création du worktree échouée" }
$testRelativePath = "app/src/androidTest/java/com/example/isitvegan/RealLabelMultilingualBenchmarkInstrumentedTest.kt"
Copy-Item -LiteralPath (Join-Path $root $testRelativePath) -Destination (Join-Path $benchmarkPath $testRelativePath)
Push-Location $benchmarkPath
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.RealLabelMultilingualBenchmarkInstrumentedTest" --console=plain *> app/build/real_label_multilingual_benchmark_gradle.log
$runExitCode = $LASTEXITCODE
Pop-Location
if ($runExitCode -ne 0) { throw "Benchmark instrumenté échoué; code $runExitCode" }
```

Après le run, recueillir à nouveau batterie et thermique, puis conserver le résultat brut avant toute nouvelle exécution. Les artefacts sont dans le worktree temporaire :

```powershell
adb shell dumpsys battery
adb shell dumpsys thermalservice
Get-ChildItem (Join-Path $benchmarkPath "app/build/outputs/androidTest-results/connected/debug") -Recurse -File
Select-String -Path (Join-Path $benchmarkPath "app/build/outputs/androidTest-results/connected/debug/*/logcat-*.txt") -Pattern "REAL_LABEL_(CORPUS|TIMING)\|"
```

Noter pour chaque passage : commit complet, modèle et serial retournés par ADB, Android, variante `debug`, batterie et température avant/après, USB/alimentation, état thermique, heure, et incident de charge de fond ou de chauffe. Garder les logcat/XML locaux liés au passage; transcrire les valeurs brutes dans le rapport de résultats avant que Gradle ne remplace les artefacts `build/`.

## Diagnostic UI sur un clic

Le test Compose `MainScreenInstrumentedTest.analysisClickUsesTheEditedOcrFieldValue` prépare deux valeurs distinctes dans l’éditeur, clique une fois et vérifie que l’ancienne valeur marqueur ne ressort pas. Il établit le routage UI vers le texte courant; il ne lance pas ML Kit et ne compte pas les appels par interception. Le code du handler contient un seul appel de service derrière le garde `analyzing`.

Dans un worktree temporaire propre au commit courant, copier le seul fichier de test UI modifié (`MainScreenInstrumentedTest.kt`) par-dessus sa version de ce worktree; la photo non suivie reste absente. Ce test démarre l’écran, saisit la valeur initiale puis l’éditée dans le champ, et clique une fois. Il peut être lancé lorsque le serial est `device` :

```powershell
$serial = "AQ5003H044Q32600084"
if ((adb -s $serial get-state).Trim() -ne "device") { throw "Nokia non autorisé dans ADB; ne pas installer ni lancer" }
Copy-Item -LiteralPath (Join-Path (Get-Location) "app/src/androidTest/java/com/example/isitvegan/MainScreenInstrumentedTest.kt") -Destination (Join-Path $benchmarkPath "app/src/androidTest/java/com/example/isitvegan/MainScreenInstrumentedTest.kt")
Push-Location $benchmarkPath
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.MainScreenInstrumentedTest#analysisClickUsesTheEditedOcrFieldValue" --console=plain *> app/build/edited_text_click_test.log
$runExitCode = $LASTEXITCODE
Pop-Location
if ($runExitCode -ne 0) { throw "Test UI instrumenté échoué; code $runExitCode" }
```

Pour isoler le délai mural et le thread principal, Perfetto est préférable à Simpleperf pour ce clic. Ouvrir l’application installée, passer en mode OCR, coller le bloc anglais corrigé dans le champ éditable, fermer le clavier et attendre que l’écran soit stable. Ne pas sélectionner une photo ni inclure OCR. Démarrer alors la trace courte ci-dessous et effectuer un seul clic sur « ANALYSE » pendant la fenêtre. Comme la saisie est déjà préparée, le geste de clic reste manuel; je demanderai ce geste quand la capture sera effectivement prête. Ne pas effacer les traces existantes sur l’appareil; choisir un nom unique et vérifier le serial :

```powershell
$serial = "AQ5003H044Q32600084"
$package = "com.example.isitvegan"
$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$traceName = "edited-analysis-$runId.pftrace"
$deviceTrace = "/data/misc/perfetto-traces/$traceName"
$artifactDir = Join-Path (Get-Location) "reports/0.7/benchmark/artifacts/$runId"
if ((adb -s $serial get-state).Trim() -ne "device") { throw "Nokia non autorisé dans ADB; ne pas capturer" }
New-Item -ItemType Directory -Path $artifactDir -Force | Out-Null
$thermalBefore = adb -s $serial shell dumpsys thermalservice
$batteryBefore = adb -s $serial shell dumpsys battery
adb -s $serial shell perfetto --background-wait -o $deviceTrace -t 25s --app $package sched freq idle am wm gfx view binder_driver hal dalvik
# Effectuer maintenant un seul clic sur ANALYSE pendant la fenêtre de 25 secondes.
```

Après le clic et la fin de la fenêtre Perfetto, recueillir la trace et l’état après capture :

```powershell
adb -s $serial pull $deviceTrace (Join-Path $artifactDir $traceName)
$thermalAfter = adb -s $serial shell dumpsys thermalservice
$batteryAfter = adb -s $serial shell dumpsys battery
$thermalBefore | Set-Content (Join-Path $artifactDir "thermal-before.txt")
$thermalAfter | Set-Content (Join-Path $artifactDir "thermal-after.txt")
$batteryBefore | Set-Content (Join-Path $artifactDir "battery-before.txt")
$batteryAfter | Set-Content (Join-Path $artifactDir "battery-after.txt")
```

Importer ensuite le `.pftrace` dans Perfetto UI/Trace Processor. Examiner le thread principal, le thread Default exécutant l’analyse, les slices de méthode app-owned, l’ordonnancement, les attentes/binder, les frames et le moment où le résultat est composé. La commande légère ne garantit pas les détails Compose runtime ou FrameTimeline. Un profil CPU seul ne mesure pas les attentes.

## Comparaison de commits

La baseline déjà mesurée (`9d98825…`) et le HEAD vérifié (`3ec32d3…`) sont disponibles, mais aucune comparaison n’est lancée dans ce diagnostic : la baseline utilise `Phase4MatcherProfileInstrumentedTest`, n’a pas les dix échantillons bruts et ne couvre pas cette étiquette. Le HEAD contient aussi plus de concepts/mappings. La priorité est la mesure du scénario corrigé et du clic réel sur le HEAD actuel. La comparaison pré/post optimisation du matcher n’est pas utile à cette question et n’est pas exécutée.

## Scénario OCR image

Le scénario image n’est pas intégré au test. L’image `5087.jpg` est présente dans un répertoire d’assets Android instrumentés non suivi par Git; ce protocole ne la copie, ne l’ajoute à un APK ni ne modifie ce répertoire. La reconnaissance ML Kit, la segmentation/sélection du bloc et l’analyse du texte OCR doivent être mesurées séparément, avec une autorisation explicite d’utilisation/embarquage et les contraintes de licence et de confidentialité vérifiées. En attendant, ce scénario est manuel et non exécuté; A/B restent reproductibles sans image.

## Exécution réelle du 8 octobre 2026

Le protocole moteur a été exécuté en worktrees détachés pour `6be60933df464dccb857b8d3287825011567344b` et `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`. Identité avant run : `adb devices -l` a montré `AQ5003H044Q32600084` à l’état `device`; `getprop ro.product.model` = `Nokia G42 5G`; Android 15. Chaque commande ADB a explicitement utilisé `-s AQ5003H044Q32600084`. Le test moteur ciblé et le test UI `MainScreenInstrumentedTest#analysisClickUsesTheEditedOcrFieldValue` ont chacun réussi sur les deux commits. Les XML et logcat sont conservés dans les deux worktrees, sous `app/build/outputs/androidTest-results/connected/debug/` et `app/build/benchmark-artifacts/`. Voir [le rapport de résultats](RESULTS_MEAT_IMPORT_ANALYSIS_NOKIA.md) pour commandes, séries brutes, relevés et analyse.

Les worktrees restent présents : `.benchmark-worktrees/pre-meat-6be6093` et `.benchmark-worktrees/post-meat-3ec32d3`. La première contient une capture Perfetto de clic non valide; l’analyse de la trace valide candidate ne trouve aucun événement input, motion ou key. Il faut reprendre avec l’écran préparé, une capture courte et un clic manuel unique. La trace post-import n’a pas été exécutée. Les tests de routage édité ne remplacent pas le profilage mural d’un clic réel.

## Reprise UI post-import — état du 8 octobre 2026

Le Nokia autorisé `AQ5003H044Q32600084` est en état `device` et `Nokia G42 5G`, Android 15. Le commit post-import est `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`. Le build debug local existe (`versionCode 59`, `versionName 0.6.13.7`), mais le package `com.example.isitvegan` n’est pas installé (aucun chemin `pm path`; launcher au premier plan). Aucune installation, interaction ou capture n’a été faite. La consigne « N’installe rien » bloque donc le profil jusqu’à ce que l’application soit installée par son propriétaire ou que l’installation soit autorisée. Voir le rapport de résultats pour le hash de l’APK et les commandes de vérification.

## Installation de l’APK post-import — 8 octobre 2026

Installation autorisée et effectuée de l’unique APK préconstruite et hashée décrite dans le rapport de résultats. ADB `AQ5003H044Q32600084`, G42 5G, Android 15. `pm path` et `dumpsys package` confirment `com.example.isitvegan`, version `0.6.13.7`; `MainActivity` est lancée. La capture attend la préparation manuelle de l’étiquette corrigée; ne pas démarrer Perfetto avant confirmation que le clavier est fermé et le bouton Analyse prêt.

## Tentative Perfetto post-import du 8 octobre 2026 — non démarrée

Sébastien a confirmé que l’écran était prêt. La commande avec `--no-clobber` a été rejetée par la version Perfetto du Nokia (`unrecognized option`) avant ouverture d’une session; aucun fichier trace n’a été créé et aucun clic n’a été demandé. Les relevés batterie/thermique et l’erreur sont décrits dans le rapport de résultats. Ne pas répéter automatiquement; il faut une autorisation explicite pour une commande corrigée utilisant un nom unique.

## Résultat de la capture UI post-import — 8 octobre 2026

La seule capture démarrée est conservée dans `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/ui-post-20261008-200620.pftrace`. Perfetto a fonctionné jusqu’à sa limite de 120 s, sans arrêt manuel faute de confirmation dans la conversation. Trace Processor y détecte un couple unique de slices `ACTION_DOWN`/`ACTION_UP` de `MainActivity`, suivi d’environ 11,5 s Running sur un `DefaultDispatch`, puis d’une frame Compose de la MainActivity marquée tardive. Le délai down→fin de la première frame après analyse est estimé à 11,75 s. Voir le rapport de résultats pour requêtes, artefacts, hash et limites. La capture sert à localiser grossièrement le coût, mais la valeur est un proxy du premier rendu, pas un callback direct d’affichage; aucune nouvelle capture n’a été faite.

## Simpleperf post-import — 8 octobre 2026

Une capture unique de 30 s a produit 9 877 échantillons sans perte sur le Nokia G42 5G, commit post-import `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`, app debug déjà installée `0.6.13.7` et APK SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`. Commande : `adb -s AQ5003H044Q32600084 shell simpleperf record --app com.example.isitvegan -o /data/local/tmp/simpleperf-ui-20261008-203354.data -e cpu-clock -f 4000 -g --duration 30`. Fichiers bruts et rapports self/children : `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-ui-20261008-203354/`.

**Cette tentative n’est pas valide pour l’attribution du CPU au clic** : aucun échantillon `DefaultDispatch` ni aucune fonction app-owned du moteur n’apparaît. L’essentiel des échantillons correspond à MainActivity, RenderThread, ART et au rendu Compose. Simpleperf ne vérifie pas le geste tactile. Aucun essai supplémentaire n’a été lancé, conformément à la limite d’une tentative. Conditions après : batterie 100 %, AC true, USB false, température batterie 24,7 °C, Thermal Status 0; conditions initiales non relevées. Détails dans le rapport de résultats et `simpleperf-summary.md` sous le dossier d’artefacts.

## Test Simpleperf UI instrumenté — 8 octobre 2026 — tentative non exploitable

Le nouveau test ciblé `RealLabelAnalysisUiCaptureTest#oneEditedRealLabelAnalysisClickEmitsCaptureMarkers` a été ajouté dans le worktree post-import `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`. Il emploie le texte propre exact, ferme le clavier et réalise un seul clic. Le test APK seulement a été installé; le package application `0.6.13.7` et son hash antérieur (`6EB2BE…CC8D9B28`) sont restés en place. Une tentative Simpleperf a enregistré 16,9393 s et 5 479 samples sans perte. Les marqueurs pré-clic/clic sont présents; l’attente a trouvé un verdict dans la sémantique Compose, mais l’assertion d’affichage a échoué parce que le verdict était hors écran, donc le marqueur résultat manque.

Le rapport `self` montre 57,66 % MainActivity et 26,14 % RenderThread; DefaultDispatch ne compte que cinq samples sur des frames de parking. Pas de frame métier d’analyse. La capture est non exploitable pour identifier des hotspots métier; aucun nouvel essai n’a été lancé. Le test source a été ajusté ensuite pour faire défiler le résultat, puis compilé localement sans exécution appareil. Voir le rapport de résultats et `SIMPLEPERF_TARGETED_ANALYSIS_UI_20261008.md`.

Si une future capture Simpleperf ne résout toujours pas les frames Kotlin/Java métier, passer au profileur CPU géré Android Studio en mode Kotlin/Java sampling/method; le profileur n’a pas été lancé dans cette reprise.

## Synchronisation du Simpleperf — 8 octobre 2026 — blocage avant clic

Les PIDs historiques prouvent un changement de processus : ancien Simpleperf PID `14231`, marqueur du test précédent PID `23703`. Dans l’orchestration ciblée, le PID package avant runner était `24218`, puis le nouveau test annonçait READY avec PID `24863`; `pidof` confirmait ce dernier. Le remplacement du processus reste l’explication probable de l’arrêt prématuré du profil antérieur, sans preuve directe de la cause exacte.

Deux orchestrations ont été arrêtées avant le clic. Dans la première, l’attachement Simpleperf par PID `-p 24863` a échoué avec `Permission denied` à l’ouverture de `cpu-clock`; le test a reçu son signal abort unique. Dans la seconde stratégie, `--app` après READY était prévue, mais le script a pris un ancien READY logcat (PID `24863`) alors que le PID courant était `25187`; il a donc refusé de profiler. Le nouveau passage READY (PID `25315`) a reçu un signal abort unique avant le clic. Aucun profil Simpleperf ne s’est lancé et aucun résultat CPU n’est disponible.

Le poller du script `collect-synchronized-simpleperf-appmode.ps1` a été corrigé pour ignorer les run IDs déjà présents avant le runner. Cette version a été vérifiée syntaxiquement seulement, pas exécutée. Conformément à la règle d’arrêt, aucune troisième tentative n’a été faite. Voir le rapport ciblé et ses artefacts dans le worktree post-import. La suite requiert une nouvelle tentative autorisée avec le filtre READY corrigé et l’attachement `--app` démarré après READY.

## Tentative synchronisée `--app` — 9 octobre 2026

Le test ciblé a émis un READY neuf (`29cce2241b104a41a822b53254cb633d`, PID `31073`), confirmé présent par `pidof com.example.isitvegan`. Le runner androidTest seul a été installé; l’APK applicative `0.6.13.7` (versionCode 59, SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`) est restée en place. L’APK de test a pour SHA-256 `8E60279D5BF6C2C8D2EABB95243B2F124B70C2E3CFCFE8064E29E9087A2D059D`; l’inspection confirme qu’elle n’embarque pas la photo.

Après `STARTED`, Simpleperf a créé un lanceur PID `31230` et un enfant d’enregistrement `--in-app` PID `31236`. Le script attendait un unique PID et a aborté avant le clic. JUnit a échoué sur l’attente du signal de clic; READY est le seul marqueur. Le brut Simpleperf de 2,04753 s (2 631 échantillons, 0 perdus) ne couvre donc pas l’analyse. Aucun rapport self/children ni aucune fonction moteur n’est rapporté. Les processus de collecte étaient arrêtés après l’abandon; le processus androidTest restant a été fermé par `am force-stop com.example.isitvegan.test`, puis les PID du runner et de Simpleperf ont été vérifiés absents. L’application principale est restée installée. Aucun essai n’a été répété.

Le script préparé dans `app/build/benchmark-artifacts/simpleperf-synchronized-20261009-102637/collect-synchronized-simpleperf-appmode.ps1` sélectionne maintenant l’unique enfant `--in-app` du run au lieu d’exiger un seul PID total. Le correctif a uniquement passé l’analyse syntaxique; il n’a pas été exécuté sur le Nokia. Résultats complets, hashes et journaux : `SIMPLEPERF_SYNCHRONIZED_ATTEMPTS_20261008.md` et le dossier d’artefacts ci-dessus.

## Orchestration validée et collecte réussie — 9 octobre 2026

La version relue de l’orchestration est dans le worktree post-import, sous `app/build/benchmark-artifacts/simpleperf-reviewed-20261009-105911/`. Elle comprend `collect-synchronized-simpleperf-appmode.ps1`, `orchestration-functions.ps1`, `validate-orchestration.ps1` et `validate-profile.py`. Le test reste `RealLabelAnalysisUiCaptureTest#oneEditedRealLabelAnalysisClickEmitsCaptureMarkers`. Les données de production et l’APK applicative n’ont pas changé.

Le script génère le run ID et le transmet au test avec `-e simpleperfRunId`; il attend uniquement ce READY, vérifie le PID package, reconnaît le lanceur `--app` et l’enfant `--in-app` par leur commande et leur run ID avec des objets Regex indépendants, puis exige STARTED et des fd `perf_event` ouverts avant le signal de clic. Après RESULT_DISPLAYED, il signale le lanceur shell par SIGINT, attend la finalisation et la stabilité du fichier, récupère le brut, puis libère le test. Les codes JUnit sont contrôlés indépendamment du code retour ADB. Les commandes et sorties sont conservées séparément; les processus PowerShell retiennent leur handle et les journaux actifs sont lus avec partage lecture/écriture. Toutes les attentes sont bornées. Le nettoyage cible seulement les outils de ce run et le package androidTest.

Le test utilise le texte exact du moteur (SHA-256 UTF-8 `7e265978f95c9731041b1f3c585196e07fb72061adb917a69a51bde7b74a8fbc`), mode `OCR_LABEL`, préférence UI/langue FR comme le parcours UI antérieur, texte anglais corrigé. Il vérifie le contenu de l’éditeur, l’absence de verdict avant le clic et la visibilité du nouveau verdict après défilement. Aucun OCR n’est lancé. Le timestamp `clockNs=System.nanoTime()` est confronté aux samples enregistrés avec `--clockid monotonic`; `monoNs=elapsedRealtimeNanos()` est conservé séparément.

Commandes réellement utilisées, depuis `.benchmark-worktrees/post-meat-3ec32d3` :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\gradlew.bat :app:compileDebugAndroidTestKotlin :app:assembleDebugAndroidTest --console=plain
$base = (Resolve-Path 'app/build/benchmark-artifacts/simpleperf-reviewed-20261009-105911').Path
powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $base 'validate-orchestration.ps1') -PreviousArtifacts (Resolve-Path 'app/build/benchmark-artifacts/simpleperf-synchronized-20261009-102637').Path
powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $base 'collect-synchronized-simpleperf-appmode.ps1') -ArtifactDir (Join-Path $base 'attempt-03') -TestApk (Resolve-Path 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk').Path
```

La commande de collecte effective a été :

```powershell
adb -s AQ5003H044Q32600084 shell simpleperf record --app com.example.isitvegan -o /data/local/tmp/simpleperf-sync-d2bf2bb146904755ae215471c26a256a.data -e cpu-clock -f 4000 -g --clockid monotonic --duration 90 --start_profiling_fd 1 --add-meta-info run_id=d2bf2bb146904755ae215471c26a256a
```

Pour une future reproduction autorisée, choisir un nouveau dossier inexistant : le script refuse tout écrasement. Aucun fichier appareil existant n’est effacé; chaque run utilise un chemin unique. Installer uniquement le runner androidTest; l’APK applicative doit conserver le hash installé `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`, version `0.6.13.7`/59. L’APK runner utilisée a pour hash `8661FB8754F85E24A0B400D016F4B660659A5A42A6BD25522225B622646B616A`, sans photo. Le script contrôle l’identité complète du Nokia et le hash du fichier `base.apk` réellement installé.

Les deux premiers passages distincts ont corrigé des défauts démontrés de préflight (`ExitCode` vide) et de lecture concurrente des journaux. Le troisième a réussi : **OK (1 test)**, run `d2bf2bb146904755ae215471c26a256a`, PID échantillonné et test `4487`, worker TID `4583`, **19,0192 s / 144 286 samples / 0 perdus / 5 236 piles tronquées**. Les données horodatées couvrent BEFORE_CLICK à RESULT_DISPLAYED; les 63 515 échantillons worker se situent dans cette fenêtre. Le décalage clic → assertion visible de 16,594 s est celui de l’environnement profilé/instrumenté et ne remplace pas le proxy Perfetto 11,75 s.

Les rapports `self`/`children` sont produits par le helper Android Performance; les rapports du seul worker et le callgraph permettent l’attribution. Les principales lignes worker sont `TextNormalizer.normalize` JIT 88,78 % inclusif, `OriginQualifierRuleSet.extractAttached` 72,65 %, `IngredientTreeParser.parse` 52,35 % et `IngredientMatcher.<init>` 10,30 %. Les piles relient la normalisation à `Regex.<init>` / `Pattern.compile` / ICU. **Pourcentages inclusifs imbriqués, non additifs.** Les détails, coûts propres, sources, limites et hashes sont dans `SIMPLEPERF_SYNCHRONIZED_ATTEMPTS_20261008.md`. Aucune capture supplémentaire après le succès.
