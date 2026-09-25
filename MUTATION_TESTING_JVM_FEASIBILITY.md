# Inspection de faisabilité — Module JVM pour PIT

## 1. Objectif et statut

Cette inspection évalue un futur module JVM pour PIT classique sur le métier pur d’**Is It Vegan?**. Aucun module, plugin, dépendance, fichier Gradle ou source n’a été modifié et PIT n’a pas été lancé.

Statut global : `RECOMMANDE` pour une extraction permanente progressive du cœur métier. Le harness temporaire est `JVM_AVEC_ADAPTATION` et ne doit pas servir de mesure finale de qualité.

## 2. État initial et versions

Commandes initiales : `git status --short`, `git diff --stat`, `git diff --name-only`.

État observé : seuls `MUTATION_TESTING_INSPECTION.md` et `MUTATION_TESTING_PIT_PROTOTYPE.md` apparaissaient comme fichiers non suivis. Les différences suivies étaient vides. Ces deux rapports sont restés inchangés.

Versions observées : module `:app`, AGP `9.4.1`, Gradle `9.7.1`, Kotlin `2.4.20`, compilation Java 17, JBR Android Studio Java 25 utilisé par Gradle, JUnit `4.13.2`. La baseline `.\gradlew testDebugUnitTest --rerun-tasks` a réussi (`BUILD SUCCESSFUL`).

## 3. Inventaire des classes candidates

| Composant | Statut | Dépendances observées | Tests / adaptation |
|---|---|---|---|
| `IngredientTreeParser` | `JVM_PUR` | Kotlin, `BigDecimal` | `IngredientTreeParserTest`, hiérarchie, versions ; réutilisable |
| `IngredientTokenizer` | `JVM_PUR` | Kotlin, parser | `ParserModulesTest` ; réutilisable |
| `IngredientMatcher` | `JVM_PUR` | modèles, normalisation, lexiques | matching/verdict ; réutilisable après extraction des modèles |
| `TextNormalizer`, `QuantityCleaner` | `JVM_PUR` | `java.text.Normalizer` ou Kotlin standard | tests indirects ; réutilisables |
| `VerdictEngine`, `UnknownCollector` | `JVM_PUR` | modèles et statuts | `VeganAnalyzerTest`, inconnus, versions ; réutilisables |
| `LabelLanguageSegmenter` | `JVM_PUR` | lexiques et modèles | tests de langue/hotfixes ; réutilisable si l’enum `UiLanguage` est séparé de ses préférences Android |
| `LabelSectionExtractor`, `LabelPreprocessor` | `JVM_PUR` | segmenter, lexiques, parser | tests sections et pipeline ; réutilisables |
| `LabelLexicon`, `FunctionalClassLexicon` | `JVM_PUR` | normalisation, Kotlin standard | tests parser et `Version0510Test` ; réutilisables |
| `MultilingualIngredientLexicon` | `JVM_PUR` | normalisation, `MiniJson`, modèles | tests multilingues ; réutilisable avec JSON injecté |
| `OriginQualifierRules`, `MiniJson` | `JVM_PUR` | Kotlin standard et modèles | tests hiérarchie/connaissance/chocolat ; réutilisables avec texte JSON fourni |
| `OcrTextCleaner`, `OcrTextReconstructor`, `OcrTextSelection` | `JVM_PUR` | Kotlin, `kotlin.math`, modèles OCR | tests OCR purs ; réutilisables |
| `OcrImageSizing`, `OcrCropGeometry`, `OcrModels` | `JVM_PUR` | Kotlin standard, data classes | tests géométrie/taille ; réutilisables |
| `OcrIngredientNormalizer`, `OcrBlockLanguageClassifier` | `JVM_PUR` | lexiques et normalisation | tests OCR/langues ; réutilisables |
| `OcrSession` | `JVM_PUR` | modèles et rapports | `OcrSessionTest` partiellement ; découper l’export si nécessaire |
| `OcrThreading` | `JVM_AVEC_ADAPTATION` | `kotlinx.coroutines` | test dédié ; isoler l’orchestration asynchrone |
| `VeganAnalyzer` | `JVM_AVEC_ADAPTATION` | `Context`, `org.json.JSONArray`, assets et tout le cœur | nombreux tests ; scinder chargement Android et service pur |
| `UiLanguagePreferences` | `ANDROID_DEPENDANT` | `Context`, `Configuration` | garder dans `:app`; l’enum `UiLanguage` est extractible |
| `OcrBitmapCropper` | `ANDROID_DEPENDANT` | `android.graphics.Bitmap` | garder dans `:app` |
| `OcrImageInputFactory` | `ANDROID_DEPENDANT` | `Context`, Bitmap, Uri, ExifInterface, ML Kit | garder dans `:app` |
| `OcrProcessor` | `ANDROID_DEPENDANT` | Context, Bitmap, Uri, ML Kit, coroutines | garder dans `:app` |
| `OcrFirstScreen`, `OcrCropDialog`, `MainActivity`, `ui/theme/*` | `ANDROID_DEPENDANT` | Compose, Activity, Android | garder dans `:app` |
| `CrossContactNotice`, `DiagnosticReport` | `JVM_AVEC_ADAPTATION` | diagnostics et modèles | extractibles avec le noyau de diagnostic |

