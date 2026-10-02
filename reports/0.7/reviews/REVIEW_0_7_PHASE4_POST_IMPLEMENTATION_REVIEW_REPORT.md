# Relecture indépendante post-implémentation — phase 4 v0.7

## Objet et décision

Cette relecture porte sur le commit `21454d9 perf(v0.7): optimize linked alias matcher preparation`. Elle examine l’optimisation de la préparation de `hasLongerLinkedAlias`, ses tests, ses mesures et son périmètre. Aucun code, test, asset ou rapport existant n’a été corrigé pendant la relecture.

Décision : `NO_GO_PHASE5`.

La transformation inspectée est logiquement saine pour la base actuelle, mais la validation indépendante ne peut pas autoriser la phase 5 : l’arbre de travail est modifié, la variante documentaire annoncée n’existe pas, les mesures post-commit ne sont pas reproductibles sur un état propre dans cette session, et la couverture dédiée ne rend pas explicites tous les cas synthétiques exigés. Cette décision ne conclut pas à une régression fonctionnelle du matcher ; elle conclut à une validation indépendante incomplète.

## État Git et documents

Les commandes initiales ont donné : branche `master`, synchronisée avec `origin/master`, HEAD `21454d9`, puis :

- `PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md` modifié localement ;
- `mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt` modifié localement ;
- `mutation-core/src/test/kotlin/com/example/isitvegan/Phase4MatcherProfileTest.kt` modifié localement ;
- `REVIEW_0_7_PHASE4_SEQUENCE_CORRECTION_REPORT.md` non suivi.

Ces changements préexistants ont été conservés. Ils ne doivent pas être confondus avec le contenu exact de `21454d9`.

`PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT(1).md` n’existe pas dans le dépôt. La variante effectivement retrouvée est `PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md`. Le rapport `REVIEW_0_7_PHASE4_MATCHER_OPTIMIZATION_REPORT.md` est explicitement considéré comme prématuré et invalide pour la baseline et la validation, conformément au contexte de la relecture. Il a été lu seulement pour identifier les affirmations à contrôler, jamais comme preuve.

Les rapports des phases 1 à 3, de readiness, de verrouillage du verdict, d’audit OCR/matching et d’architecture du module JVM ont aussi été inspectés.

## Fichiers examinés

Ont été examinés dans l’état du commit, leur historique ou leur état courant selon le besoin :

- `IngredientMatcher.kt` et sa version parent ;
- `Phase4MatcherProfileTest.kt` ;
- `IngredientAnalysisService`, `AnalysisResult`, `AnalysisDiagnostics` et `TokenDiagnostic` ;
- `VerdictEngine` et `VeganAnalyzer` ;
- `KnowledgeIndex` et `IndexedCandidateProvider` ;
- `Phase2ParityBenchmarkTest`, `KnowledgeIndexTest` et `IndexedCandidateProviderTest` ;
- les rapports phase 1, phase 2, phase 3 et les audits demandés.

## Analyse du diff et du périmètre

Dans le code du commit, le calcul historique était : pour chaque alias normalisé `A`, rechercher un alias `B` tel que `B.length > A.length`, `B.startsWith("$A ")` et que le suffixe après cet espace corresponde à `^(?:de|d|du|des|a|au)(?:\s|$).*`.

Le nouveau code construit un ensemble immuable de bases. Pour chaque alias long, il examine chaque séparateur espace, extrait la base et le suffixe, puis ajoute la base seulement si elle existe déjà dans l’ensemble des alias normalisés et si le même prédicat de suffixe réussit. La consultation lors de la création des entrées devient une appartenance à cet ensemble.

Cette transformation conserve :

- `TextNormalizer.normalize`, le filtrage des blancs et `distinct()` ;
- les séparateurs effectivement considérés, uniquement les espaces présents dans la chaîne normalisée ;
- les six connecteurs `de`, `d`, `du`, `des`, `a`, `au` et leurs frontières ;
- les regex de matching, le tri décroissant par longueur, les collisions et l’ordre des candidats ;
- les règles de contexte arôme, goût, extrait, lait végétal, E-numbers, allemand et inconnus.

