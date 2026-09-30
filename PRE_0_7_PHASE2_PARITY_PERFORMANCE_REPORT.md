# Rapport v0.7 — phase 2 : parité et performance

## État initial et périmètre

Les vérifications initiales ont été exécutées avant les modifications :

- branche `master`, à jour avec `origin/master` ;
- commit de référence : `264e216 docs: close 0.6.13.7 documentation cycle` ;
- version applicative : `0.6.13.7`, code `59` ;
- modifications préexistantes conservées : `MultilingualIngredientLexicon.kt` modifié et rapports/fichiers de la phase 1 non suivis ;
- aucun commit, push, importeur, générateur Python, build documentaire, SQLite, Room ou changement de production effectué.

Le matcher et le service d’analyse actuels restent la référence. `KnowledgeIndex` n’est utilisé que par les tests et les mesures.

## Fichiers créés ou modifiés

- `mutation-core/src/test/kotlin/com/example/isitvegan/Phase2ParityBenchmarkTest.kt` — corpus contrôlé, adaptateur de comparaison et benchmark JVM ;
- `app/src/androidTest/java/com/example/isitvegan/Phase2RuntimeBenchmarkInstrumentedTest.kt` — mesure instrumentée sur appareil ;
- `PRE_0_7_PHASE2_PARITY_PERFORMANCE_REPORT.md` — présent rapport.

Les fichiers de production existants de la phase 1 n’ont pas été modifiés pendant cette phase.

## Corpus de parité

Le corpus contient 36 cas :

| Catégorie | Cas |
|---|---:|
| Cas conceptuels directs | 28 |
| Analyses structurées | 8 |
| Langues principales | FR, NL, EN, DE |
| Concepts simples | eau, sucre, sel, blé, lait, œuf, gélatine, miel, café, fraise, chocolat |
| E-numbers | `E471`, `E 471`, `471`, classe fonctionnelle et composition |
| Contextes protégés | chocolat, arôme, goût, extrait, fraise, café, lait, lait végétal, miel |
| Compositions | parents, enfants, profondeur, ordre, pourcentage, répétitions, inconnu |
| Traces | FR, NL, EN, DE |

Les mappings IT et ES restent couverts par la conversion de la phase 1 ; les 1 996 mappings sont rechargés et leur présence est vérifiée dans les tests runtime.

## Méthode de comparaison

Pour chaque cas, l’adaptateur compare :

- les identifiants sélectionnés par `IngredientMatcher` avec les candidats énumérés par l’index ;
- les candidats bloqués avec les candidats disponibles dans l’index ;
- les statuts des concepts sélectionnés avec `RuntimeConcept` ;
- les inconnus et les résolutions du matcher ;
- les tokens d’analyse, leur ordre, leur parent, leur profondeur, leur pourcentage et leur chemin de diagnostic ;
- les occurrences répétées ;
- les traces et leur effet nul sur le verdict.

La comparaison considère une différence de sélection comme significative. L’index est seulement autorisé à fournir un ensemble de candidats ; il ne prétend pas reproduire les règles contextuelles qui sélectionnent ou bloquent ces candidats.

## Résultats fonctionnels

Les 36 cas passent. Aucun candidat sélectionné ni candidat bloqué par le matcher n’est absent de l’index. Les statuts des concepts sélectionnés sont identiques.

Les différences observées sont celles attendues par le contrat de la phase 1 :

- `KnowledgeIndex.findCandidatesInText` peut exposer un candidat lexical pour `arôme chocolat`, `extrait de café`, `lait végétal` ou une expression de goût ;
- `IngredientMatcher` applique ensuite les protections arôme/goût/extrait, les priorités, les frontières et les blocages contextuels ;
- une recherche de candidats contenus est donc plus large qu’une sélection métier et ne peut pas remplacer le matcher.

Cette différence est acceptée et documentée. Aucune différence de verdict ou de statut n’a été introduite, puisque le chemin de production n’a pas changé. Les diagnostics de composition conservent les parents, les occurrences, les inconnus, les pourcentages et les chemins ; les traces restent séparées et n’affectent pas le verdict.

## Résultats JVM

Mesure effectuée avec 5 échauffements et 20 répétitions par opération, séquentiellement, sur les trois assets locaux. Les valeurs sont médianes, avec minimum et maximum en microsecondes.

Taille des fichiers : `ingredients.json` 228 147 octets, mappings 911 829 octets, règles d’origine 14 851 octets.

Structure logique de l’index : 479 concepts, 7 208 formes, 2 762 clés d’alias, 5 clés en collision et 1 cible canonique invalide (`cereals`).

