# Rapport Phase 1 — Extraction progressive de `:mutation-core`

## Statut

`ESCALADE_MODELE_REQUISE`

La migration n’a pas été commencée. Une décision d’architecture est nécessaire pour préserver les tests existants sans extraire prématurément les composants langue, sections et orchestration explicitement exclus. La reprise devrait être faite avec un modèle de niveau supérieur, idéalement Terra Medium ou Sol Medium.

## État initial et baseline

Les contrôles Git obligatoires ont montré uniquement les trois rapports attendus comme fichiers non suivis : `MUTATION_TESTING_INSPECTION.md`, `MUTATION_TESTING_JVM_FEASIBILITY.md` et `MUTATION_TESTING_PIT_PROTOTYPE.md`. Aucun fichier suivi n’était modifié.

La branche temporaire `mutation-core-phase1` a été créée, sans commit ni push.

La commande `.\gradlew testDebugUnitTest --rerun-tasks` a réussi avec `BUILD SUCCESSFUL`.

## Inspection réalisée

Les sept composants demandés ont été inspectés : `IngredientTreeParser`, `IngredientTokenizer`, `IngredientMatcher`, `TextNormalizer`, `QuantityCleaner`, `VerdictEngine` et `UnknownCollector`.

Le noyau est majoritairement pur JVM, mais `IngredientTreeParser` dépend directement de `FunctionalClassLexicon` et `OriginQualifierRuleSet`. Le matcher dépend des modèles `Ingredient`, `VeganStatus` et `IngredientToken`. Ces dépendances seraient extractibles mécaniquement avec le noyau.

## Blocage précis

Les tests demandés en premier ne sont pas isolés du reste du pipeline :

- `IngredientTreeParserTest` appelle `VeganAnalyzer.analyzeWithDiagnostics` et `DiagnosticReport.build` ;
- `ParserModulesTest` appelle `LabelPreprocessor.preprocess`, en plus du tokenizer, matcher, collector et verdict ;
- `HierarchyAnalysisTest` appelle `VeganAnalyzer`, `DiagnosticReport` et charge `origin_qualifier_rules.json` depuis le chemin des assets Android.

`VeganAnalyzer` dépend de `Context`, `org.json.JSONArray` et du chargement des assets. `LabelPreprocessor` et `DiagnosticReport` appartiennent à des couches explicitement hors du périmètre de cette phase. Déplacer seulement les classes pures rendrait ces tests non compilables ou obligerait à créer des doublons/adaptateurs dont la fidélité ne serait pas garantie.

## Décision non prise

Il faut choisir entre une extraction plus large incluant l’orchestration pure de `VeganAnalyzer`, un refactoring préalable de `VeganAnalyzer` pour injecter la base et les règles, ou des tests JVM dédiés au noyau sans déplacer les trois tests demandés. Ce choix peut modifier l’API, la visibilité `internal` et la structure des tests ; il n’a pas été improvisé.

## Modifications effectuées

Aucune modification de Gradle, `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, code Kotlin, tests, assets ou JSON. Aucun module `:mutation-core` n’a été créé. Aucun plugin ou dépendance n’a été ajouté. PIT n’a pas été installé ni lancé.

Le seul fichier ajouté est ce rapport. Les trois rapports existants ont été préservés et n’ont pas été modifiés.

## Vérifications finales

Les contrôles demandés sont `git status --short`, `git diff --check`, `git diff --stat` et `git diff --name-only`. La baseline reste verte. `assembleDebug` et `connectedDebugAndroidTest` n’ont pas été lancés puisqu’aucune migration n’a été effectuée.

## Recommandation de reprise

Reprendre avec un modèle supérieur pour décider d’une frontière de cœur cohérente. La solution la plus fidèle semble être une extraction progressive comprenant une API pure pour charger/injecter la base, les alias et les règles, puis le déplacement coordonné des tests qui vérifient cette API. Il faut conserver la direction `:app -> :mutation-core`, préserver les packages et éviter toute copie silencieuse des classes ou tests.
