# Relecture indépendante — revalidation finale v0.7 phase 4

## Décision

**GO_WITH_WARNINGS**

Aucun constat bloquant ne remet en cause le passage à la phase 5. La référence contient bien l’optimisation annoncée, la parité du pré-calcul avec le prédicat historique est vérifiée sur les 2 485 alias normalisés par une assertion exécutée avec succès, et les artefacts conservés prouvent le succès des suites JVM et de la suite Android complète sur le Nokia G42 5G sous Android 15.

Les avertissements portent sur la traçabilité de l’état initial et du lancement Android ciblé séparé, ainsi que sur une formulation trop large de la portée du test de profilage. Les invariants métier sont couverts par les suites générales conservées, pas par toutes les opérations simplement chronométrées dans le test phase 4.

## Périmètre et éléments consultés

Cette relecture est statique. Aucune suite de tests, campagne de benchmark, commande d’import, génération ou script Python n’a été lancé. Aucun fichier existant n’a été modifié.

Éléments consultés directement :

- état Git courant, `HEAD`, métadonnées et diff du commit `21454d9` avec son parent ;
- `REVALIDATION_0_7_PHASE4_CLEAN_WORKTREE_REPORT.md` ;
- `mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt` ;
- `mutation-core/src/test/kotlin/com/example/isitvegan/Phase4MatcherProfileTest.kt` et son diff local ;
- `app/src/androidTest/java/com/example/isitvegan/Phase4MatcherProfileInstrumentedTest.kt` ;
- tests de parité et tests métier existants, notamment `Phase2ParityBenchmarkTest.kt`, `RealLabelsInstrumentedTest.kt` et `VerdictExplanationInstrumentedTest.kt` ;
- résultats JUnit XML conservés sous `mutation-core/build/test-results/test/` et `app/build/test-results/testDebugUnitTest/` ;
- résultat Android agrégé `app/build/outputs/androidTest-results/connected/debug/TEST-Nokia G42 5G - 15.xml` ;
- rapport HTML et logcat conservés pour `Phase4MatcherProfileInstrumentedTest` ;
- présence des rapports phase 4 et état des chemins de production, `knowledge/` et assets.

Les répertoires `build/` sont des artefacts locaux non versionnés. Ils constituent une preuve directe de résultats exécutés dans ce worktree, mais pas un historique durable ou signé.

## Faits vérifiés directement

### Référence et portée du commit

- `HEAD` pointe sur `21454d985a803652cd16eb40979bc348ef897ef2`, sujet `perf(v0.7): optimize linked alias matcher preparation`.
- Le diff avec le parent remplace le calcul pairwise de `hasLongerLinkedAlias` par un ensemble calculé une fois. L’utilisation se trouve dans `IngredientMatcher.kt:136-160` et l’algorithme de pré-calcul dans `IngredientMatcher.kt:349-367`.
- Le pré-calcul conserve la même expression des six connecteurs à `IngredientMatcher.kt:367` et exige que le préfixe soit lui-même présent dans l’ensemble des alias à `IngredientMatcher.kt:355-361`.
- Le commit ajoute aussi une instrumentation optionnelle de profilage dans `IngredientMatcher`. Elle est inactive lorsque le collecteur est absent. La modification de production du commit est donc un peu plus large que le seul remplacement algorithmique, sans changement de règle métier identifié.
- Le commit agrège également des fichiers des phases 1 à 3. Son titre correspond bien à l’optimisation examinée, mais le commit complet n’est pas un diff exclusivement limité à la phase 4.
- Aucun changement entre le parent et `21454d9` n’apparaît dans les assets, `knowledge/`, `VerdictEngine`, `VeganAnalyzer`, `IngredientAnalysisService`, `IngredientTreeParser` ou `IngredientTokenizer` pour les chemins contrôlés.

### État Git disponible

L’état courant contient :

- `mutation-core/src/test/kotlin/com/example/isitvegan/Phase4MatcherProfileTest.kt` modifié localement ;
- `REVALIDATION_0_7_PHASE4_CLEAN_WORKTREE_REPORT.md` non suivi avant la présente revue ;
- aucun fichier de production, asset ou fichier sous `knowledge/` modifié localement.

La mention « worktree propre » du rapport est correctement formulée comme un état initial (`REVALIDATION_0_7_PHASE4_CLEAN_WORKTREE_REPORT.md:11-17`). Elle ne décrit pas l’état final. Cet état initial ne peut toutefois pas être reconstitué directement à partir de Git après les modifications locales.

### Modification locale du test JVM

Le diff local ajoute 53 lignes et ne touche qu’au test autorisé.