Autres classes réellement découvertes : modèles d’analyse, enums de statuts, lexiques et utilitaires OCR. Aucun import Android direct n’a été observé dans le groupe `JVM_PUR`.

## 4. Android, assets et dépendances transitives

Les imports Android directs sont concentrés dans `VeganAnalyzer`, les préférences de langue, la fabrique d’images, le processeur ML Kit, les bitmaps et l’UI Compose. ML Kit est utilisé par `OcrImageInputFactory` et `OcrProcessor`. CameraX n’est pas importé directement dans les fichiers inspectés ; la chaîne UI/image reste néanmoins Android.

Le métier dépend des assets `ingredients.json`, `ingredient_aliases_multilingual.json` et `origin_qualifier_rules.json`. Plusieurs tests lisent `src/main/assets/...` via `File`, ce qui devra devenir une ressource de test JVM ou une injection explicite. Il ne faut pas extraire seulement cinq classes sans leurs modèles, alias, lexiques et règles d’origine.

## 5. Tests réutilisables

Le répertoire `app/src/test/java` contient environ 300 tests JUnit 4. Les groupes les plus réutilisables sont :

- parsing et hiérarchie : `IngredientTreeParserTest`, `HierarchyAnalysisTest`, `ParserModulesTest` ;
- langue et sections : `LabelLanguageSegmenterTest`, les deux hotfixes de langue et `LabelSectionExtractorTest` ;
- matching, verdict, traces et inconnus : `VeganAnalyzerTest`, `CrossContactTest`, `UnknownCollectionHotfixTest`, `Version0510Test`, `Version0511Test`, `Version0591Test` ;
- OCR pur : `OcrTextCleanerTest`, `OcrTextReconstructorTest`, `OcrImageSizingTest`, `OcrCropGeometryTest`, `OcrLanguageZonesTest`, `OcrMultilingualSelectionTest`, `OcrNativeOrderRegressionTest` ;
- régressions : `Version064LanguageTest` à `Version0681ChocolateLabelTest`.

`TEST_REUTILISABLE` s’applique aux tests appelant directement le cœur pur après extraction. `TEST_NON_REUTILISABLE` s’applique aux tests dépendant de `Context`, aux chemins d’assets Android, à l’orchestration de `VeganAnalyzer` ou à ML Kit. Plusieurs tests sans import Android restent indirectement non autonomes parce qu’ils passent par `VeganAnalyzer`.

Les tests doivent être déplacés ou partagés, jamais copiés silencieusement : une copie risque de tester une classe différente de celle utilisée par l’application.

## 6. Option A — module JVM permanent `:mutation-core`

Il faudrait déclarer le module dans `settings.gradle.kts`, appliquer `java-library` et Kotlin JVM, puis faire dépendre `:app` de `:mutation-core`. Ces fichiers n’ont pas été modifiés. Le module devrait contenir les modèles, normalisation, lexiques, parser/tokenizer, matcher, sections/langues, règles d’origine, verdict et OCR textuel pur.

`VeganAnalyzer` devrait être scindé : un service pur recevant database, alias et règles, et un adaptateur Android chargeant les assets via `Context`. La direction doit rester `:app -> :mutation-core`; aucune circularité n’est acceptable. Il faudra préserver les packages et traiter les nombreuses déclarations `internal`.

Avantages : PIT analyse le même code compilé que l’application, tests partageables, maintenance et reproductibilité élevées. Risques : migration de fichiers/tests, API de chargement des assets, visibilité et régressions. Statut : `RECOMMANDE`.

## 7. Option B — harness JVM temporaire