| Opération | Min | Médiane | Max |
|---|---:|---:|---:|
| Lecture des trois JSON | 4 898 µs | 5 659 µs | 6 562 µs |
| Désérialisation `IngredientKnowledge` | 18 397 µs | 20 965 µs | 24 615 µs |
| Construction `KnowledgeIndex` | 15 967 µs | 19 850 µs | 24 949 µs |
| Construction `IngredientMatcher` | 143 987 µs | 169 073 µs | 188 002 µs |
| Recherche alias exact | 1 µs | 1 µs | 1 µs |
| Recherche alias multilingue | 1 µs | 1 µs | 2 µs |
| Recherche E-number | 1 µs | 1 µs | 2 µs |
| Recherche de candidats contenus | 5 443 µs | 5 698 µs | 7 805 µs |
| Analyse simple | 148 845 µs | 178 641 µs | 206 902 µs |
| Analyse imbriquée | 154 801 µs | 185 744 µs | 194 255 µs |
| Analyse multilingue | 140 064 µs | 169 534 µs | 183 334 µs |
| Analyse avec traces | 147 933 µs | 171 686 µs | 205 435 µs |
| Génération des diagnostics | 165 514 µs | 183 218 µs | 199 838 µs |

La recherche exacte est très rapide, mais `findCandidatesInText` parcourt les formes et applique des regex ; elle n’est pas représentative d’une future recherche indexée par clé exacte.

La mesure JVM de mémoire (`used heap`) n’est pas exploitable comme delta : elle a varié de 324 268 024 octets avant chargement à 101 149 776 après désérialisation, puis 178 810 272 après indexation, ce qui montre l’influence du GC et du contexte du processus. Aucune taille mémoire précise n’est donc revendiquée.

## Résultats Android

Un appareil réel était disponible : Nokia G42 5G, Android 15. Le test instrumenté a réussi et a exécuté 10 répétitions après 3 échauffements.

| Opération | Min | Médiane | Max |
|---|---:|---:|---:|
| Lecture des assets | 18 545 µs | 18 801 µs | 19 601 µs |
| Désérialisation | 1 012 758 µs | 1 019 157 µs | 1 483 892 µs |
| Construction de l’index | 1 200 816 µs | 1 224 565 µs | 1 237 594 µs |
| Construction du matcher | 3 532 290 µs | 3 550 547 µs | 3 595 877 µs |
| Recherche exacte | 119 µs | 132 µs | 146 µs |
| Recherche multilingue | 132 µs | 143 µs | 176 µs |
| Recherche E-number | 161 µs | 168 µs | 187 µs |
| Analyse diagnostique | 5 596 915 µs | 5 618 860 µs | 5 688 878 µs |

Le PSS du processus a été relevé à environ 64 838 KiB avant, 112 126 KiB après connaissance et 99 862 KiB après indexation. Ces valeurs sont une approximation du processus Android et ne permettent pas d’attribuer précisément la mémoire à une seule structure.

## Influence de l’échauffement, du JIT et du GC

Les mesures ont un échauffement séparé et rapportent minimum, médiane et maximum. Elles restent sensibles au JIT, au GC, au cache de fichiers, à la charge du daemon Gradle et, sur Android, à la charge du processus et du système. Elles établissent des ordres de grandeur reproductibles dans cette exécution ; elles ne constituent pas un benchmark statistique multi-appareils.

## Tests de non-régression

- `.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.Phase2ParityBenchmarkTest` — succès ;
- `.\gradlew.bat :mutation-core:test` — succès avant le dernier ajout de cas, puis le test de phase 2 a été relancé avec succès après cet ajout ;
- `.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.Phase2RuntimeBenchmarkInstrumentedTest'` — succès sur l’appareil connecté ;
- `git diff --check` — succès.

Les scripts Python, importeurs, générateurs, build documentaire, PIT et campagnes instrumentées lourdes n’ont pas été exécutés. Le test Android exécuté était limité au benchmark de cette phase.

## Limites

Le corpus démontre que l’index contient les candidats et conserve les concepts/stats nécessaires, mais il ne démontre pas une équivalence complète de la sélection contextuelle, des priorités ou des résolutions `EXACT`/`PARTIAL_CONTEXTUAL`/`BLOCKED_CONFLICT`. Ces responsabilités restent dans `IngredientMatcher`.

Les analyses JVM et Android ne mesurent pas une analyse exécutée via l’index, puisqu’il n’est volontairement pas intégré au chemin de production. Il n’est donc pas possible de conclure à un gain de bout en bout sur `AnalysisResult` ou `AnalysisDiagnostics`.

## Recommandation

**Option B — Index fonctionnel mais bénéfice non démontré pour l’intégration de production.**

L’index apporte une construction nettement plus rapide que `IngredientMatcher` et des recherches exactes rapides, mais la recherche de candidats contenus est linéaire et l’index ne porte pas les règles métier de contexte. Le corpus ne justifie donc pas son branchement en production.

Conserver `KnowledgeIndex` comme fondation architecturale et comme outil de mesure. Une phase 3 peut étudier un adaptateur optionnel limité à la recherche exacte, avec double lecture et comparaison occurrence par occurrence, sans supprimer le matcher ni le chemin JSON.

La phase 3 ne doit commencer qu’après validation explicite de ce périmètre expérimental. Aucune migration d’assets, de format, SQLite ou Room n’est recommandée.
