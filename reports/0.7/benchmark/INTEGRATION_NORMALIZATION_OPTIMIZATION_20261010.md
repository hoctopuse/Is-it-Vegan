# Intégration de l’optimisation de normalisation — 10 octobre 2026

## Révision et périmètre

Le dépôt principal était sur `master`, HEAD `528a9dbf26a917a0d008d0f05f4b429129d1d49a`. Le commit de départ `3ec32d3914ad071b8435b9155e2e7d6467aeebd1` en est un ancêtre. Les deux fichiers de production étaient identiques au commit de départ avant intégration. Le worktree optimisé `.benchmark-worktrees/normalize-origin-3ec32d3` reste détaché sur ce commit et conserve ses changements préexistants ; aucun worktree n’a été supprimé ou modifié.

Le diff de production transplanté est limité à :

- `mutation-core/src/main/kotlin/com/example/isitvegan/TextNormalizer.kt` : réutilisation des trois `Regex` immuables déjà mesurées.
- `mutation-core/src/main/kotlin/com/example/isitvegan/OriginQualifierRules.kt` : normalisation de l’entrée, des cibles et qualificatifs invariants hors de leurs boucles respectives, sans changer ordre, comparaison ou diagnostics.
- `mutation-core/src/test/kotlin/com/example/isitvegan/NormalizationSemanticsTest.kt` : ajout du test sémantique qui compare les sorties complètes de qualification et couvre accents, ligatures, marque combinante, casse, ponctuation, espaces, chaînes vides, nombres E, parenthèses et qualificatifs FR/EN/NL/DE/ES. Aucun test équivalent n’existait dans `mutation-core/src/test`.

Les deux optimisations ont été évaluées comme un seul patch. Les mesures A/B Nokia rapportées ci-dessous ne permettent pas d’attribuer une fraction du gain à chaque changement. Le profil CPU Simpleperf post-patch n’a pas été réalisé ; aucune fonction dominante après optimisation n’est connue.

## Preuves Android archivées

Le rapport final `COMPARISON_NORMALIZATION_AB_AND_PROFILE_20261010.md` remplace, pour la conclusion A/B, le rapport A seul `COMPARISON_NORMALIZATION_NOKIA_20261010.md` et le rapport intermédiaire `COMPARISON_NORMALIZATION_NOKIA_RESULTS_20261010.md`. Ces rapports historiques et `INSTALL_CLEANUP_INVESTIGATION_20261010.md` sont conservés sans modification.

L’archive compacte est `reports/0.7/benchmark/evidence-normalization-ab-20261010/`. Elle contient le `comparison-summary.json` avec les mesures brutes des quatre passages, le manifeste SHA-256 de la collecte d’origine, les quatre sorties AndroidJUnitRunner et les relevés batterie/thermiques avant/après A1, B1, B2 et A2. Le manifeste de cette sélection est `ARCHIVE_SHA256SUMS.txt`; ses hashes ont été recalculés après copie. Les originaux restent dans les dossiers `app/build/benchmark-artifacts/normalization-ab-direct-20261010/` des worktrees A et B. Aucun APK, `perf.data`, capture complète, image ou worktree n’est archivé dans Git.

La référence A et la variante B ont été comparées sur Nokia G42 5G, Android 15, avec le même test instrumenté, mêmes chaînes/assets, debug runner, cinq échauffements et dix mesures warm par scénario. Pour l’analyse complète du texte corrigé, les médianes de passage étaient A1 10,651817 s, B1 1,071226 s, A2 10,578593 s et B2 1,075168 s, soit environ 89,9 % de réduction appariée. Pour le parsing seul : A1 5,657424 s, B1 0,123615 s, A2 5,622978 s et B2 0,123876 s, soit environ 97,8 %. Les variantes OCR déterministes suivent le même profil. Les assertions et tests Android instrumentés ont réussi dans les quatre passages. Le rapport final décrit la dispersion, les conditions thermiques et les limites d’attribution.

SHA-256 du rapport d’optimisation archivé : `7C20DA97D51AC229A47933D48D62109848226F1A3CB4E46F511426A1BC6E91F4`. SHA-256 du rapport A/B final : `C43F36828E9CDDF3AF192506FAFE6470716F5767D627988221A0A7C91583CB66`. Dans l’archive, `comparison-summary.json` a pour hash `047C3F936893E8DC2A7FB4A7C6F7B4FDC2572B556F9E64888648F10F4ECED266` et le manifeste source `source-artifact-sha256.txt` a pour hash `9EBA2137FFB7B2ADF1B1909BE6BEAD87D4BA2ADBC11AC8E89DF762BAEA3738B4`. Le manifeste recalculé des preuves archivées `ARCHIVE_SHA256SUMS.txt` a pour hash `929FC3DDABA3979FD0922B3925D482BB2BE1090FD90BA2F75ED51F1522926A76`.

Ces résultats établissent le gain pour ce harness et ce texte sur cet appareil. Ils ne constituent pas une mesure du Pixel. Le rapport Simpleperf post-patch est bloqué au précontrôle du hash d’APK et ne fournit aucune preuve CPU après optimisation.

## Validation locale de l’intégration

Commandes exécutées avec le wrapper Gradle Windows, hôte Lenovo/Windows :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\gradlew.bat :mutation-core:test --tests '*NormalizationSemanticsTest' --tests '*ParserModulesTest' --tests '*IngredientTreeParserTest' --tests '*Version059CompletionTest' --tests '*Diagnostic*Test' :app:testDebugUnitTest --tests '*Version0591Test' --console=plain
.\gradlew.bat :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin --console=plain
```

Les deux commandes ont terminé avec `BUILD SUCCESSFUL` (tests ciblés : 54 s; compilations : 12 s). Aucune suite complète n’a été lancée. Les deux échecs connus sur la suite complète `:mutation-core` n’ont pas été relancés ni corrigés : `KnowledgeIndexTest.currentJsonConvertsAllConceptsAndMappingsWithoutChangingIdentity` (482 attendus, 488 obtenus) et `Phase4MatcherProfileTest.linkedAliasPrecomputationIsEquivalentToTheFormerPairwisePredicate` (2485 attendus, 2574 obtenus), précédemment reproduits sur le commit vierge.

L’APK optimisée actuellement installée sur le Nokia n’a pas été touchée. Aucun appel ADB, installation, profilage, commit distant ou push n’a eu lieu dans cette intégration.

## État du dépôt

Le commit local ciblé contient uniquement les deux fichiers Kotlin de production, le test sémantique, `.gitignore`, les rapports finaux d’optimisation et A/B, ce rapport d’intégration et les preuves compactes listées dans leur manifeste. Il ne contient aucun changement de données ou d’assets, fichier OCR/image, APK, capture volumineuse, test instrumenté, script benchmark ou contenu de worktree. Le commit local et le statut final sont consignés dans le compte rendu de la tâche.
