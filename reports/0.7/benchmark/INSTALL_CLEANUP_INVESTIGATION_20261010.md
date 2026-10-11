# Enquête sur le nettoyage de package pendant le benchmark Nokia — 10 octobre 2026

## Conclusion

La comparaison A/B et le profil CPU de B n’ont pas été exécutés. Les journaux de l’essai A précédent montrent des appels Android `installer_clear_app_data` pendant les deux transactions d’installation, y compris l’installation manuelle avec `adb install -r`. Après la fin de l’instrumentation, le processus applicatif a été arrêté et le package n’était plus trouvé. Les artefacts ne montrent pas qui a retiré le package : ils ne contiennent aucune commande `uninstall` et l’arrêt de processus `am_kill` n’est pas une preuve de désinstallation.

Le manifest fusionné utilise `androidx.test.runner.AndroidJUnitRunner`, cible `com.example.isitvegan` et ne déclare ni `clearPackageData`, ni Android Test Orchestrator. Une invocation directe par `am instrument` éviterait les actions de fin de la tâche Gradle, mais ne neutraliserait pas le nettoyage observé pendant l’installation elle-même. Faute de preuve que `adb install -r` préserve les données sur ce téléphone dans ces conditions, le garde-fou impose l’arrêt avant installation. Aucune autre méthode non destructive n’a été établie.

## État et identité vérifiés

- Dépôt principal : branche `master`, HEAD `528a9dbf26a917a0d008d0f05f4b429129d1d49a`.
- Worktree A : `.benchmark-worktrees/normalize-origin-reference-3ec32d3`, HEAD détaché `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`; test instrumenté non suivi déjà présent.
- Worktree B : `.benchmark-worktrees/normalize-origin-3ec32d3`, HEAD détaché `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`; modifications préexistantes de l’optimisation dans `TextNormalizer.kt`, `OriginQualifierRules.kt`, le test sémantique et le rapport d’optimisation. Elles ont été préservées.
- État initial/final du dépôt principal : `?? .benchmark-worktrees/`, `?? reports/0.7/benchmark/COMPARISON_NORMALIZATION_NOKIA_20261010.md`, `?? reports/0.7/benchmark/COMPARISON_NORMALIZATION_NOKIA_RESULTS_20261010.md`; ce rapport s’ajoute à ces fichiers non suivis. Aucun fichier suivi modifié.
- L’unique appareil listé est `AQ5003H044Q32600084`, état ADB `device`, modèle `Nokia G42 5G`, Android `15`.
- `pm path com.example.isitvegan` et `pm path com.example.isitvegan.test` sont vides lors de cette reprise. Aucun APK n’est installé actuellement.

Les APK locales existantes ont été hashées sans les reconstruire :

| Variante | APK application SHA-256 | APK androidTest SHA-256 |
|---|---|---|
| A, worktree de référence | `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28` | `A193F93005C34F7C8391AFAA3E2637D268CE92A29FB229BD88F253921BD9191A` |
| B, worktree optimisé | `415F23B484B9F208497636C7E18B66C91435E571936C98D0518FDBCC91C1C0CD` | `A193F93005C34F7C8391AFAA3E2637D268CE92A29FB229BD88F253921BD9191A` |

Les rapports du passage A indiquent pour A et B le même package applicatif `com.example.isitvegan`, version `0.6.13.7` / code 59, ainsi que le même certificat SHA-256 `b2cf0b744229e5fd5d6f0c49e53c96ce8fc878af20190c71618f8d349f590230`. Le runner cible `com.example.isitvegan`. Aucune installation n’a été tentée durant cette enquête.

Le fichier de benchmark est identique dans les deux worktrees (SHA-256 `065A1C1DC3E9B5644315BF166923E03F5D1E4DBD26B2AFD8B4ADA1AC79D57B1F`). Les trois assets sont identiques entre A et B, comme consigné dans `COMPARISON_NORMALIZATION_NOKIA_RESULTS_20261010.md`. Aucun changement du harness ou des assets n’a été fait ici.

## Éléments sur le nettoyage

Artefacts précédents, conservés sous `.benchmark-worktrees/normalize-origin-reference-3ec32d3/app/build/benchmark-artifacts/normalization-ab-20261010/` :