L’inspection du diff ne montre pas de changement de `VerdictEngine`, `VeganAnalyzer`, `IngredientAnalysisService`, parser, tokenizer, OCR, sélection de langue OCR, assets, données de connaissance, importeurs Python ou version applicative. Le commit global contient cependant aussi les fichiers des phases 1 à 3 (`KnowledgeIndex`, `IndexedCandidateProvider`, lexique et tests correspondants), car ces travaux ont été regroupés dans le même commit. Ils sont hors de la transformation spécifique du matcher et restent hors production ; ce regroupement doit être signalé lors de la validation Git finale.

## Équivalence du prédicat

Pour toute chaîne `B` et tout séparateur situé après `A`, le nouveau calcul produit `A` et le suffixe `B` après `A + espace`. La condition `base in aliases` reproduit la condition historique selon laquelle `A` devait être l’alias candidat. La longueur strictement supérieure est implicite dès qu’un suffixe suit le séparateur. Aucun faux fragment avant espace ne peut donc être ajouté.

L’égalité de la regex et de l’espacement a été vérifiée dans le diff historique. La normalisation est effectuée avant les deux chemins. Les doublons sont éliminés par `distinct()` au même endroit ; les collisions entre ingrédients restent dans les `AliasEntry` et continuent à être triées comme auparavant.

Le test du commit recalcule le prédicat pairwise sur toute la collection d’alias normalisés et compare l’ensemble des bases historiques avec l’ensemble pré-calculé. Il parcourt donc les 2 485 alias annoncés par la mesure de la connaissance actuelle ; une égalité d’ensembles sur le même domaine couvre les bases vraies et, par complément, les bases fausses. Le test ne masque aucun sous-ensemble par une sélection de cas.

Limite observée : le test ne pose pas une assertion indépendante `size == 2485` et ne fournit pas un cas synthétique explicite pour chacun des six connecteurs. Les alias réels couvrent le prédicat de la base courante, mais la protection contre une future régression de connecteur reste moins explicite que le corpus requis. Les tests de phase 2/3 couvrent FR/NL/EN/DE, formes allemandes, arômes, extraits, compositions, traces, inconnus, occurrences, diagnostics et statuts ; la phase 4 dédiée ne constitue donc pas seule une parité métier complète avant/après.

Le test compare aussi un matcher profilé à un matcher non profilé. Cette comparaison est utile pour vérifier que l’instrumentation n’altère pas le résultat, mais les deux instances utilisent l’algorithme optimisé. Elle ne constitue pas à elle seule une comparaison historique ; la preuve historique vient du prédicat pairwise et de l’inspection du diff.

## Performance

Fait observé dans le rapport post-implémentation disponible : la mesure annoncée compare le balayage pairwise historique et le pré-calcul avec 5 échauffements et 20 itérations sur les mêmes 2 485 alias. Les médianes annoncées sont environ 111 966 µs avant et 870 µs après, soit une réduction de `(111966 - 870) / 111966 = 99,22 %`, arrondie à 99,2 %.

Cette comparaison est homogène pour le sous-problème de préparation du prédicat si les mesures ont bien été prises avec le même processus et la même connaissance, ce que le code du test indique. Elle ne mesure pas la construction complète du matcher : celle-ci inclut normalisation, création des entrées et compilation des regex. Elle ne démontre pas non plus un gain global d’analyse Android.

La construction complète, les recherches, les analyses, le parsing et les diagnostics sont rapportés séparément. Les affirmations de gain global doivent rester limitées au prédicat préparé. Les effets du JIT, du GC, du cache de fichiers, de la fréquence CPU et de la précision de `System.nanoTime()` sont correctement pertinents ; la médiane réduit l’effet d’une valeur isolée sans transformer ce benchmark JVM en mesure statistique multi-machine.

Mesure indépendante reproduite pendant cette relecture : aucune. La relecture est restée en lecture seule et l’arbre ne correspondait pas exactement au commit, ce qui empêchait de présenter un nouveau run comme mesure propre du commit. Les valeurs ci-dessus sont donc des affirmations mesurées du rapport post-implémentation, contrôlées méthodologiquement par inspection, et non une nouvelle campagne indépendante.

