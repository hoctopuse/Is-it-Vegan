# Inspection PIT / mutation testing

Date de l’inspection : 2026-09-25 (heure locale Europe/Brussels ; l’horodatage exact n’a pas été conservé par la commande d’inspection).

## 1. Périmètre et état initial

- Dépôt : `C:\Users\Sebastien\StudioProjects\Is-it-Vegan`
- Branche : `master`
- Dernier commit observé : `fdb4915e29bc12d85f8753a156734968f4751170` — `feat(ocr): fiabiliser le pipeline multilingue et le parsing en 0.6.8.1`
- `git status --short` initial : sortie vide.
- `git diff --stat` initial : sortie vide.
- `git diff --name-only` initial : sortie vide.
- Instructions applicables : [`AGENTS.md`](AGENTS.md), notamment préservation des changements, exécution locale des tests Kotlin concernés, exclusion des répertoires générés et absence d’expansion de la base d’ingrédients.

Le seul changement durable effectué pendant cette inspection est la création de ce fichier.

## 2. Faits observés dans la configuration

Le projet est un seul module Android : `:app`, inclus par [`settings.gradle.kts`](settings.gradle.kts). Le module applique `com.android.application` et `org.jetbrains.kotlin.plugin.compose`; aucun module `java`, `java-library`, JVM autonome ou source set PIT n’est déclaré.

Versions déclarées :

| Élément | Valeur observée |
|---|---|
| Version applicative | `0.6.8.1`, code `35` |
| AGP | `9.4.1` |
| Gradle wrapper | `9.7.1` |
| Kotlin | `2.4.20` |
| Java de compilation | source/target `17` |
| compileSdk / targetSdk | `37 / 37` |
| minSdk | `30` |
| JUnit | `4.13.2` |
| Configuration | `org.gradle.configuration-cache=true`, daemon JVM toolchain version `25` déclarée |

La version Java réellement utilisée par Gradle n’a pas pu être détectée : `JAVA_HOME` est vide et `java` n’est pas disponible dans le `PATH` de l’environnement d’inspection. La configuration du projet demande Java 17 pour la compilation, mais le fichier `gradle-daemon-jvm.properties` décrit une résolution de toolchain version 25 ; cela devra être vérifié sur une machine équipée d’un JDK.

Source sets observés :

- production : `app/src/main/java` et ressources/assets `app/src/main` ;
- tests JVM : `app/src/test/java` ;
- tests instrumentés : `app/src/androidTest/java` et assets OCR associés ;
- aucun source set `integrationTest` ou source set dédié à PIT.

Tâches Gradle et variantes : **NON VERIFIE**. La commande `gradlew tasks --all` s’est arrêtée avant le démarrage de Gradle à cause de l’absence de Java. D’après la configuration, les variantes Android attendues sont au minimum `debug` et `release`, avec les tâches de tests usuelles `testDebugUnitTest` et `connectedDebugAndroidTest`, mais leur présence effective n’a pas été exécutée.

## 3. Code métier et dépendances Android

Le dépôt contient 38 fichiers Kotlin de production. Les composants métier repérés sont :

- analyse et verdict : `VeganAnalyzer`, `VerdictEngine`, `UnknownCollector`, `DiagnosticReport`, `CrossContactNotice` ;
- parsing : `IngredientTokenizer`, `IngredientTreeParser`, `IngredientMatcher`, `TextNormalizer`, `QuantityCleaner` ;
- sections et langues : `LabelLanguageSegmenter`, `LabelSectionExtractor`, `LabelPreprocessor`, `LabelLexicon`, `OcrBlockLanguageClassifier`, `MultilingualIngredientLexicon` ;
- OCR indépendant ou quasi indépendant : `OcrTextCleaner`, `OcrTextReconstructor`, `OcrTextSelection`, `OcrImageSizing`, `OcrCropGeometry`, `OcrModels`, `OcrSession`, `OcrThreading`, `OcrIngredientNormalizer` ;
- connaissances et matching : `ingredients.kt`, `FunctionalClassLexicon`, `OriginQualifierRules`.

Les imports Android/AndroidX/ML Kit sont présents directement dans `VeganAnalyzer`, `UiLanguage`, les écrans Compose, les composants image/OCR, `OcrProcessor` et le thème. `VeganAnalyzer` charge aussi la base via `Context`. Les tests JVM l’utilisent néanmoins avec les chemins d’analyse qui acceptent une base déjà fournie ; ce point indique une séparation fonctionnelle utile, mais ne prouve pas que les classes produites par AGP soient directement acceptées par PIT.

