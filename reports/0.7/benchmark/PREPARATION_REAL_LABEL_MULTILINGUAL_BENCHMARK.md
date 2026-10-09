> **Mise à jour du 8 octobre 2026 — préparation exécutée.** Les résultats physiques sont consignés dans [RESULTS_MEAT_IMPORT_ANALYSIS_NOKIA.md](RESULTS_MEAT_IMPORT_ANALYSIS_NOKIA.md). Les commits comparés sont `6be60933df464dccb857b8d3287825011567344b` et `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`. Le Nokia G42 5G/Android 15 a été vérifié. Les mesures moteur et tests UI ciblés ont réussi sur les deux commits. Deux traces pré-import ne contiennent pas le clic attendu; le profil UI réel reste donc à refaire avec un seul geste manuel pendant la capture. Les worktrees temporaires sont conservés. Les paragraphes de préparation ci-dessous décrivent l’état initial et sont remplacés par cette mise à jour pour les résultats actuels.
# Préparation du benchmark — étiquette réelle multilingue

## État

Harness préparé; aucune mesure physique n’a été exécutée et aucun résultat de performance n’est revendiqué. Le worktree au début de l’inspection était `master`, HEAD `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`, sans modification suivie/stagée. Le seul élément non suivi observé est `app/src/androidTest/assets/benchmark/`, contenant `real-labels/5087.jpg` (419 514 octets, SHA-256 `0035C77089F01B0D1D20553B8BE3848AE6ECB05A6EA4E59025010BDF907083A9`). Il a été laissé intact et n’est pas inclus dans les changements. Comme Android empaquette les assets `androidTest`, la commande physique doit s’exécuter dans un worktree temporaire propre au commit, auquel seul le fichier de test est copié; le protocole donne les commandes et exclut ainsi ce fichier local.

## Livrables et scénarios

- `app/src/androidTest/java/com/example/isitvegan/RealLabelMultilingualBenchmarkInstrumentedTest.kt` ajoute un test textuel Android instrumenté hors production. Il mesure les temps d’accès/chargement, le chargement `IngredientKnowledge`, la construction du matcher, l’extraction de section, le parsing isolé et l’analyse complète.
- `reports/0.7/benchmark/PROTOCOL_REAL_LABEL_MULTILINGUAL_NOKIA.md` donne le protocole et les commandes PowerShell.
- Scénarios moteurs : texte corrigé avec `Flavouring`, et mutations déterministes `syna`, `WHEAT lour`, `CTitric aid`, `Favouring`. Tous analysent des chaînes en mémoire; aucun chronométrage A/B ne lance ML Kit.
- Pour chaque analyse : première invocation dans le processus (sans prétendre vider les caches), 5 échauffements, 10 répétitions; les valeurs brutes nanosecondes, minimum, médiane basse, médiane arithmétique et maximum sont émises en logcat. Les assertions sont faites hors intervalle chronométré.
- Matching contextuel, règles d’origine et construction détaillée du résultat ne sont pas isolables par une frontière publique existante; ils restent dans le temps global d’analyse. Aucun changement du code de production n’a été nécessaire.
- OCR image : manuel et non exécuté. La photo reste non suivie et non embarquée par ce changement.

## Assertions et données

Le test vérifie pour chaque exécution l’identité de la chaîne d’entrée, le mode complet, la sélection EN, le texte de section attendu pour la variante, un arbre non vide et les correspondances existantes `sugar`, `wheat_flour`, `vegetable_oil`, `glucose_syrup`. `vegetable_oil` est justifié par l’expression protégée active de la base courante (`vegetable oil`, parenthèses ignorées pour le verdict) et par les tests de hiérarchie existants; aucun match `palm_oil` n’est présumé pour le libellé `Vegetable oil (Palm)`. Il affirme aussi qu’un résultat avec inconnus ne peut pas porter le verdict `VEGAN`. Aucun verdict global de l’étiquette n’a été inventé.

À titre de contexte mesuré antérieur, le rapport baseline compte 479 concepts et 1 996 mappings au commit de référence. La lecture locale des assets du HEAD compte 488 concepts et 2 093 mappings; les groupes d’alias passent de 1 668 à 1 707. Le test produit les comptes et tailles UTF-8 de chaque build au moment où il est exécuté; ces volumes ne sont pas figés en assertions.

## Commits comparés