- Le compte exact de 2 485 alias est une assertion exécutable à `Phase4MatcherProfileTest.kt:113-117`.
- Le prédicat pairwise historique est recalculé sur la liste complète à `Phase4MatcherProfileTest.kt:118-125`, puis son ensemble est comparé au pré-calcul à `Phase4MatcherProfileTest.kt:127`.
- Les six connecteurs `de`, `d`, `du`, `des`, `a`, `au` sont itérés explicitement à `Phase4MatcherProfileTest.kt:130-163`.
- Pour chacun, le test vérifie une forme positive, le connecteur en fin de chaîne comme frontière valide, un suffixe invalide, l’absence du préfixe court dans l’ensemble d’alias et un alias trop court qui ne coïncide pas avec une frontière.
- Le test FR/NL/EN/DE et normalisation vérifie `eau`, `melk`, `milk`, `Schokolade` et `"  E 471  "` à `Phase4MatcherProfileTest.kt:166-178`. Il affirme la présence de l’identifiant attendu, sans affirmer l’unicité du résultat.

Le résultat conservé `mutation-core/build/test-results/test/TEST-com.example.isitvegan.Phase4MatcherProfileTest.xml` contient quatre tests, zéro échec et zéro erreur. Il nomme les deux tests ajoutés, le test de profilage et le test de parité. Son `system-out` relève `normalizedAliases=2485`.

La parité des ensembles sur les 2 485 alias n’est donc pas seulement décrite dans le rapport : elle est codée par des assertions et son exécution réussie est attestée par l’artefact JUnit.

### Résultats JVM conservés

Les XML présents attestent :

- suite `mutation-core` : 107 tests, zéro échec, zéro erreur, zéro ignoré ;
- suite `testDebugUnitTest` : 287 tests, zéro échec, zéro erreur, zéro ignoré ;
- `Phase2ParityBenchmarkTest` : trois tests réussis, incluant les protections arôme/goût/extrait, les répétitions, parents/enfants, inconnus, pourcentages et traces (`Phase2ParityBenchmarkTest.kt:84-145`).

Ces artefacts rendent les succès des suites JVM directement vérifiables. Ils ne conservent pas la ligne de commande exacte ni l’ordre séquentiel des lancements.

### Résultats Android conservés

`app/build/outputs/androidTest-results/connected/debug/TEST-Nokia G42 5G - 15.xml:2` atteste une suite de 40 tests, zéro échec, zéro erreur et zéro ignoré. Le nom du fichier identifie le Nokia G42 5G et Android 15 ; chaque suite porte le serial `AQ5003H044Q32600084`.

Dans cet agrégat :

- `Phase4MatcherProfileInstrumentedTest` a un test réussi, durée 435,271 s (`TEST-Nokia G42 5G - 15.xml:42-47`) ;
- `RealLabelsInstrumentedTest` a 17 tests réussis (`TEST-Nokia G42 5G - 15.xml:48-69`) ;
- `VerdictExplanationInstrumentedTest` a 7 tests réussis (`TEST-Nokia G42 5G - 15.xml:70-81`).

Le logcat conservé pour le test phase 4 contient le démarrage, la fin et les mesures. Il relève notamment 2 490 entrées, 2 485 alias normalisés, 96 appels au matcher et les chronométrages de construction, matching et analyses.

Le test Android phase 4 compare bien un matcher profilé et un matcher sans collecteur sur six entrées à `Phase4MatcherProfileInstrumentedTest.kt:31-37`, puis vérifie que le profil contient des entrées et des regex à `Phase4MatcherProfileInstrumentedTest.kt:59-61`. Les recherches et analyses de `Phase4MatcherProfileInstrumentedTest.kt:38-56` sont principalement exécutées et chronométrées ; leurs verdicts, structures et diagnostics ne font pas chacun l’objet d’une assertion métier dans ce test.

Les invariants métier sont néanmoins attestés par les autres tests de la suite Android complète : séparation des traces, inconnus empêchant un verdict vegan, visibilité de `UNCERTAIN`, sémantique des statuts, compositions imbriquées et pourcentages apparaissent explicitement dans les tests listés dans l’XML et dans `RealLabelsInstrumentedTest.kt:66-144` et `VerdictExplanationInstrumentedTest.kt:80-131`.

### Production, données et fonctionnement offline

- Le diff local ne contient aucune modification de production, d’asset, de donnée éditoriale, d’importeur, de version ou de migration.
- Aucun appel de production à `KnowledgeIndex.from` ni construction de `IndexedCandidateProvider` n’a été trouvé hors de leurs propres définitions dans `mutation-core/src/main` et `app/src/main`. Ils restent donc hors du chemin de production selon l’inspection statique disponible.
- La modification phase 4 n’ajoute aucun accès réseau ni service externe. Le test instrumenté lit les trois assets locaux à `Phase4MatcherProfileInstrumentedTest.kt:18-29` et `99-103`.
- Aucun élément examiné ne modifie la séparation des traces, la gestion des inconnus, `UNCERTAIN`, les statuts, les protections arôme/goût/extrait, les compositions, occurrences ou diagnostics.

## Éléments seulement rapportés ou non vérifiables directement

Les points suivants ne disposent pas d’un journal de commande durable distinct :