Les candidats les plus clairement purs au niveau des imports sont `IngredientTokenizer`, `IngredientTreeParser`, `IngredientMatcher`, `LabelLanguageSegmenter`, `LabelSectionExtractor`, `LabelPreprocessor`, `VerdictEngine`, `UnknownCollector`, les lexiques, les modèles OCR et plusieurs utilitaires. Ils restent compilés dans le module Android et ne constituent donc pas, à eux seuls, un module JVM indépendant.

Les comportements explicitement couverts comprennent le parsing/arbre d’ingrédients, le matching et les alias, les verdicts vegan/végétarien/incertain, les traces et mentions « contient », la protection de « beurre de cacao », `LabelLanguageSegmenter`, `LabelSectionExtractor`, le pipeline OCR et les diagnostics OCR. Ces constats viennent des noms de tests et de leur contenu inspecté ; ils ne constituent pas une mesure de couverture mutationnelle.

## 4. Tests existants

Les tests JVM sont dans le package `com.example.isitvegan`, utilisent JUnit 4 (`org.junit.Test`, `org.junit.Assert`) et comptent 35 fichiers / 267 méthodes annotées `@Test` selon le comptage local. La liste exacte est :

`CrossContactTest`, `HierarchyAnalysisTest`, `IngredientTreeParserTest`, `LabelLanguageHotfixTest`, `LabelLanguageSegmentationHotfixTest`, `LabelLanguageSegmenterTest`, `LabelSectionExtractorTest`, `OcrCropGeometryTest`, `OcrImageSizingTest`, `OcrLanguageZonesTest`, `OcrMultilingualSelectionTest`, `OcrNativeOrderRegressionTest`, `OcrSessionTest`, `OcrTextCleanerTest`, `OcrTextReconstructorTest`, `OcrThreadingTest`, `ParserModulesTest`, `UiLanguageTest`, `UnknownCollectionHotfixTest`, `VeganAnalyzerTest`, `Version05101Test`, `Version0510Test`, `Version0511Test`, `Version0591Test`, `Version059CompletionTest`, `Version064LanguageTest`, `Version065OcrPipelineTest`, `Version066KnowledgeEnrichmentTest`, `Version066MultilingualLexiconTest`, `Version066MultilingualMatchingTest`, `Version066OcrNormalizationTest`, `Version066TraceLanguageTest`, `Version067OcrStabilizationTest`, `Version0681ChocolateLabelTest`, `Version068RobustnessTest`.

Les tests JVM n’importent pas directement Android, Compose ou ML Kit dans les fichiers de test inspectés. `OcrThreadingTest` utilise également les coroutines. En revanche, plusieurs classes appelées transitivement appartiennent au module Android et `VeganAnalyzer` importe `android.content.Context`. La capacité exacte de PIT à exécuter ces tests avec les classes `debug` d’AGP est donc **NON VERIFIEE**.

Les cinq tests instrumentés observés sont `Version0511InstrumentedTest`, `RealLabelsInstrumentedTest`, `OcrTextCleanerInstrumentedTest`, `OcrFixturesInstrumentedTest` et `MainScreenInstrumentedTest`. Ils nécessitent un appareil/émulateur Android et AndroidX/Compose ; ils ne sont pas des candidats directs pour une campagne PIT JVM.

## 5. Baseline

Commande tentée : `./gradlew test` via `gradlew.bat test` (équivalent Windows disponible).

Résultat : `BLOQUE_ENVIRONNEMENT`. Durée observée : environ 4 secondes jusqu’à l’échec du lanceur. Nombre de tests exécutés : 0. Erreur bloquante : `JAVA_HOME is not set and no 'java' command could be found in your PATH.`

Il s’agit d’un problème de l’environnement d’inspection, avant configuration ou compilation du projet. Il n’est pas possible d’en déduire que les tests du projet échouent. La baseline devra être relancée avec un JDK compatible, idéalement Java 17 ou la toolchain effectivement validée pour AGP/Gradle.

## 6. Comparaison PIT

### `info.solidsoft.pitest`

Statut : **INCOMPATIBLE** pour le module Android actuel en intégration directe.