## Tests et échecs connus

Le rapport indique que `:mutation-core:test` réussit et que le test ciblé de phase 4 réussit. Les résultats XML présents dans le workspace confirment le succès du test ciblé exécuté précédemment, mais ce résultat provient du working tree local modifié et non d’un checkout propre de `21454d9`.

`testDebugUnitTest` a échoué dans six tests d’importeurs éditoriaux avec `ModuleNotFoundError: No module named 'pypdf'`. Ces tests sont hors du périmètre du matcher et l’erreur est environnementale : elle survient lors de l’appel à un importeur Python, sans modification de cet importeur par la phase 4. Elle ne masque pas un échec du test ciblé `Phase4MatcherProfileTest`, mais elle empêche de déclarer la validation globale verte. Aucune dépendance n’a été installée et aucun importeur n’a été exécuté pendant cette relecture.

La suite globale JVM du module `mutation-core` est donc une validation ciblée réussie, tandis que la suite Android JVM globale reste partiellement non validée pour une cause externe hors périmètre.

## Android

`Phase4MatcherProfileInstrumentedTest` existe et mesure construction, appels répétés, analyses et PSS. `adb` n’est pas disponible dans l’environnement et aucun appareil n’a été accessible. Aucune mesure Android n’a été reproduite pendant cette relecture.

Les anciennes valeurs Android figurant dans les documents ne sont pas utilisées comme preuve indépendante de cette implémentation. Les valeurs JVM ne sont pas présentées comme des mesures appareil. La PSS, lorsqu’elle est mentionnée, reste une mesure globale du processus dépendante du GC et ne permet pas d’attribuer précisément une allocation au matcher.

## Invariants métier

L’optimisation ne touche qu’au booléen préparé `hasLongerLinkedAlias`. Par inspection du diff, aucune règle de statut `VEGAN`, `VEGETARIAN`, `NON_VEGAN` ou `UNCERTAIN` n’est modifiée. Les inconnus, traces, compositions, parents, enfants, pourcentages, occurrences, diagnostics et résultats conditionnels restent gérés par le même pipeline.

`KnowledgeIndex` et `IndexedCandidateProvider` ne sont pas appelés par `IngredientAnalysisService` ni par le chemin applicatif. Aucune migration d’asset, changement de stockage, branchement de l’index ou modification du verdict n’est observé.

## Anomalies

Anomalies bloquantes pour l’autorisation de phase 5 :

- l’état de travail est modifié et ne permet pas une validation finale du commit sans ambiguïté ;
- la variante `PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT(1).md` annoncée n’existe pas ;
- aucune mesure indépendante n’a été reproduite sur un état exactement égal au commit ;
- la couverture dédiée manque de cas synthétiques explicites pour chaque connecteur et certaines formes E-number avec tiret/parenthèses.

Anomalies non bloquantes pour le comportement observé :

- la logique du prédicat est équivalente par inspection et par égalité des ensembles sur la base actuelle ;
- l’absence d’Android est correctement isolée et ne transforme pas les résultats JVM en résultats appareil ;
- l’échec `pypdf` est externe au matcher et précisément localisé ;
- le gain de 99,2 % est un gain de préparation du prédicat, pas une preuve de gain global de l’application.

## Conclusion

Le matcher du commit `21454d9` conserve le prédicat historique et son ordre de matching selon l’analyse du diff. Les tests existants démontrent l’équivalence sur la base d’alias actuelle et les suites des phases précédentes couvrent une large partie des invariants métier. La relecture ne trouve pas de régression métier dans la transformation inspectée.

Elle ne peut toutefois pas autoriser la phase 5 avant une validation sur un arbre propre correspondant exactement au commit, une clarification documentaire de la variante absente et une couverture explicitement renforcée des cas synthétiques annoncés. `KnowledgeIndex` doit rester hors production, aucune migration d’asset n’est autorisée et aucune modification du verdict n’est autorisée.