- l’état exact du worktree avant la première modification ;
- la sortie initiale de `git worktree list` et de `adb devices` ;
- l’ordre précis des commandes Gradle ;
- le succès d’un lancement Android ciblé séparé avant la suite complète ; les artefacts actuellement présents prouvent toutefois que ce test précis a réussi dans la suite complète sur le Nokia ;
- le résultat historique de `git diff --check` ;
- l’absence d’exécution de script Python, importeur ou générateur ;
- les avertissements de console `sun.misc.Unsafe` et bibliothèques natives non stripables ;
- l’affirmation spécifique « aucune erreur pypdf » comme observation de console. Aucun échec ni texte `pypdf` n’apparaît dans les XML conservés, mais ceux-ci ne remplacent pas un transcript complet.

Ces limites concernent la traçabilité. Elles ne contredisent aucun artefact disponible.

## Constats classés par gravité

### Bloquant / élevé

Aucun constat.

### Modéré — portée du test de profilage formulée trop largement

Le rapport attribue au « corpus » phase 4 la couverture des tirets, parenthèses et invariants métier complets (`REVALIDATION_0_7_PHASE4_CLEAN_WORKTREE_REPORT.md:70-74`). Le test phase 4 compare surtout deux instances du même code optimisé (`Phase4MatcherProfileTest.kt:14-24`) et chronomètre plusieurs analyses sans assertions sur leur résultat (`Phase4MatcherProfileTest.kt:26-81`). Il ne constitue donc pas, isolément, une preuve de non-régression métier exhaustive ni une comparaison avant/après du matcher.

Impact : non bloquant, car la parité algorithmique est prouvée séparément sur tous les alias et les suites générales réussies couvrent les invariants. La formulation devrait distinguer « exécuté/chronométré » de « résultat métier asserté ».

### Faible — lancement ciblé distinct non conservé

Le succès de la suite complète et du test phase 4 dans cette suite est directement prouvé. En revanche, aucun artefact séparé n’établit que la commande ciblée adaptée a constitué un lancement indépendant avant la suite complète.

Impact : limite de traçabilité sans effet sur la conclusion technique, puisque le scénario ciblé est présent et réussi dans l’agrégat complet.

### Faible — propreté initiale historique

Le worktree est maintenant volontairement non propre. Le rapport limite correctement la propreté au début de la revalidation, mais cette chronologie repose sur sa déclaration et non sur un artefact Git persistant.

Impact : non bloquant. Le diff courant correspond exactement au test local annoncé et au rapport ajouté ; aucun changement de production n’est masqué dans l’état disponible.

### Information — rapports antérieurs absents

Les fichiers suivants sont effectivement absents :

- `REVIEW_0_7_PHASE4_SEQUENCE_CORRECTION_REPORT.md` ;
- `REVIEW_0_7_PHASE4_POST_IMPLEMENTATION_REVIEW_REPORT.md` ;
- `REVALIDATION_0_7_PHASE4_FINAL_REPORT.md`.

`PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md` et `REVIEW_0_7_PHASE4_MATCHER_OPTIMIZATION_REPORT.md` sont présents au commit de référence. Les trois absences réduisent la continuité documentaire, mais les preuves statiques et les artefacts de tests disponibles suffisent à statuer techniquement.

## Réserves documentaires et techniques

- Les résultats sous `build/` peuvent être supprimés lors d’un nettoyage et ne sont pas une preuve archivée avec le dépôt.
- Le test de parité exhaustive verrouille la connaissance actuelle, soit 2 485 alias ; une modification future des données fera intentionnellement échouer le compte fixe et exigera une revalidation.
- Le test multilingue ajouté vérifie quelques formes représentatives et la présence de l’identifiant attendu. La couverture multilingue plus large vient des suites existantes.
- Les mesures Android sont des mesures intégrées au test, sensibles au JIT, au GC et à la charge du téléphone. Elles établissent que les scénarios s’exécutent sur l’appareil, mais ne constituent pas à elles seules un benchmark comparatif robuste avant/après.
- L’instrumentation optionnelle ajoutée au code de production élargit légèrement le diff de phase 4, même si elle reste inactive par défaut et qu’aucune dérive fonctionnelle n’est observée.

## Conclusion et recommandation

La décision proportionnée aux preuves consultables est **GO_WITH_WARNINGS**. La phase 5 peut commencer : la transformation ciblée est logiquement équivalente au prédicat historique, l’assertion exhaustive a réussi, le code métier hors matcher n’est pas modifié et les suites JVM/Android conservées ne montrent aucune régression.

Un benchmark réel ultérieur sur le Nokia doit être préparé séparément du passage en phase 5 : même APK, même corpus, même protocole d’échauffement, plusieurs séries avant/après et conservation des résultats bruts. Cette préparation n’est pas une condition préalable au démarrage de la phase 5 et ne doit pas être confondue avec les tests de correction déjà suffisants.

La décision n’étant pas `NO_GO`, aucune condition de déblocage n’est requise.