- Baseline Nokia déjà mesurée : `9d98825d22caa9d5a8ac65406c65e424dd1ac195`.
- Après optimisation du matcher : `21454d985a803652cd16eb40979bc348ef897ef2`.
- Référence avant optimisation : parent de `21454d9`.
- HEAD post-import viande : `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`.

Les hashes cités restent présents, mais aucune comparaison n’est retenue dans ce diagnostic. L’ancien passage `9d98825` utilisait un autre harness et un autre texte, et ne conserve pas les dix valeurs brutes; il ne permet pas d’attribuer un écart au clic actuel. La comparaison pré/post matcher ne répond pas à la question UI et n’est pas lancée. La mesure de référence à obtenir est le moteur sur le HEAD courant, puis le délai du même clic sur ce HEAD.

## Diagnostic du clic et test de régression

`MainActivity` charge la base une seule fois, dans `Dispatchers.IO`, puis active le bouton. Dans `OcrFirstScreen`, la valeur transmise au clic est `session.editableText` en mode OCR, sinon le champ manuel. Le texte OCR initial n’est pas réinjecté depuis `rawOcrText` au moment du clic. Le handler prend mode/langue/bloc en instantané, place `analyzeWithDiagnostics` dans un `coroutineScope.launch`, puis exécute analyse, formatage du résultat et `DiagnosticReport.build` dans `OcrThreading.cpu` (`Dispatchers.Default`). Une action acceptée passe par un seul appel au service; `analyzing` bloque une seconde action et désactive le bouton. Le résultat est publié sur le thread principal seulement si le texte éditable est encore identique à celui soumis.

Le travail synchrone restant avant le lancement de la coroutine comprend `focusManager.clearFocus()` et `appVersionName(context)`, qui appelle `PackageManager.getPackageInfo`. Le service reconstruit un `IngredientMatcher` par analyse et reparcourt segmentation, extraction, parsing et matching dans ce seul appel. Le chargement des assets n’est pas refait par le handler. Sans trace UI, le coût du `PackageManager`, les temps d’ordonnancement, le rendu et le temps moteur ne peuvent pas encore être ventilés.

Le test existant `OcrSessionTest` garde texte brut/édité distinct et teste le pipeline avec une chaîne directe; il ne simule pas le clic. Le nouveau `MainScreenInstrumentedTest.analysisClickUsesTheEditedOcrFieldValue` remplit le champ OCR avec une chaîne marqueur, la remplace par `Ingredients: water, sugar`, clique une fois et vérifie le verdict affiché sans marqueur. Cette action UI est une preuve du routage du champ édité, mais elle simule la valeur OCR initiale dans l’éditeur et ne lance pas ML Kit. Le code inspecté montre un appel de service pour cette action; le test runtime de cette méthode reste à exécuter pour confirmer l’intégration sur appareil.

## Validation et état appareil

Au début de cette reprise, `HEAD` est encore `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`; les commits `9d98825…` et `3ec32d3…` existent. Le seul worktree listé est le worktree principal. Les fichiers benchmark/report déjà préparés et la photo restent non suivis; le seul fichier suivi modifié par cette reprise est `MainScreenInstrumentedTest.kt`. Aucun fichier métier, OCR ou version n’a été modifié.

ADB a listé un seul appareil, serial `AQ5003H044Q32600084`, état `unauthorized`. Aucune commande `shell`, installation, mesure Android ou capture n’a été lancée. Le modèle, l’Android, la batterie, l’alimentation USB et la température restent donc non mesurés pour cette session. L’étape physique est bloquée jusqu’à ce qu’ADB voie ce serial en état `device`; aucun autre appareil ne sera utilisé.

