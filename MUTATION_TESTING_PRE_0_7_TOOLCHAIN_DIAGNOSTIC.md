# Diagnostic outillage PIT pré-0.7

## Périmètre et état Git

Diagnostic exécuté exclusivement dans le worktree `C:\Users\Sebastien\StudioProjects\Is-it-Vegan-mutation-testing-pit`.

- branche : `mutation-testing-pit`
- HEAD : `1f39308f55f20a18b36a2fbdae611d9ac78fbb03`
- état initial : propre
- synchronisation : `git merge-base --is-ancestor master HEAD` réussi ; `master` est déjà fusionnée localement dans cette branche
- aucune action sur `master`, aucun commit et aucun push

## Configuration réellement utilisée

Configuration suivie de [`mutation-core/build.gradle.kts`](mutation-core/build.gradle.kts) : plugin `info.solidsoft.pitest` version `1.19.0`, toolchain Kotlin/Java 17, `targetTests = com.example.isitvegan.*`, sorties `HTML` et `XML`, rapports non horodatés, deux threads. La configuration suivie cible dix classes.

Pour le diagnostic minimal, aucune configuration suivie n'a été modifiée. Un script Gradle d'initialisation temporaire, supprimé ensuite, a surchargé en mémoire la tâche `pitest` avec :

```text
targetClasses = com.example.isitvegan.TextNormalizer
targetTests = com.example.isitvegan.*
threads = 1
verbose = true
```

Commande exacte :

```text
.\gradlew.bat :mutation-core:pitest --init-script .\pit-diagnostic.init.gradle --rerun --stacktrace --info --no-daemon --console=plain
```

Le processus PIT réellement lancé utilisait PIT `1.22.1`, avec `--targetClasses=com.example.isitvegan.TextNormalizer`, `--targetTests=com.example.isitvegan.*`, `--threads=1`, HTML/XML, et le classpath `mutation-core/build/pitClasspath`.

## Versions et JDK disponibles

- Gradle wrapper : `9.7.1`
- PIT Gradle plugin : `1.19.0`
- moteur PIT : `1.22.1`
- Kotlin plugin : `2.4.20` (Gradle affiche Kotlin `2.4.0`)
- Java source/target et toolchain du module : `17`
- launcher Gradle : Oracle JDK `27`, `C:\Java\jdk-27`
- daemon Gradle : Eclipse Temurin `25.0.3`, `C:\Users\Sebastien\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2`
- compilation et processus PIT/minions : Eclipse Temurin `17.0.20.1`, `C:\Users\Sebastien\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2`
- JBR Android Studio disponible : JetBrains Runtime `25.0.3`, `C:\Program Files\Android\Android Studio\jbr`
- `java` et `javac` ne sont pas dans le `PATH`
- aucun JDK ni dépendance n'a été téléchargé

## Baseline de test

Commande :

```text
.\gradlew.bat :mutation-core:test --no-daemon --console=plain
```

Résultat : `BUILD SUCCESSFUL`, exit code `0`. Sorties complètes capturées dans `C:\Users\Sebastien\AppData\Local\Temp\mutation-core-test-final.out` et `C:\Users\Sebastien\AppData\Local\Temp\mutation-core-test-final.err`.

## Résultat PIT minimal

Résultat reproductible : `BUILD SUCCESSFUL`, exit code `0`.

Extraits utiles des sorties complètes capturées dans `C:\Users\Sebastien\AppData\Local\Temp\pit-minimal-3.out` et `C:\Users\Sebastien\AppData\Local\Temp\pit-minimal-3.err` :

```text
Using PIT: 1.22.1
PIT >> INFO : Created 1 mutation test units in pre scan
PIT >> INFO : MINION : 90 tests discovered
PIT >> FINE : Coverage generator Minion exited ok
PIT >> INFO : Created 1 mutation test units
PIT >> FINE : Minion exited ok
PIT >> INFO : Completed in 4 seconds
BUILD SUCCESSFUL in 18s
```

Rapports générés : `mutation-core/build/reports/pitest/index.html` et `mutation-core/build/reports/pitest/mutations.xml`. Trois mutations de `TextNormalizer.normalize` sont rapportées : deux survivantes et une tuée par `ParserModulesTest.matcherFindsKnownIngredientsInsideReviewedModifiers`. Aucun fichier `hs_err_pid*.log`, `java_error_in_*.log` ou `replay_pid*.log` n'a été trouvé dans le worktree.

## Cause et distinction outil/code

L'échec `UNKNOWN_ERROR` n'est pas reproduit. Le classpath PIT est construit, l'instrumentation atteint `TextNormalizer`, le minion de couverture découvre 90 tests et se termine normalement, puis le minion de mutation se termine normalement. Aucun problème confirmé de classpath, fork JVM, instrumentation, tests ou compatibilité PIT/JDK n'est observé.

Le résultat disponible permet seulement de classer l'incident initial comme un échec transitoire ou externe du processus minion. Le log initial ne contient pas d'exception enfant permettant une cause plus précise. La différence de JDK reste une hypothèse à surveiller, mais elle n'est pas démontrée : le même pipeline PIT 1.22.1 avec PIT/minions en Java 17 passe ici.

Il s'agit d'un problème d'outillage à investiguer, sans défaut du code applicatif établi. Les mutations survivantes sont un résultat PIT normal et ne sont pas la cause de l'incident minion.

## Prochaine action minimale

Conserver cette campagne comme smoke test. Si `UNKNOWN_ERROR` réapparaît, relancer exactement la commande avec sorties séparées et récupérer immédiatement le journal du daemon Gradle et tout crash JVM avant toute campagne élargie. Ne lancer aucun autre groupe tant que ce smoke test n'est pas vert.

## Contrôles finaux

- `:mutation-core:test` : vert
- `git diff --check` : vert
- script temporaire supprimé
- seul fichier durable ajouté : `MUTATION_TESTING_PRE_0_7_TOOLCHAIN_DIAGNOSTIC.md`