Un harness pourrait copier un petit noyau et quelques tests, injecter les trois JSON et appliquer PIT. Il serait réversible et utile pour une preuve sur deux classes, mais les résultats concerneraient une copie. La synchronisation, les changements de visibilité et les fixtures créent un risque élevé de divergence. Statut : `JVM_AVEC_ADAPTATION`; solution finale `A_REJETER`.

## 8. PIT classique — compatibilité théorique

Le Plugin Portal Gradle publie `info.solidsoft.pitest`; sa version affichée comme la plus récente est `1.19.0` au moment de l’inspection et l’application documentée utilise le plugins DSL : [Gradle Plugin Portal](https://plugins.gradle.org/plugin/info.solidsoft.pitest). Cela n’a pas été résolu dans le dépôt.

Configuration théorique future :

```kotlin
plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
    id("info.solidsoft.pitest") version "1.19.0"
}
```

Statut : `NON_VERIFIE`.

- Java 25 doit être vérifié avec la matrice PIT exacte ; le premier prototype devrait compiler le module en Java 17 pour rester aligné sur l’application.
- Kotlin 2.4.20 produit du bytecode JVM mutable, mais fonctions synthétiques, `when`, null-safety, data classes et inline peuvent rendre certains mutants difficiles à interpréter.
- JUnit 4 est adapté en principe à un module JVM classique si les tests et fixtures sont autonomes.
- PIT sait en principe produire HTML/XML via son plugin Gradle ; les chemins et options exacts restent à valider.
- `targetClasses` et `targetTests` devront viser uniquement le package du cœur ; UI, adaptateurs Android et code généré doivent être exclus.
- Les seuils ne doivent être introduits qu’après une campagne produisant effectivement des mutants.

## 9. Comparaison décisionnelle

| Option | Modifications | Fidélité au code Android | Complexité | Coût IA | Risque |
|---|---:|---:|---:|---:|---:|
| PIT Android direct | faible en apparence | faible | élevée | élevé | élevé |
| Module JVM permanent | moyenne/importante | élevée | moyenne/élevée au départ | moyen puis faible | moyen maîtrisable |
| Harness JVM temporaire | faible/moyenne | moyenne/faible | moyenne | moyen/élevé à chaque sync | élevé |
| Mutation manuelle ciblée | aucune structure durable | variable | faible par essai | élevé si répétée | élevé, peu reproductible |

Le module permanent maximise fidélité, stabilité, reproductibilité et publication de rapports. Le harness minimise la modification initiale mais mesure une copie.

## 10. Coûts et risques

Coût local estimé : extraction de plusieurs dizaines de fichiers et migration en plusieurs itérations ; une petite campagne PIT devrait prendre de quelques secondes à quelques minutes, tandis que les régressions OCR/multilingues peuvent devenir coûteuses en temps et mémoire. Coût IA : moyen à élevé lors de l’extraction, puis faible après stabilisation ; élevé de manière récurrente pour synchroniser un harness.

Risques principaux : divergence entre copie et application, oubli d’un alias/règle JSON, chargement de fixture par chemin relatif, changement de visibilité `internal`, mutants Kotlin synthétiques peu lisibles et mélange actuel entre orchestration Android et analyse métier.

## 11. Recommandation et architecture future

Extraire progressivement `:mutation-core` en conservant `com.example.isitvegan`, commencer par modèles, normalisation, lexiques, parser/tokenizer, matcher et verdict, puis injecter les données JSON et extraire sections/langues/OCR textuel. Garder Context, Bitmap, Uri, ML Kit, Compose et écrans dans `:app`.

```text
:mutation-core  -> modèles, cœur métier, OCR textuel, fixtures JVM, tests JUnit, PIT
:app            -> Android, Context/assets adapter, Bitmap/Uri, ML Kit, Compose
:app -> :mutation-core (seule direction autorisée)
```

## 12. Futur prototype et éléments non testés

Étapes futures : extraire un premier noyau ; injecter database/alias/règles ; partager les tests purs ; vérifier les deux baselines ; ajouter PIT classique au seul module JVM ; limiter à deux classes ; produire HTML/XML ; comparer les verdicts `:app`/noyau ; élargir seulement après validation.

La résolution de `info.solidsoft.pitest`, la compatibilité effective avec Java 25/Kotlin 2.4.20/Gradle 9.7.1, la création du module, la campagne PIT, les rapports, les seuils, la durée et la mémoire restent `NON_VERIFIE`. Aucun code, test, asset ou Gradle n’a été modifié.