Commandes locales exécutées sur le Lenovo hôte (elles ne sont pas des timings Android) : `:app:compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL; `:app:testDebugUnitTest --tests com.example.isitvegan.OcrSessionTest` — BUILD SUCCESSFUL. Le premier valide la compilation du harness UI/moteur; le second exécute les tests de modèle de session existants, pas le nouveau clic Compose. Aucune mesure moteur, trace Perfetto/Simpleperf, comparaison de commits ou test instrumenté n’a été exécuté.

Après passage du serial ADB à l’état `device`, le plus petit prochain passage est le test moteur ciblé dans un worktree propre à `3ec32d3`, puis un seul test de clic Compose. Ensuite, préparer l’écran avec le vrai texte édité et démarrer la capture Perfetto de 25 secondes; un clic manuel sur « Analyse » sera nécessaire pendant la fenêtre. Le protocole de worktree évite d’inclure la photo non suivie. Le délai réel du clic et son principal poste de coût restent indéterminés jusqu’à ces mesures. Aucun changement de matcher, import métier, commit ou push n’a été effectué.


## Reprise post-import — 8 octobre 2026

Le Nokia autorisé est reconnu, mais le package app post-import n’est pas installé. Comme cette reprise interdit l’installation, le profil Perfetto n’a pas été lancé et aucune nouvelle mesure UI n’est revendiquée. Le résultat et le blocage sont consignés dans `RESULTS_MEAT_IMPORT_ANALYSIS_NOKIA.md`.

## Installation du build post-import

L’APK exacte post-import est maintenant installée et vérifiée sur le Nokia. Le profil attend la préparation manuelle de l’écran avec l’étiquette corrigée; aucune trace nouvelle n’a encore été commencée.

## Capture post-import — essai non démarré

Sébastien avait préparé l’écran. La version Perfetto du Nokia a rejeté `--no-clobber` avant le démarrage; aucun clic ni trace. Conformément à la consigne, aucun second essai automatique. Voir le rapport de résultats pour l’erreur et les relevés conservés.

## Résultat du profil UI post-import

La trace unique `ui-post-20261008-200620.pftrace` est exploitable pour relier un seul ACTION_DOWN/UP, le travail Default et le premier rendu après l’analyse. Délai estimé down→première frame résultat : 11,75 s. Elle a toutefois atteint son timeout de 120 s faute de confirmation de fin; voir le rapport de résultats et ses limites. Aucun nouvel essai n’a été lancé.

## Profil Simpleperf UI post-import — 8 octobre 2026

Une tentative Simpleperf unique a été faite sur le Nokia autorisé, pour identifier les fonctions CPU d’un clic. Elle n’est pas exploitable pour ce but : 9 877 échantillons sur 29,9702 s, aucun échantillon de `DefaultDispatch` ou d’une fonction métier app-owned. Les échantillons dominants concernent MainActivity, RenderThread, ART et Compose. Aucun profil supplémentaire n’a été lancé. APK post-import déjà installée inchangée (`0.6.13.7`, SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`), commit `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`. Artefacts, self/children et détail : `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-ui-20261008-203354/`. Les mesures Perfetto antérieures montrent déjà un long calcul CPU sur DefaultDispatch; cette capture Simpleperf ne localise pas ses fonctions et ne permet pas d’affiner cette conclusion.

## Profil Simpleperf par test UI — 8 octobre 2026

Le test instrumenté dédié a inséré le texte corrigé dans le champ OCR et émis les marqueurs avant/après l’unique clic. Une capture de 16,9393 s, 5 479 échantillons, sans perte, a été conservée. Le verdict a été trouvé dans l’arbre Compose, mais le test a échoué à l’assertion visible car le résultat était hors viewport; aucun marqueur de résultat validé n’a été émis. Seulement cinq samples `DefaultDispatch` montrent le parking du scheduler, et aucune frame app-owned du moteur n’apparaît. Résultat non exploitable; aucune répétition n’a été faite conformément à la règle liée à l’absence du worker.

Le test a ensuite été ajusté pour défiler vers le verdict et a compilé localement; pas de seconde exécution. La suite conseillée si Simpleperf ne résout toujours pas les méthodes métier est le profil CPU géré Android Studio (Java/Kotlin sampling/method). Aucun profil supplémentaire n’a été lancé. Fichiers et détail : `SIMPLEPERF_TARGETED_ANALYSIS_UI_20261008.md` et dossier d’artefacts sous le worktree post-import.

## Tentatives synchronisées Simpleperf — 8 octobre 2026

La vérification des PIDs confirme le mismatch du run précédent : PID profilé `14231`, marqueurs d’instrumentation PID `23703`. Nouvelle orchestration : avant runner `24218`, READY du test `24863`. L’attachement Simpleperf direct `-p 24863` a échoué (`cpu-clock: Permission denied`) avant tout clic. La tentative distincte `--app` a été interrompue par une sélection de READY périmé; un nouveau run READY PID `25315` a reçu son signal abort, sans clic ni profilage. Le script a été corrigé pour exclure les run IDs du snapshot de logcat antérieur au runner, mais cette correction n’a pas été exécutée sur appareil. Pas de Simpleperf active, pas de `perf.data`, pas de résultats CPU; aucune troisième tentative automatique. Voir `SIMPLEPERF_SYNCHRONIZED_ATTEMPTS_20261008.md`.
