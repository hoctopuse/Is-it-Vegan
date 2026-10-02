# Prototype contrôlé du plugin PIT Android

## Objectif et périmètre

Vérifier la résolution et la compatibilité pratique de `pl.droidsonroids.pitest` version `0.2.27` avec **Is It Vegan?**, sans campagne complète.

## État Git initial et isolation

État initial autorisé :

```text
?? MUTATION_TESTING_INSPECTION.md
```

`git diff --stat` et `git diff --name-only` étaient vides. Le fichier d’inspection a été préservé. Le prototype a été exécuté dans la branche temporaire `mutation-pitest-prototype`. Aucun commit ni push n’a été effectué.

## Versions et environnement

- Gradle wrapper : `9.7.1`.
- AGP : `9.4.1`.
- Kotlin : `2.4.20`.
- Plugin testé : `pl.droidsonroids.pitest:0.2.27`.
- JVM Gradle : JetBrains JDK 25 Android Studio, `25.0.3`, sous `C:\Program Files\Android\Android Studio\jbr`.
- Oracle JDK 27 était détecté via `JAVA_HOME`, mais n’a pas été sélectionné pour l’exécution.
- Module : `:app`.
- Variante : `debug` / `testDebugUnitTest`.

## Baseline avant modification

Commande : `.\gradlew testDebugUnitTest --rerun-tasks`.

Résultat : `BUILD SUCCESSFUL`. Les tâches observées incluaient `:app:compileDebugKotlin`, `:app:compileDebugUnitTestKotlin` et `:app:testDebugUnitTest`. Aucun test instrumenté n’a été lancé.

## Modification temporaire

Ajout unique dans `app/build.gradle.kts` :

```kotlin
id("pl.droidsonroids.pitest") version "0.2.27"
```

Aucune autre version, dépendance, source, test, donnée, alias ou asset n’a été modifié.

## Résolution et tâches Gradle

Commande : `.\gradlew help --stacktrace`.

La sortie confirme :

```text
Resolved plugin [id: 'pl.droidsonroids.pitest', version: '0.2.27']
```

Statut : `PLUGIN_RESOLVED`.

La commande `.\gradlew tasks --all` n’a pas atteint l’affichage exploitable des tâches : la compilation du script Kotlin DSL échoue pendant la configuration. Aucune tâche PIT n’a pu être confirmée. Statut : `PIT_TASK_UNAVAILABLE`.

## Compilation après ajout

La configuration échoue dans `app/build.gradle.kts` sur les lignes existantes `compileSdk { ... }` et `version = release(37)`, signalées comme `Unresolved reference` après application du plugin. Le plugin est donc résolu mais incompatible avec la configuration Kotlin DSL observée.

Statut : `BASELINE_REGRESSION` pendant l’ajout, classé globalement `PLUGIN_INCOMPATIBLE`.

Aucun contournement ou bricolage de configuration n’a été tenté.

## Campagne PIT

Aucune commande PIT n’a été lancée, car aucune tâche n’a été rendue disponible après configuration. Les classes candidates `IngredientTreeParser`, `IngredientMatcher`, `LabelLanguageSegmenter`, `LabelSectionExtractor` et `VerdictEngine` n’ont pas été ciblées.

- `PLUGIN_RESOLVED` : oui ;
- `PIT_TASK_FOUND` : non ;
- `PIT_EXECUTED` : non ;
- `MUTANTS_GENERATED` : non ;
- `MUTATION_RESULTS_VALID` : non.

Il n’y a donc aucun résultat de mutants, tests PIT, survivants, couverture ou rapport HTML/XML. Les tests instrumentés, Compose, CameraX et ML Kit n’ont pas été lancés.

## Faits, hypothèses et limites

Faits observés : résolution réussie ; erreur de compilation du script Kotlin DSL après application ; aucune tâche PIT exploitable ; aucune campagne exécutée.

Hypothèse : `0.2.27` n’est pas utilisable tel quel avec la combinaison AGP `9.4.1`, Gradle `9.7.1` et le DSL Kotlin du projet. La compatibilité du moteur PIT avec les classes métier Kotlin reste non vérifiée.

Aucun symptôme de surcharge mémoire attribuable à PIT n’a été observé. PIT n’a pas démarré. Une première relance finale a rencontré un fichier de résultats temporaire manquant ; la seconde exécution identique a réussi.

## Restauration et baseline finale

La ligne PIT a été supprimée de `app/build.gradle.kts`, puis les daemons ont été arrêtés avec `.\gradlew --stop`.

Commande finale : `.\gradlew --no-daemon --console=plain testDebugUnitTest --rerun-tasks --stacktrace`.

Résultat : `BUILD SUCCESSFUL`. Statut : `BASELINE_OK`.

Le code de production, les tests, `ingredients.json`, les alias et les assets n’ont pas changé.

## État final et recommandation

Le seul fichier durable ajouté est `MUTATION_TESTING_PIT_PROTOTYPE.md`. `MUTATION_TESTING_INSPECTION.md` a été conservé et la configuration Gradle restaurée.

Statut global : `PLUGIN_INCOMPATIBLE`.

Ne pas lancer de campagne complète avec cette configuration. Une suite nécessiterait d’abord une compatibilité documentée du plugin avec AGP `9.4.1`, Gradle `9.7.1` et le DSL Kotlin utilisé par le projet, puis un nouveau prototype isolé.