- `A1-package-events.txt` contient un `installer_clear_app_data_caller` pour `com.example.isitvegan` à 14:29:32, un autre pour ce package à 14:30:01, puis un pour `com.example.isitvegan.test` à 14:30:01.
- Les piles associées traversent `Installer.clearAppData`, `AppDataHelper.clearAppDataLeafLIF` / `clearAppDataLIF`, puis `InstallPackageHelper.executePostCommitStepsLIF` et le chemin d’installation PackageManager.
- Le premier événement coïncide avec l’installation manuelle A via `adb install -r`; le second avec le déploiement de la tâche `:app:connectedDebugAndroidTest`. Ce sont des événements PackageManager observés, pas une preuve complète du contenu réellement supprimé ni de la commande ayant demandé le nettoyage.
- À la fin du test, le journal rapporte `am_kill` pour `com.example.isitvegan` avec la raison « stop … due to finished inst », puis `notification_cancel_all`. Ce sont des événements d’arrêt/nettoyage de notification, pas une preuve de désinstallation.
- `A1-package-state-after.txt` confirme qu’après le test `pm path` était vide et `dumpsys package` ne trouvait pas `com.example.isitvegan`.
- La sortie enregistrée de `connectedDebugAndroidTest` ne montre pas de commande ADB détaillée ni d’action explicite d’uninstall. Le mécanisme exact de disparition demeure donc inconnu. Aucun `adb uninstall`, `pm uninstall` ou `pm clear` n’a été exécuté manuellement par nous.

Inspection de configuration : `app/build.gradle.kts` ne configure que `androidx.test.runner.AndroidJUnitRunner`; le manifest fusionné de l’app déclare `debuggable=true`, et celui d’androidTest déclare le runner ciblant l’app. Les recherches dans les sources et manifests fusionnés n’ont trouvé ni `clearPackageData`, ni Orchestrator, ni déclaration d’effacement des données. Cela rend plausible une invocation directe du runner, mais ne démontre pas que la tâche Gradle soit la cause du retrait observé.

## Décision de sûreté et mesures

Le lancement direct envisagé, sans l’exécuter, serait le runner du manifest avec `-e class com.example.isitvegan.RealLabelMultilingualBenchmarkInstrumentedTest#profilesRealLabelTextAndDeterministicOcrVariants`. Il éviterait `connectedDebugAndroidTest` et sa phase de déploiement/nettoyage. Cependant, il faudrait d’abord installer l’APK A, puis remplacer l’app par B. Comme l’installation manuelle précédente a déjà coïncidé avec l’appel `clearAppData`, il n’est pas démontré que ce parcours respecte l’exigence « sans effacer les données ». Le package est actuellement absent, mais l’essai créerait ensuite les données de l’appareil et ne permettrait pas de garantir leur préservation lors du remplacement A/B. Le travail s’arrête donc avant toute installation.

En conséquence :

- aucune compilation Gradle n’a été lancée dans cette reprise;
- aucun APK n’a été installé, aucune donnée n’a été effacée volontairement et aucun runner n’a été lancé;
- aucun résultat A/B n’existe : le seul passage A historique reste un résultat isolé, non comparable à B;
- aucune capture Simpleperf/Perfetto n’a été lancée et aucun nouveau hotspot ne peut être attribué;
- l’application B n’est pas installée sur le Nokia.

Les anciennes données A (médiane warm de l’analyse complète autour de 10,51 s et parsing autour de 5,59 s) restent des références A seules, sans interprétation de gain. Pour reprendre sans risque, il faut d’abord établir avec le propriétaire de l’appareil ou une configuration Android contrôlée pourquoi l’installation appelle `clearAppData`, puis choisir un dispositif de test qui conserve les données et le package. Tant que ce point n’est pas expliqué, ne pas relancer A/B ni le profil du worker B.

## Commandes exécutées dans cette reprise

Lectures Git (`git status`, `git rev-parse`, `git worktree list`), recherches `rg`, consultation des rapports et manifests fusionnés, calcul local des SHA-256 des APK existantes, `adb devices -l`, puis lectures ciblées `getprop ro.product.model`, `getprop ro.build.version.release`, `pm path` pour les deux packages. Aucun test ou build n’a été exécuté. Les APK, logs, rapports, worktrees et données existants ont été conservés.