La documentation officielle du plugin indique une application avec `java` ou `java-library`, des `sourceSets.main`/`sourceSets.test`, et précise que le plugin standard ne prend pas directement en charge les applications Android à cause des différences entre projets Java et Android. La FAQ renvoie vers le fork Android `pl.droidsonroids.pitest` ([dépôt et FAQ du plugin](https://github.com/szpak/gradle-pitest-plugin)).

Il ne peut donc pas être appliqué simplement à `:app` ni être supposé consommer `testDebugUnitTest`. Pour le rendre utilisable, il faudrait soit extraire les classes métier dans un module JVM, soit construire une configuration non standard de classes compilées, tests, classpath Android et rapports. Cela nécessiterait au minimum une modification de configuration Gradle et une validation sous AGP 9.4.1 / Gradle 9.7.1 / Kotlin 2.4.20 / Java 17. Le niveau de confiance pour une adaptation directe est faible.

### `pl.droidsonroids.pitest`

Statut : **COMPATIBLE_AVEC_ADAPTATION**, avec confiance faible à moyenne.

Le plugin est explicitement présenté comme un plugin PIT pour projets Android. Le portail Gradle répertorie actuellement la version `0.2.27`, publiée le 24 mars 2026, et renvoie au dépôt source ([fiche Gradle Plugin Portal](https://plugins.gradle.org/plugin/pl.droidsonroids.pitest), [dépôt GitHub](https://github.com/koral--/gradle-pitest-plugin)). Cette information documente l’intention Android du plugin, mais ne constitue pas une preuve de compatibilité avec AGP `9.4.1`, Gradle `9.7.1`, Kotlin `2.4.20` ou Java 17 : aucune campagne ni tâche n’a été exécutée ici.

L’adaptation attendue serait l’ajout du plugin à la configuration des plugins, puis une configuration d’une variante Android et de ses tests unitaires. Le nom exact de la tâche, la variante supportée, le chemin des classes mutables et le comportement avec Kotlin 2.4/AGP 9.4 doivent être vérifiés dans une branche ou un worktree temporaire. Les tests instrumentés et Compose restent hors du périmètre PIT JVM. Les rapports HTML/XML sont attendus par PIT, sous réserve que la tâche du plugin arrive à construire le classpath Android.

Le risque principal est l’obsolescence ou la couverture partielle des versions modernes AGP/Gradle : le projet contient une configuration très récente, tandis que le plugin est une adaptation spécialisée dont la compatibilité exacte n’est pas garantie par la seule présence au portail.

### PIT limité aux classes JVM et aux tests unitaires

Statut : **COMPATIBLE_AVEC_ADAPTATION**.

L’approche la plus réaliste consiste à cibler la logique pure déjà exercée par `app/src/test/java`, en particulier parsing, matching, segmentation, extraction de sections, verdicts et diagnostics. Elle ne nécessite pas de muter les écrans, CameraX/Compose, les bitmaps, ML Kit ou les tests instrumentés.

Cependant, dans l’état actuel, ces classes sont produites par un module Android et certaines API transitives (`Context`, ressources/assets ou types Android) empêchent de considérer l’ensemble comme JVM pur. Deux variantes sont possibles : utiliser le plugin Android spécialisé pour la variante de test JVM, ou extraire ultérieurement un module JVM. La seconde option augmente fortement le changement d’architecture et n’est pas recommandée pour une première campagne.

Le risque de faux sentiment de couverture est réel : les mutants des chemins d’acquisition OCR, UI, ressources et chargement Android ne seront pas mesurés. Le rapport devra donc annoncer explicitement un périmètre métier JVM partiel.

### Autres options

Aucune autre solution locale déjà présente ou configurée n’a été trouvée. Les outils de mutation ne figurent pas dans les fichiers Gradle, le catalogue de versions ou les scripts du dépôt. Les solutions générales qui exigeraient l’installation d’un nouvel outil ou plugin sont **A ECARTER** à cette étape ; elles contreviendraient à la contrainte d’absence d’installation et n’offriraient pas un gain établi par rapport au plugin Android dédié.

## 7. Tableau de décision

| Option | Statut | Classes/tests ciblables | Modifications nécessaires | Confiance |
|---|---|---|---|---|
| `info.solidsoft.pitest` direct sur `:app` | INCOMPATIBLE | aucune démontrée | configuration non standard ou extraction JVM | faible |
| `pl.droidsonroids.pitest` sur Android | COMPATIBLE_AVEC_ADAPTATION | variante Android et tests JVM, à vérifier | plugin + configuration Gradle | faible à moyenne |
| PIT sur périmètre métier JVM | COMPATIBLE_AVEC_ADAPTATION | parsing, matching, langues, sections, verdicts, diagnostics testables | configuration du plugin Android ou extraction JVM | moyenne après baseline verte |
| autre outil | A ECARTER | non établi | installation/configuration supplémentaire | faible |

## 8. Coût et reproductibilité

Le coût CPU/mémoire d’une campagne PIT dépendra du nombre de classes mutées, du nombre de mutants et du parallélisme. Le code métier contient une suite de 267 tests, ce qui rend probable un coût supérieur à une simple suite JUnit, mais permet à PIT de sélectionner et prioriser les tests si son intégration Android fonctionne. Il faut prévoir au moins la compilation Android, le démarrage d’un processus JVM PIT par campagne et les rapports HTML/XML sous `build`; aucune durée réelle n’est estimée de façon fiable sans baseline et sans tâche PIT.

Le téléchargement initial comprendrait le plugin, PIT et ses dépendances, mais n’a pas été effectué. Une campagne locale déterministe peut réduire l’intervention IA à l’analyse des mutants survivants. Le plugin Android dédié est le chemin demandant le moins d’extraction de code ; une extraction en module JVM serait plus reproductible techniquement mais plus coûteuse et plus risquée architecturalement.

## 9. Recommandation

Recommandation principale : **essayer ultérieurement `pl.droidsonroids.pitest` sur la variante de tests unitaires Android, avec un périmètre explicitement limité aux classes métier et OCR pur déjà couvertes par `app/src/test`**. Ce choix minimise les modifications de structure et correspond au type de module réel.

Solution de repli : si le plugin Android échoue avec AGP `9.4.1` ou Gradle `9.7.1`, créer dans un worktree temporaire un prototype de module JVM séparé contenant uniquement les composants purs et leurs tests nécessaires, puis utiliser `info.solidsoft.pitest` dans ce prototype. Cette solution mesure une tranche métier utile, mais elle doit être décrite comme un périmètre extrait et non comme la mutation coverage de toute l’application.

Niveau de confiance global : **moyen-faible**. La compatibilité de principe est documentée pour le plugin Android, l’incompatibilité directe du plugin standard avec Android est documentée, mais la combinaison exacte de versions et la disponibilité des tâches n’ont pas pu être vérifiées faute de JDK.

## 10. Étapes pour une future campagne réelle

1. Fournir un JDK compatible et relancer `gradlew test`, puis relever les tâches et variantes réelles.
2. Dans une branche ou un worktree temporaire, ajouter uniquement le plugin Android PIT et sa configuration minimale.
3. Vérifier la résolution Gradle, la compilation de `testDebugUnitTest`, le nom de la tâche PIT et le classpath des classes Kotlin produites.
4. Faire un premier run très ciblé sur `IngredientTreeParser`, `IngredientMatcher`, `LabelLanguageSegmenter`, `LabelSectionExtractor`, `VerdictEngine` et `VeganAnalyzer` si ce dernier est chargeable sans `Context`.
5. Produire HTML et XML avec un parallélisme borné par la mémoire locale.
6. Vérifier que les rapports distinguent bien les mutants hors périmètre Android et conserver la baseline avant d’analyser les survivants.

## 11. Points à vérifier par l’agent suivant

- JDK réellement sélectionné par Gradle et compatibilité AGP 9.4.1 / Gradle 9.7.1.
- Résultat et durée de `test`, `testDebugUnitTest` et, si présent, des variantes release.
- Tâche exacte créée par `pl.droidsonroids.pitest:0.2.27` et compatibilité de sa configuration Kotlin DSL.
- Classes effectivement découvertes et mutées, notamment les classes Kotlin compilées par AGP.
- Dépendances Android transitives de `VeganAnalyzer`, `UiLanguage`, `OcrSession` et des diagnostics.
- Reproductibilité des rapports sur Windows et gestion des chemins longs/classpath.
- Nombre de mutants, durée par classe, consommation mémoire et stabilité des tests.

## 12. Actions volontairement non effectuées

- aucun plugin ou dépendance installé ;
- aucune modification de `build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `libs.versions.toml` ou `local.properties` ;
- aucun code de production, test, asset, ressource, alias ou base d’ingrédients modifié ;
- aucune mutation PIT lancée ;
- aucun fichier existant supprimé ou restauré ;
- aucune branche, commit ou push créé ;
- aucun test instrumenté lancé ;
- aucun répertoire `build`, `.gradle`, `.idea`, `.externalNativeBuild` ou `.cxx` analysé dans le rapport.

