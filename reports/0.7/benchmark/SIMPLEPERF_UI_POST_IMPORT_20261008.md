# Simpleperf — analyse UI post-import — 8 octobre 2026

## Verdict

**Essai non exploitable pour profiler le clic « Analyser » ou attribuer le CPU au moteur.** L’enregistrement a bien duré 29,9702 s et produit 9 877 échantillons sans perte, mais les rapports ne contiennent aucun échantillon sur `DefaultDispatch` ni aucune fonction app-owned de `IngredientAnalysisService`, `IngredientMatcher`, `IngredientTreeParser`, `VeganAnalyzer` ou `DiagnosticReport`. Simpleperf ne fournit pas de preuve d’entrée tactile; l’absence du worker d’analyse interdit de vérifier que l’action a été capturée. La consigne d’une seule tentative est respectée : aucune seconde capture n’a été lancée.

## Configuration et collecte

- Appareil : Nokia G42 5G, serial `AQ5003H044Q32600084`, Android 15, ADB `device`.
- Worktree post-import : `.benchmark-worktrees/post-meat-3ec32d3`, HEAD `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`.
- Application déjà installée, non reconstruite/réinstallée : `com.example.isitvegan`, version `0.6.13.7` (versionCode 59), debug et debuggable.
- APK déclarée dans les relevés antérieurs : SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`.
- Texte corrigé du benchmark préparé dans l’éditeur selon la procédure; la capture Simpleperf seule ne permet pas de confirmer le contenu édité ni le toucher.
- Événement `cpu-clock`, fréquence demandée 4 000 Hz, callgraph `-g`, durée automatique 30 s. Capture 20:33:54 UTC environ, commande exacte dans `capture-command.txt`.
- Résultat Simpleperf : `Recorded for 29.9702 seconds`; 9 877 échantillons, 0 perdus. Avertissement de restriction d’accès aux symboles kernel.
- Conditions après capture : batterie 100 %, alimentation AC true, USB false, température batterie 24,7 °C; Thermal Status 0. Avant capture, batterie/températures n’ont pas été relevées dans cet essai; état thermique préalable inconnu.

## Échantillons observés

Répartition threads : MainActivity 6 277 (63,55 %), RenderThread 2 633 (26,66 %), DefaultExecutor 648 (6,56 %), Profile Saver 150 (1,52 %), Binder 128 (1,30 %), autres threads en proportions faibles. Le worker `DefaultDispatch` absent est notable au vu de la trace Perfetto antérieure, mais les deux profils ne doivent pas être combinés comme s’ils étaient synchronisés.

Top `self` (CPU propre, échantillonné) : `artQuickToInterpreterBridge` 3,85 %, `art::interpreter::ExecuteSwitchImplCpp` 2,62 %, `art::StackVisitor::WalkStack` 1,73 %, symboles kernel non résolus 1,29 % et 1,11 %, `artQuickGenericJniTrampoline` 1,28 %, puis diverses fonctions ART, RenderThread/HWUI et runtime. Aucun symbole métier de l’application n’est classé.

Le rapport `children` est inclusif : ses principaux coûts ART/interpréteur remontent notamment à `ExecuteSwitchImplAsm` 63,55 % et `artQuickToInterpreterBridge` 63,54 %. Des piles de traversal/draw Android et Compose apparaissent aussi, par exemple `ViewRootImpl.performTraversals` (~28,49 %), `performDraw` (~28,25 %), `ThreadedRenderer.draw` (~27,79 %) et `AndroidComposeView.dispatchDraw` (~26,37 %). Cela décrit surtout l’activité UI/rendu échantillonnée et ne prouve pas le coût du moteur. Les pourcentages ne sont pas des durées exactes; lignes `children` imbriquées non additives.

## Fichiers

- `perf.data` — capture brute, 9 651 276 octets; SHA-256 `5FB123C501AB5E2ABFC9E49395523EA710C2DB1D2AB7E2892D3A7001A17F3A64`.
- `simpleperf-record.log`, `record-exit.txt`, `capture-command.txt`, `analysis-command.txt`, `preflight.txt`.
- `simpleperf-self.txt`, `simpleperf-children.txt`, `simpleperf-threads.txt`, `hotspots-helper.log`.
- `battery-after.txt`, `thermal-after.txt`.

Le helper `simpleperf_hotspots.sh` de la skill Android Performance a généré les rapports depuis les données de l’appareil (fallback device-side; aucun Simpleperf hôte disponible). Sur Git Bash Windows, `MSYS_NO_PATHCONV=1` est requis pour préserver les chemins ADB `/data/...`.

## Limites

Build debug avec interpréteur/JIT, donc les coûts ART peuvent dominer et les symboles Kotlin/Java app peuvent manquer. L’échantillonnage `cpu-clock` ne mesure ni le temps mural ni les attentes. Les symboles kernel sont restreints. Aucun Perfetto supplémentaire, test, mesure mémoire, rebuild ou réinstallation n’a été effectué. Le profil ne permet pas d’identifier les fonctions CPU de l’analyse. Il faudrait une nouvelle tentative autorisée avec le clic et la fenêtre de calcul vérifiables, mais aucune répétition automatique n’est effectuée ici.
