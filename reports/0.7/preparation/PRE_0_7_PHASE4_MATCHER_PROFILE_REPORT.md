# Phase 4 v0.7 — profilage et optimisation sûre du matcher

## État Git et séquence

Au début de cette reprise, `git status` était propre et `HEAD` était `21454d9 perf(v0.7): optimize linked alias matcher preparation`. Ce commit contient déjà une optimisation de phase 4, contrairement au contexte initial qui annonçait un état antérieur. Aucun historique n'a été réécrit et aucun commit ou push n'a été effectué pendant cette reprise.

`REVIEW_0_7_PHASE4_MATCHER_OPTIMIZATION_REPORT.md` a été produit avant la présente exécution réelle. Il est prématuré et invalide : il n'est ni une baseline, ni une preuve, ni une validation. Seuls le diff courant, les tests et les mesures ci-dessous sont évaluables.

## Baseline réelle et hypothèse

Le code de référence historique (parent de `21454d9`) calculait, pour chaque alias normalisé, `hasLongerLinkedAlias` en balayant tous les alias normalisés. Le prédicat était : un alias plus long commence par `"$alias "` et le suffixe respecte `^(de|d|du|des|a|au)(\s|$).*`.

La comparaison avant/après est homogène pour cette préparation : mêmes 2 485 alias normalisés, même prédicat historique reconstruit dans le test, 5 échauffements puis 20 itérations JVM. Les autres mesures ne disposent pas d'une exécution historique complète reproductible dans le commit courant ; elles sont donc des profils du matcher actuel et ne sont pas présentées comme un gain avant/après.

| Opération JVM | Min | Médiane | Max | n |
|---|---:|---:|---:|---:|
| Baseline historique, balayage pairwise | 99 049 µs | 111 966 µs | 117 184 µs | 20 |
| Après, pré-calcul immuable des préfixes | 843 µs | 870 µs | 1 094 µs | 20 |

Le coût du prédicat préparé est réduit d'environ 99,2 % à la médiane. La construction complète actuelle du matcher est de 26 570 / 33 127 / 46 902 µs (min/médiane/max), mais elle inclut normalisation et compilation des regex ; elle ne peut pas être comparée directement aux valeurs du rapport prématuré.

## Implémentation et parité

L'optimisation construit une fois un `Set` immuable des bases d'alias liés en parcourant les séparateurs de chaque alias long. L'ordre de `aliases`, les regex, les priorités, les protections arôme/goût/extrait, les E-numbers et toutes les règles contextuelles restent inchangés. `KnowledgeIndex` et `IndexedCandidateProvider` restent hors du chemin de production.

Le test JVM compare exhaustivement le résultat du prédicat historique pairwise et le pré-calcul sur les 2 485 alias normalisés de la connaissance actuelle. Il ajoute aussi des cas synthétiques de connecteurs, faux préfixes, articles et prépositions. Le corpus fonctionnel couvre alias courts/longs/composés, FR/NL/EN/DE, E-numbers en casse, espaces, tirets et parenthèses, collisions, arôme/arôme naturel/extrait/goût, lait végétal, inconnus et occurrences répétées. Les comparaisons du matcher profilé et non profilé sont égales pour chaque cas.

Les tests de phase 2 restent la couverture de parité des compositions à trois niveaux, pourcentages, parent/enfant, diagnostics, traces, verdicts et résultats conditionnels. Aucune donnée ni moteur de verdict n'a été modifié.

## Profil JVM actuel

Les appels répétés du matcher, 20 itérations après 5 échauffements : recherche exacte 531 / 856 / 2 075 µs ; multilingue 654 / 809 / 1 422 µs ; texte long 12 578 / 13 764 / 19 871 µs ; sans correspondance 3 306 / 3 487 / 4 027 µs. Les E-numbers font partie du corpus de parité. Les analyses complètes actuelles sont : simple 38 477 / 44 945 / 53 126 µs, imbriquée 49 830 / 55 372 / 68 780 µs, multilingue 39 690 / 42 636 / 49 019 µs et traces 30 897 / 33 558 / 40 032 µs.

Le collecteur sur le corpus compte 2 490 entrées d'alias, 2 485 alias normalisés, 443 220 tests regex et 577 candidats. Il confirme que le balayage et les regex demeurent le coût principal des recherches répétées. Aucune mesure d'allocations fiable n'est revendiquée : le heap et le GC ne permettent pas un delta attribuable ici.

## Android, limites et périmètre

Le test instrumenté `Phase4MatcherProfileInstrumentedTest` existe et mesure construction froide, appels répétés, analyse complète et PSS processus. Il n'a pas été exécuté dans cette reprise : `adb` est absent du `PATH` et aucun appareil n'est adressable. Les mesures Android antérieures du rapport prématuré ne sont pas reprises comme validation.

` :mutation-core:test` a réussi. `testDebugUnitTest` a exécuté 44 suites mais échoue dans six tests d'importeurs éditoriaux hors périmètre, car le module Python `pypdf` est absent (`ModuleNotFoundError`) ; aucun importeur ou dépendance Python n'a été modifié. Cette condition empêche de déclarer la phase prête pour la production malgré la parité ciblée réussie.

Fichiers modifiés : `mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt`, `mutation-core/src/test/kotlin/com/example/isitvegan/Phase4MatcherProfileTest.kt`, ce rapport et `REVIEW_0_7_PHASE4_SEQUENCE_CORRECTION_REPORT.md`.

Fichiers volontairement inchangés : `VerdictEngine`, `VeganAnalyzer`, `IngredientAnalysisService`, parseur, tokenizer, OCR, données de connaissance, assets, importeurs Python, `KnowledgeIndex`, `IndexedCandidateProvider`, version de l'application.

Les mesures restent sensibles au JIT, GC, fréquence CPU et cache de fichiers. La preuve exhaustive est limitée à la base actuelle ; le test synthétique maintient le contrat général des connecteurs. Une relecture indépendante doit évaluer le nouveau diff et ces mesures avant toute phase 5.

## Conclusion

`NO_GO_FOR_PRODUCTION`
