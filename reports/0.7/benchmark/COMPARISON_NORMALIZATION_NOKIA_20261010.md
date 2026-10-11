# Comparaison Android de l’optimisation de normalisation — 10 octobre 2026

## Résultat

Aucune durée A/B n’a été mesurée. Le Nokia est identifié et les worktrees, le test et les trois assets sont prêts et identiques. Le test instrumenté charge le code métier depuis `com.example.isitvegan`, donc depuis l’APK cible installée. Cette APK est la référence non optimisée. Or la consigne de ce passage interdit l’installation d’une APK applicative de production. Installer uniquement l’APK androidTest ne peut donc pas faire exécuter le code optimisé. Lancer le test dans ces conditions mesurerait deux fois le moteur de référence et produirait un résultat trompeur.

Il faut autoriser explicitement l’installation successive des APK debug de l’application A et B, ou convenir d’un harness Android différent qui charge réellement chaque variante sans remplacer l’APK cible. Aucune commande de build, installation, instrumentation ou chronométrage n’a été lancée dans ce passage.

## État Git et sources

- Référence A : worktree détaché `.benchmark-worktrees/normalize-origin-reference-3ec32d3`, commit `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`, créé pour ce passage depuis un chemin absent.
- Optimisée B : `.benchmark-worktrees/normalize-origin-3ec32d3`, même HEAD et mêmes changements préexistants : `TextNormalizer.kt`, `OriginQualifierRules.kt`, `NormalizationSemanticsTest.kt` et le rapport `OPTIMIZATION_NORMALIZATION_ORIGIN_20261009.md`.
- Dépôt principal : branche `master`, HEAD `528a9dbf26a917a0d008d0f05f4b429129d1d49a`; état initial `?? .benchmark-worktrees/`, aucun changement suivi staged/unstaged. Le répertoire contient les worktrees existants et ceux créés pour ce travail.
- Le diff de production B contre A est exclusivement le patch des deux fichiers Kotlin décrit dans le rapport d’optimisation : trois Regex réutilisées et calcul local des valeurs normalisées invariantes dans `extractAttached`. Aucun asset ni code métier supplémentaire n’a été modifié.

## Harness et corpus

Harness copié sans adaptation depuis le worktree post-import : `RealLabelMultilingualBenchmarkInstrumentedTest.kt`. SHA-256 dans A, B et source : `065A1C1DC3E9B5644315BF166923E03F5D1E4DBD26B2AFD8B4ADA1AC79D57B1F`.

Les trois SHA-256 suivants concordent entre A et B :

| Asset | SHA-256 |
|---|---|
| `app/src/main/assets/ingredients.json` | `78DCCB4B02DBFC14921FCF40C62228FBF5DCDB8294B63A1C2DCDAE05FD1A3825` |
| `app/src/main/assets/ingredient_aliases_multilingual.json` | `3952C31957690A2D998DC2A2BAE740E01D434824FE31EA01FD555D1AA52992E2` |
| `app/src/main/assets/origin_qualifier_rules.json` | `77E6E6F436F11666F659EA4BD70F45B02CEBE89AE940B9077567891286F9D40E` |

Le harness fournit le texte anglais propre et quatre variantes déterministes. Il mesure chargement d’assets et de connaissance, construction du matcher, analyses complètes (première invocation puis cinq échauffements et dix échantillons), extraction de section et parsing. Il assert les champs d’entrée, section anglaise, arbre et mappings attendus hors des intervalles mesurés. La méthode ciblée est `RealLabelMultilingualBenchmarkInstrumentedTest#profilesRealLabelTextAndDeterministicOcrVariants`. Le test présent dans A et B est identique octet par octet. Aucun test JVM de profilage n’est utilisé comme mesure Android.

## Préflight Nokia en lecture seule

Commande : `C:\platform-tools\adb.exe devices -l`, puis commandes ciblées avec `-s AQ5003H044Q32600084`.

- ADB : `device`; modèle `Nokia G42 5G`; Android 15.
- Batterie avant tout passage : niveau 100 %, USB alimenté, AC non alimenté; `dumpsys battery` indique 21,0 °C. `dumpsys thermalservice` indique Thermal Status 0 et une température batterie de 28,3 °C; les deux interfaces donnent ici des lectures différentes. Les températures CPU signalées sont approximativement 55–60 °C.
- Package présent : `com.example.isitvegan`, version `0.6.13.7`, versionCode 59. `pm path` donne `/data/app/~~eo1eB9jUylWEx8_sWsbBxw==/com.example.isitvegan-fEKlaEKkzmi78AL-EBeAzQ==/base.apk`.
- SHA-256 calculé sur l’APK installée : `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`. Ce hash est celui de l’APK post-import de référence déjà documentée. Aucune APK optimisée n’existe dans les nouveaux worktrees, et aucune n’a été installée.

Ces relevés ne décrivent qu’un préflight; ils ne sont pas des conditions avant/après passage. Aucun ordre A/B ou B/A n’a été exécuté. Aucun XML JUnit, logcat de test ou résultat temporel n’a été produit. Les anciens résultats du rapport post-import et la baseline du 2 octobre ne sont pas des comparaisons de ce patch : ils utilisent d’autres révisions ou protocoles et ne sont pas repris comme chiffres A/B.

## Commandes et suites

Commandes réellement exécutées : vérification de statut/HEAD/worktrees, `git worktree add --detach` pour A, inspection du diff B, copie du seul fichier de test vers A et B, calcul des SHA-256 du test et des assets, vérification d’identité ADB et lectures `getprop`, `dumpsys battery`, `dumpsys thermalservice`, `pm path`, `dumpsys package` et `sha256sum` du package installé.

Pas de compilation Gradle dans ce passage, aucun test JUnit exécuté, aucun benchmark Android, aucune capture Simpleperf/Perfetto. Les deux échecs connus de `mutation-core` ne sont ni relancés ni investigués.

## Suite requise et conclusion

Le patch paraît cohérent avec le hotspot et ses tests fonctionnels précédents passent, mais sa conservation pour un gain de performance reste à confirmer sur Android. Le protocole existant doit être exécuté avec le même runner et le même harness sur A puis B, puis dans l’ordre inverse si la température reste comparable; collecter les séries brutes pour construction matcher, parsing et analyse complète, première invocation, cinq échauffements et dix mesures warm, assertions JUnit et relevés thermiques/batterie avant/après. Comparer les médianes suivant la définition du harness avec min/max et dispersion. Ne pas déduire un gain d’un écart comparable au bruit.

Cette exécution exige le remplacement temporaire de l’APK applicative par les builds debug A et B. Tant que ce remplacement reste interdit, le résultat correct est « comparaison bloquée », et non une mesure proxy. Aucun changement d’APK installée, aucune capture, aucune modification d’asset, aucun commit ou push n’a eu lieu. Les worktrees de référence et optimisé ainsi que les artefacts d’optimisation antérieurs sont conservés.
