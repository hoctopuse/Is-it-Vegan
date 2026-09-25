# Reprise architecture — extraction de `:mutation-core`

## 1. État Git initial

La branche de départ ne contenait aucune modification suivie. Les seuls fichiers non suivis étaient les quatre rapports autorisés :

- `MUTATION_TESTING_INSPECTION.md` ;
- `MUTATION_TESTING_PIT_PROTOTYPE.md` ;
- `MUTATION_TESTING_JVM_FEASIBILITY.md` ;
- `MUTATION_CORE_PHASE1_REPORT.md`.

La branche temporaire `mutation-core-architecture` a été créée. Aucun commit ni push n’a été effectué.

## 2. Baseline avant migration

- `.\gradlew testDebugUnitTest --rerun-tasks` : `BUILD SUCCESSFUL`.
- `.\gradlew assembleDebug` : `BUILD SUCCESSFUL`.

## 3. Architecture retenue

Le projet contient désormais un module Kotlin/JVM `:mutation-core` compilé en Java 17 et un module Android `:app` qui en dépend. La direction est uniquement `:app -> :mutation-core`.

Le cœur contient le pipeline de sélection du texte métier, prétraitement, parsing, matching, règles d’origine, verdict et diagnostic. L’application conserve le chargement des assets par `Context`, l’UI, Compose, Bitmap, Uri et ML Kit.

## 4. Fichiers déplacés et créés

Ont été déplacés de `app/src/main/java/com/example/isitvegan` vers `mutation-core/src/main/kotlin/com/example/isitvegan` : modèles d’ingrédients, normalisation, quantité, lexiques, règles d’origine, parser, tokenizer, matcher, inconnus, verdict, langue et sections, prétraitement, diagnostic, normalisation OCR bornée, classificateur linguistique pur et service d’analyse.

Fichiers d’intégration créés :

- `mutation-core/build.gradle.kts` ;
- `app/.../AndroidIngredientKnowledgeLoader.kt` ;
- nouvelle façade Android `app/.../VeganAnalyzer.kt` ;
- `mutation-core/.../UiLanguage.kt` pour l’enum pur ;
- `mutation-core/src/test/.../CoreTestAnalyzer.kt` comme adaptateur de tests sans logique métier.

`settings.gradle.kts`, le catalogue de versions, les builds racine/app et la documentation d’architecture ont été adaptés. Kotlin reste en `2.4.20`, Gradle en `9.7.1`, AGP en `9.4.1` et le niveau Java du nouveau module est 17.

## 5. API pure créée

`IngredientAnalysisService` reçoit un `IngredientKnowledge` immuable et expose l’analyse simple et l’analyse diagnostique. Il contient le comportement auparavant exécuté directement dans l’objet Android `VeganAnalyzer`.

`IngredientKnowledge.fromJson` reçoit trois chaînes : base d’ingrédients, alias multilingues et règles d’origine. Il utilise le lecteur JSON JVM déjà présent, construit les modèles du cœur et conserve les erreurs de validation des règles.

## 6. Injection de connaissance

`AndroidIngredientKnowledgeLoader` est l’unique frontière de chargement Android. Il lit les trois assets avec `Context`, transmet leur contenu à `IngredientKnowledge.fromJson`, puis `VeganAnalyzer.loadDatabase` installe un `IngredientAnalysisService`.

Aucune copie de production des trois JSON n’a été placée dans les sources principales du module JVM. Les assets de production sont restés inchangés.

## 7. Classes conservées dans `:app`

Restent dans l’application : `MainActivity`, `OcrFirstScreen`, `OcrProcessor`, `OcrImageInputFactory`, `OcrBitmapCropper`, les écrans et thèmes Compose, `UiLanguagePreferences`, les accès `Context`, Bitmap, Uri, ExifInterface et ML Kit. Le cœur n’a aucune dépendance vers `:app`.

## 8. Tests déplacés ou adaptés

Les tests suivants ont été déplacés, pas copiés :

- `IngredientTreeParserTest` ;
- `ParserModulesTest` ;
- `HierarchyAnalysisTest` ;
- `LabelSectionExtractorTest` ;
- `Version059CompletionTest`.

Ils utilisent `CoreTestAnalyzer`, qui construit `IngredientAnalysisService` avec une connaissance injectée. Leurs cas fonctionnels et assertions n’ont pas été étendus. Les autres tests restent dans `:app` et passent par la façade Android, laquelle délègue au même service du module.

## 9. Fixture JSON

`HierarchyAnalysisTest` utilise `mutation-core/src/test/resources/origin_qualifier_rules.json`. Cette fixture est une copie contrôlée de l’asset de production nécessaire au test déjà existant. Son SHA-256 lors de la migration est identique à celui de l’asset : `AAEEFEA599159C3F87B99CD33CAB2BD3319C3CE596607509AEEF16AF29B4F451`.

Le risque de divergence demeure si l’asset change sans mise à jour de la fixture. Une phase ultérieure devrait automatiser cette vérification ou générer la ressource de test depuis la source de référence.

## 10. Résultats Gradle

- `:mutation-core:test --rerun-tasks` : `BUILD SUCCESSFUL`.
- `testDebugUnitTest --rerun-tasks` : `BUILD SUCCESSFUL`.
- `assembleDebug` : `BUILD SUCCESSFUL`.
- `connectedDebugAndroidTest` : non exécuté, car `adb devices` ne listait aucun appareil ou émulateur.

## 11. Vérification Android et dépendances

La recherche d’imports `android.*`, `androidx.*`, ML Kit, `Context`, Bitmap et Uri dans `mutation-core/src` ne retourne aucun résultat. `app/build.gradle.kts` déclare `implementation(project(":mutation-core"))`. Aucun fichier du module JVM ne référence `:app`.

## 12. Régressions rencontrées et résolution

La première compilation du cœur a révélé la dépendance pure de `LabelLanguageSegmenter` vers `OcrBlockLanguageClassifier`; cette classe JVM a été déplacée avec la frontière linguistique.

La compilation des tests Android a ensuite rejeté trois smart casts de `LabelSections.ingredientsText`, car une propriété publique d’un autre module ne peut pas être smart-castée de la même manière. Les suites directement concernées ont été déplacées dans le module JVM, où elles testent la classe propriétaire. Aucun comportement métier n’a été modifié.

La revue automatique a refusé deux déplacements groupés trop larges avant exécution. La migration a été reprise par étapes après création explicite de l’adaptateur Android et vérification individuelle de l’absence d’import Android.

## 13. Limites restantes

Les classes exposées auparavant `internal` ont dû devenir publiques à la frontière Gradle afin que l’application et ses tests puissent utiliser le cœur. Une future phase pourra réduire cette surface autour de `IngredientAnalysisService` et des modèles réellement nécessaires.

Une partie des tests métier reste sous `app/src/test`; ils testent bien le code du module via la façade, mais pourront être migrés progressivement. Les composants OCR géométriques/textuels qui ne sont pas nécessaires au service restent dans `:app`.

## 14. Préparation de la future étape PIT

PIT classique pourra être ajouté uniquement à `:mutation-core`. La première campagne devra cibler au maximum deux classes déterministes, par exemple `VerdictEngine` et `IngredientMatcher`, exécuter les tests JUnit 4 du module et produire HTML/XML. Le plugin PIT n’a pas été ajouté pendant cette migration.

## 15. État final attendu

Les quatre rapports précédents sont conservés sans modification. Les changements durables sont le module JVM, l’adaptateur Android, la migration des tests, les configurations Gradle, la documentation nécessaire et le présent rapport. Les fichiers JSON de production sont inchangés. Aucun commit ni push n’a été effectué.
